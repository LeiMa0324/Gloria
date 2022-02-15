package gloria.gloriaOptimizer.graph.pool;

import gloria.exhaustive.partitionSetCreator;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.TransitionOperation.EndOperation;
import gloria.gloriaOptimizer.graph.TransitionOperation.MergeOperation;
import gloria.gloriaOptimizer.graph.TransitionOperation.ReuseOperation;
import gloria.gloriaOptimizer.graph.node.EndNode;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Plan_Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.gloriaOptimizer.template.EventType.EventType;
import lombok.Data;
import gloria.gloriaOptimizer.template.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Data
public abstract class Pool {


    protected Edge edge;
    protected boolean isStartPool = false;
    protected boolean isEndPool = false;
    protected HashMap<String, Node> nodeHash;
    protected ArrayList<Pool> predPools;
    protected boolean prunable = true;
    protected OptimizerType type;

    public Pool(){
        this.nodeHash = new HashMap<>();
        predPools = new ArrayList<>();
    }

    public Pool(Edge edge){
        this.nodeHash = new HashMap<>();
        this.edge = edge;
        predPools = new ArrayList<>();
    }

    public int construct(){

        if (edge.getRightEventType().getName().equals("END")){
            constructEndPool();
            return 0;
        }

        if (this.type == OptimizerType.EXHAUSTIVE){
            constructByEnumeration();
        }

        if (this.type==OptimizerType.NOPRUNE||this.type==OptimizerType.NORMAL){
            normalConstruct();
        }

        if (this.type == OptimizerType.GREEDY){
            greedyConstruct();
        }

        if (this.type == OptimizerType.NOTSHARE){
            notShareConstruct();
        }


        return 0;
    }

    protected abstract int notShareConstruct();

    public int snapshotDesignationConstruct(EventType eventType){
        Node p_node = new Plan_Node();

        for (Node node: this.predPools.get(0).getNodeHash().values()){

            if (node.isStartNode()||node.getSsets().get(0).getCheckpoints().get(0).getName().equals(eventType.getName())){
                p_node = node;
            }
        }
        if (p_node.isStartNode()){
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

        }else {
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
        }

        return 0;

    }

    public abstract int normalConstruct();

    public abstract int greedyConstruct();

    protected void constructEndPool(){
        EndNode endNode = new EndNode(this.edge.getEdgeQueries(), this);
        endNode.setEdge(this.edge);
        endNode.setPool(this);

        //connect
        for (Pool predC: this.predPools) {


            for (Node p_node : predC.nodeHash.values()) {

                Transition transition = new Transition();
                transition.setLeftNode(p_node);
                transition.setRightNode(endNode);

                for (Sset s : p_node.getSsets()) {
                    EndOperation endOperation = new EndOperation();
                    endOperation.operate(s, this.edge);
                    transition.addOperation(endOperation);
                }
                transition.finish();
            }
        }

        this.nodeHash.put(endNode.toString(), endNode);
        this.isEndPool = true;

    }

    /**
     * only select the local optimal node
     * @return
     */
    public int greedySelect(){
        Double min = Double.POSITIVE_INFINITY;
        String minKey = "";
        for(Node node: this.nodeHash.values()){
            double cost = 0.0;
            for (Transition transition: node.getIncomingTransitions()){
                cost += transition.getWeight()+transition.getLeftNode().getExistingCost();
            }
            node.setExistingCost(cost);

            if (node.getExistingCost()<min){
                min = node.getExistingCost();
                minKey = node.toString();
            }

        }

        String finalMaxKey = minKey;
        this.nodeHash.entrySet().removeIf(stringNodeEntry -> !stringNodeEntry.getKey().equals(finalMaxKey));

        return 0;

    }


    protected boolean isBeneficialToMerge(ArrayList<Sset> ssets){
        int totalF = 0;
        for (Sset s: ssets){
            totalF+= s.isShared()?s.getCheckpointNum():this.edge.getLeftEventType().getFrequency();
        }
        return this.edge.getLeftEventType().getFrequency()<totalF;
    }

    protected boolean isBeneficialToLocalCheck(Sset sset){

        if (this.type.equals(OptimizerType.NOPRUNE)){
            return true;
        }else {

//            if (this.edge.getEdgeQueries().size()>1){
                return this.edge.getLeftEventType().getFrequency()<sset.getCheckpointNum();
//            }
        }
//        return false;
    }

    /**
     * construct a pool by enumerating BellNum partitions
     */
    public void constructByEnumeration(){
        partitionSetCreator<Integer> creator = new partitionSetCreator<Integer>(new HashSet<Integer>(this.edge.getEdgeQueries()));
        Set<Set<Set<Integer>>> partitionSets = creator.findAllPartitions();

        for (Set<Set<Integer>> partition: partitionSets){
            Node node = new Plan_Node();
            node.setPool(this);
            node.setEdge(this.edge);

            for (Set<Integer> set: partition){
                Sset sset = new Sset();
                sset.setQueries(new ArrayList<>(set));
                sset.setShared(sset.getQueries().size()>1);
                if (sset.isShared()){
                    sset.getCheckpoints().add(this.edge.getLeftEventType());
                }
                sset.setEdge(this.edge);
                node.getSsets().add(sset);
            }
            //don't store anything

        }
    }

    public Transition createEmptyTransition(Node PNode){

        Transition transition = new Transition();
        Node rightNode = new Plan_Node();
        rightNode.setEdge(this.edge);
        transition.setLeftNode(PNode);
        transition.setRightNode(rightNode);

        return transition;

    }

}
