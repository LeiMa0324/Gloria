package gloria.gloriaOptimizer.graph.pool;

import gloria.gloriaOptimizer.graph.PruningEngine.PruneEngine;
import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.TransitionOperation.LocalOperation;
import gloria.gloriaOptimizer.graph.TransitionOperation.NonShareOperation;
import gloria.gloriaOptimizer.graph.TransitionOperation.Operation;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Plan_Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.template.EventType.EventType;

import java.util.ArrayList;

/**
 * cluster of multi pre_clusters
 */
public class Pool_MultiPred extends Pool {

    public Pool_MultiPred(Edge edge){
        super(edge);
    }


    public int normalConstruct(){
        ArrayList<Pool> indiPools = new ArrayList<>();


        for (Pool predC: this.predPools){
            Pool_SinglePred pool = new Pool_SinglePred(this.edge);
            pool.setSinglePredPool(predC);
            pool.setType(type);
            pool.construct();
            pool.setPrunable(predC.prunable);
            PruneEngine.SEQPrune(pool);
            indiPools.add(pool);
        }

        ArrayList<Node> combinedNodes = NodeCombination(indiPools);

        for (Node node: combinedNodes){
            if (node.getSsets().size()>1&& isBeneficialToMerge(node.getSsets())){
                mergeFromMultiPNodes(node);
            }else {
                addNode(node);
            }
        }

        return 0;
    }

    public int greedyConstruct(){
        normalConstruct();

        return 0;
    }


    public int notShareConstruct(){

        Node newNode = new Plan_Node();
        newNode.setEdge(this.edge);
        newNode.setPool(this);

        for (Pool predC: this.predPools){

            Node p_node = predC.nodeHash.values().iterator().next();

            //always not share
            NonShareOperation nonShareOperation = new NonShareOperation();
            nonShareOperation.setLeftNode(p_node);

            nonShareOperation.operate(p_node.getSsets().get(0), this.edge);

            Transition tran = new Transition();
            tran.setLeftNode(p_node);


            newNode.getSsets().add(nonShareOperation.getRightNodeSset());

            tran.setRightNode(newNode);
            tran.addOperation(nonShareOperation);

            newNode = tran.finish();

        }

        this.nodeHash.put(newNode.toString(), newNode);

        return 0;
    }

    /**
     * combination of nodes in each independent single clusters
     * @param indiPools
     */
    protected ArrayList<Node> NodeCombination(ArrayList<Pool> indiPools){

        ArrayList<Node> combinedNodes = new ArrayList<>(indiPools.get(0).getNodeHash().values());

        for (int i = 1; i< indiPools.size(); i++){

            ArrayList<Node> combinedNodesSnap = new ArrayList<>(combinedNodes);
            ArrayList<Node> resNodes = new ArrayList<>();

            for (Node node: indiPools.get(i).getNodeHash().values()){

                for (Node oldNode: combinedNodesSnap){
                    //copy from old node
                    Node newNode = new Plan_Node();
                    newNode.getSsets().addAll(oldNode.getRelatedSsets(this.edge));
                    newNode.setEdge(this.edge);
                    newNode.setPool(this);
                    ArrayList<Transition> incomingTrans = new ArrayList<>(oldNode.getIncomingTransitions());
                    newNode.setIncomingTransitions(incomingTrans);

                    //copy from this cluster's node
                    newNode.getSsets().addAll(node.getRelatedSsets(this.edge));
                    incomingTrans.addAll(node.getIncomingTransitions());

                    newNode.setIncomingTransitions(incomingTrans);
                    for (Transition transition:incomingTrans){
                        transition.setRightNode(newNode);
                    }

                    resNodes.add(newNode);
                }
            }

            combinedNodes = resNodes;

        }

        return combinedNodes;

    }

    protected void mergeFromMultiPNodes(Node nonMergedNode){
        ArrayList<Node> p_nodes = new ArrayList<>();

        for (Transition transition: nonMergedNode.getIncomingTransitions()){
            p_nodes.add(transition.getLeftNode());
        }

        Node mergedNode = new Plan_Node();
        mergedNode.setEdge(this.edge);
        mergedNode.setPool(this);

        ArrayList<EventType> checkpoints = new ArrayList<>();
        checkpoints.add(this.edge.getLeftEventType());
        Sset rightSset = new Sset(this.edge.getEdgeQueries(), checkpoints, true, this.edge);
        mergedNode.getSsets().add(rightSset);

        for (Node p_node : p_nodes) {


            ArrayList<Sset> leftSsets = new ArrayList<>();
            leftSsets.addAll(p_node.getRelatedSsets(this.edge));

            ArrayList<Operation> operations = new ArrayList<>();

            p_node.getOutgoingTransitions().clear();

            for (Sset sset: p_node.getRelatedSsets(this.edge)){
                LocalOperation localOperation = new LocalOperation();
                localOperation.setLeftNode(p_node);
                localOperation.setRightNode(mergedNode);
                localOperation.operate(sset, this.edge);
                operations.add(localOperation);
            }

            Transition transition = new Transition();

            transition.setLeftNode(p_node);
            transition.setOperations(operations);
            transition.setRightNode(mergedNode);


            mergedNode = transition.finish();
            mergedNode.setPool(this);
            mergedNode.setEdge(this.edge);

        }

        addNode(mergedNode);
    }

    /**
     * final process of node, add the node into hash table
     * @param node
     */
    public void addNode(Node node){

        Double existingCost = 0.0;
        for (Transition transition: node.getIncomingTransitions()){
            existingCost+=transition.getLeftNode().getExistingCost()+transition.getWeight();
        }
        node.setExistingCost(existingCost);
        node.mergeSameSsets();

        //replace the merged node with minimum cost
        if ((!nodeHash.containsKey(node.toString()))||
                nodeHash.containsKey(node.toString())&&node.getExistingCost()<nodeHash.get(node.toString()).getExistingCost()){
            nodeHash.put(node.toString(), node);
        }
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
