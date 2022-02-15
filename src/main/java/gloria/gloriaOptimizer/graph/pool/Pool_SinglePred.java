package gloria.gloriaOptimizer.graph.pool;

import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.TransitionOperation.*;
import gloria.gloriaOptimizer.graph.node.*;
import lombok.Data;
import gloria.gloriaOptimizer.template.Edge;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Data
/**
 * cluster of a single pred cluster
 */
public class Pool_SinglePred extends Pool {

    public void setSinglePredPool(Pool singlePredPool) {
        this.singlePredPool = singlePredPool;
        this.predPools.add(singlePredPool);
    }

    private Pool singlePredPool;


    public Pool_SinglePred(){
        super(); this.nodeHash = new HashMap<>();
    }
    public Pool_SinglePred(Edge edge){
        super(edge);    this.nodeHash = new HashMap<>();

    }


    public int normalConstruct(){

        Pool predPool = this.singlePredPool==null?this.predPools.get(0):this.singlePredPool;

        for (Node p_node: predPool.nodeHash.values()){
            generateNodesFromPNode(p_node);
        }

        return 0;

    }

    public int greedyConstruct(){
        if (this.singlePredPool==null){
            System.out.println("hh");
        }

        if (singlePredPool!=null&&singlePredPool.nodeHash.values().iterator().hasNext()) {
            Node p_node = singlePredPool.nodeHash.values().iterator().next();

            //always reuse
            if (p_node.getSsets().size() == 1 && p_node.getSsets().get(0).isShared()) {

                ReuseOperation reuseOperation = new ReuseOperation();
                reuseOperation.setLeftNode(p_node);
                reuseOperation.operate(p_node.getSsets().get(0), this.edge);

                Transition tran = new Transition();
                tran.setLeftNode(p_node);

                Node newNode = new Plan_Node();
                newNode.getSsets().add(reuseOperation.getRightNodeSset());
                newNode.setEdge(this.edge);
                newNode.setPool(this);

                tran.setRightNode(newNode);
                tran.addOperation(reuseOperation);

                newNode = tran.finish();

                this.nodeHash.put(newNode.toString(), newNode);

            } else {
                normalConstruct();
                greedySelect();
            }
        }

        return 0;
    }


    public int notShareConstruct(){


        if (singlePredPool.nodeHash.values().iterator().hasNext()) {

            Node p_node = singlePredPool.nodeHash.values().iterator().next();
            Node newNode = new Plan_Node();
            newNode.setEdge(this.edge);
            newNode.setPool(this);

            //always not share
            for (Sset sset: p_node.getSsets()) {
                if (!CollectionUtils.intersection(sset.getQueries(), this.edge.getEdgeQueries()).isEmpty()) {

                    NonShareOperation nonShareOperation = new NonShareOperation();
                    nonShareOperation.setLeftNode(p_node);
                    nonShareOperation.operate(sset, this.edge);

                    Transition tran = new Transition();
                    tran.setLeftNode(p_node);


                    newNode.getSsets().add(nonShareOperation.getRightNodeSset());

                    tran.setRightNode(newNode);
                    tran.addOperation(nonShareOperation);

                    newNode = tran.finish();
                }
            }



            this.nodeHash.put(newNode.toString(), newNode);

        }

        return 0;
    }
    /**
     * the combination of all s-set transitions
     * @param p_node
     */
    protected void generateNodesFromPNode(Node p_node){

        if (p_node.getSsets().size()>1&&isBeneficialToMerge(p_node.getRelatedSsets(this.edge))){
            MergeOperation mergeOperation = new MergeOperation();
            mergeOperation.operate(p_node.getSsets(), this.edge);

            Transition transition = createEmptyTransition(p_node);
            transition.addMergedOperation(mergeOperation);
            Node rightNode = transition.finish();
            rightNode.setPool(this);

            if (nodeHash.containsKey(rightNode.toString())){
                transition.redirectTo(nodeHash.get(rightNode.toString()));
            }else {
                nodeHash.put(rightNode.toString(), rightNode);
            }
        }


        ArrayList<Transition> transitions = new ArrayList<>();

        //for every s-set multiply the transitions
        for (Sset sset: p_node.getSsets()){
            transitions = transitionMultiplication(transitions, sset, p_node);

        }

        // finish all transitions and add new nodes into cluster
        for (Transition transition: transitions){
            Node newNode = transition.finish();
            newNode.setPool(this);
            if (nodeHash.containsKey(newNode.toString())){
                transition.redirectTo(nodeHash.get(newNode.toString()));
            }else {
                nodeHash.put(newNode.toString(), newNode);
            }
        }

    }

    public ArrayList<Transition> transitionMultiplication(ArrayList<Transition> transitions, Sset p_sset, Node p_node){

        ArrayList<Operation> operations = new ArrayList<>();

        if (transitions.isEmpty()){
            Transition transition = createEmptyTransition(p_node);
            transitions.add(transition);
        }

        if (p_sset.isShared()) {

            //create reuse transition
//            if (!p_sset.containsCheck(this.edge.getLeftEventType().getName())) {
            ReuseOperation reuseOperation = new ReuseOperation();
            reuseOperation.setLeftNode(p_node);
            reuseOperation.operate(p_sset, this.edge);

            operations.add(reuseOperation);
//            }

            if (isBeneficialToLocalCheck(p_sset)&&CollectionUtils.intersection(p_sset.getQueries(), this.edge.getEdgeQueries()).size()>1
//                    ||p_sset.containCheckpoint(this.edge.getLeftEventType().getName())
            ) {
                LocalOperation localOperation = new LocalOperation();
                localOperation.setLeftNode(p_node);
                localOperation.operate(p_sset, this.edge);

                operations.add(localOperation);
            }
        }
        if (!p_sset.isShared()){

            if (CollectionUtils.intersection(p_sset.getQueries(), this.edge.getEdgeQueries()).size()>1
                    &&this.edge.getEdgeQueries().size()>1&&isBeneficialToLocalCheck(p_sset)) {
                LocalOperation localOperation = new LocalOperation();
                localOperation.setLeftNode(p_node);
                localOperation.operate(p_sset, this.edge);
                operations.add(localOperation);

            }

            NonShareOperation nonShareOperation = new NonShareOperation();
            nonShareOperation.setLeftNode(p_node);
            nonShareOperation.operate(p_sset, this.edge);

            operations.add(nonShareOperation);
        }


        return transitionMitosis(transitions, operations);
    }

    /**
     * multiply the transition by number of operations
     * @param transitions
     * @param operations
     * @return
     */
    public ArrayList<Transition> transitionMitosis(ArrayList<Transition> transitions, ArrayList<Operation> operations){
        ArrayList<Transition> newTrans = new ArrayList<>();
        for (Transition tran: transitions){
            for (Operation op: operations){

                Transition copyTran = new Transition();
                copyTran.setLeftNode(tran.getLeftNode());
                Node newNode = new Plan_Node();
                newNode.getSsets().addAll(tran.getRightNode().getSsets());
                newNode.setEdge(tran.getRightNode().getEdge());
                newNode.setPool(this);

                copyTran.setRightNode(newNode);
                copyTran.addOperation(op);

                newTrans.add(copyTran);

            }
        }

        return newTrans;
    }






    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Pool of edge "+this.edge.toString()+"\n");
        int i =0;
        for (Node node: this.nodeHash.values()){
            sb.append("     Node "+i+": "+node.nodeInfo());
            i++;
        }

        return sb.toString();
    }
}
