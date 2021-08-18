package gloriaOptimizer.costModel;

import gloriaOptimizer.graph.TransitionOperation.MergeOperation;
import gloriaOptimizer.graph.TransitionOperation.Operation;
import gloriaOptimizer.graph.node.EndNode;
import gloriaOptimizer.graph.node.Sset;
import gloriaOptimizer.template.EventType.EventType;
import org.apache.commons.collections4.CollectionUtils;

public class CostModel {

    /**
     * Edges: A -> B -> C
     * Plan case 1:
     *          (1, 2, 3)A, (1,2)A
     *          B.cost = A*(B->C).Q/((A->B).Q)
     * Plan case 2:
     *          (1, 2, 3)X, (1,2)X
     *          B.cost = A*X*(B->C).Q/((A->B).Q)
     * compute the e
     */
    public static Double checkToReuseCost(Operation operation){
        Double cost = 0.0;

        //the left edge's checkpoint and first event type
        EventType A = operation.getLeftNodeSset().getEdge().getLeftEventType();

        for (EventType X: operation.getLeftNodeSset().getCheckpoints()) {

            if (X == A) {
                cost = A.getFrequency() * (operation.getRightNodeSset().getQueries().size() + 0.0) /
                        (operation.getLeftNodeSset().getQueries().size() + 0.0);
            } else {
                cost = A.getFrequency() * X.getFrequency() *
                        (operation.getRightNodeSset().getQueries().size() + 0.0) /
                        (operation.getLeftNodeSset().getQueries().size() + 0.0) + 0.0;
            }
        }

        return cost;

    }

    /*
     * cases:
     *    left node    right node
     *      start - > local check
     *      check - > local check
     *      not share -> local check

     * @return the cost of the transition
     */

    public static Double anyToLocalCheckCost(Operation operation){


        //start - > local check.  return A.cost
        if (operation.getLeftNode().isStartNode()){
            return anyToNotShareCost(operation);
        }

        //check - > local check. A(X) -> B -> C, B 's cost
        if (operation.getLeftNodeSset().isShared()){
            EventType B = operation.getRightNode().getEdge().getLeftEventType();
            Integer Q = operation.getRightNodeSset().getQueries().size();
            Double cost = checkToReuseCost(operation);
            for (EventType X: operation.getLeftNodeSset().getCheckpoints()){
                cost+=snapshotEvaluationCost(B, X, Q);
            }
            return cost;
        }
        //not share -> local check. A -> B -> C, B's cost, equals not share to not share
        if (!operation.getLeftNodeSset().isShared()){

            EventType A = operation.getLeftNodeSset().getEdge().getLeftEventType();
            EventType B = operation.getLeftNodeSset().getEdge().getRightEventType();
            Integer Q = operation.getRightNodeSset().getQueries().size();  //Q is the later sset's size
            return notShareCost(A, B, Q);
        }
        System.out.println("Exception: CostModel:anyToLocalCheckCost.");
        return -1.0;
    }

    /**
     * cases:
     *      start - > not share
     *      not share -> not share

     * @return
     */

    public static Double anyToNotShareCost(Operation operation){


        //start -> not share. return A.cost
        if (operation.getLeftNode().isStartNode()){
            return operation.getRightNode().getEdge().getLeftEventType().getFrequency()*operation.getRightNodeSset().getQueries().size()+0.0;
        }

        //not share -> not share. A -> B -> C, B 's cost
        if (!operation.getLeftNodeSset().isShared()){
            EventType A = operation.getLeftNodeSset().getEdge().getLeftEventType();
            EventType B = operation.getLeftNodeSset().getEdge().getRightEventType();
            Integer Q = operation.getRightNodeSset().getQueries().size();
            return notShareCost(A, B, Q);
        }

        System.out.println("Exception: CostModel:anyToNotShareCost.");

        return -1.0;

    }

    /**
     * cases:
     *      not share -> end
     *      check -> end

     * @return
     */
    public static Double anyToEnd(Operation operation){

        if (!operation.getRightNode().isEndNode()){
            System.out.println("Exception: CostModel:anyToEnd.");
            return -1.0;
        }

        EndNode endNode = (EndNode) operation.getRightNode();

        //not share -> end
        if (!operation.getLeftNodeSset().isShared()){
            EventType A = operation.getLeftNodeSset().getEdge().getLeftEventType();
            EventType B = operation.getLeftNodeSset().getEdge().getRightEventType();
            Integer Q = CollectionUtils.intersection(endNode.getQueries(), operation.getLeftNodeSset().getQueries()).size();
            return notShareCost(A, B, Q);
        }

        // check -> end
        if (operation.getLeftNodeSset().isShared()){
            EventType E = operation.getLeftNodeSset().getEdge().getRightEventType();
            Integer Q = CollectionUtils.intersection(endNode.getQueries(), operation.getLeftNodeSset().getQueries()).size();

            Double cost = 0.0;
            for (EventType X: operation.getLeftNodeSset().getCheckpoints()){
                cost+=snapshotEvaluationCost(E, X, Q);
            }
            return cost;
        }

        System.out.println("Exception: CostModel:anyToEnd.");
        return 0.0;

    }

    /**
     * cases:
     *      not share/share -> merge
     * @param
     * @param
     * @return
     */
    public static Double anyToMerge(MergeOperation mergeOperation){

        Double cost = 0.0;
        for (Sset sset: mergeOperation.getLeftNodeSsets()){
            int Q = CollectionUtils.intersection(mergeOperation.getRightNodeSset().getQueries(),
                    sset.getQueries()).size();
            if (sset.isShared()) {
                EventType X = sset.getCheckpoints().get(0);
                EventType A = sset.getEdge().getLeftEventType();


                if (X==A){
                    cost += A.getFrequency()*(Q+0.0)/
                            (sset.getQueries().size()+0.0);
                }else {
                    cost += A.getFrequency()*X.getFrequency()*
                            (Q+0.0)/
                            (sset.getQueries().size()+0.0)+0.0;
                }
                cost+=snapshotEvaluationCost(A, X, Q);
            }

            if (!sset.isShared()){
                if (sset.getEdge()==null){  //start
                    cost+= mergeOperation.getRightNode().getEdge().getLeftEventType().getFrequency()*Q;
                }else {
                    cost += notShareCost(sset.getEdge().getLeftEventType(), sset.getEdge().getRightEventType(), Q);
                }
            }
        }
        return cost;
    }


    /**
     * the evaluation cost of Event E carrying checkpoint for queries Q
     * @param E the event type
     * @param X the checkpoint
     * @param Q the related query set
     * @return evaluation cost E*X*Q
     */
    public static Double snapshotEvaluationCost(EventType E, EventType X, Integer Q){

        return E.getFrequency()*X.getFrequency()*Q+0.0;
    }
    /**
     * the B.cost of not share (A, B)
        B.cost = A*B*Q
     * @param A the preceding event type
     * @param B the current event type
     * @param Q the related query set
     * @return non-share execution cost of B = ABQ
     */
    public static Double notShareCost(EventType A, EventType B, Integer Q){

        return A.getFrequency()*B.getFrequency()*Q+0.0;
    }
}
