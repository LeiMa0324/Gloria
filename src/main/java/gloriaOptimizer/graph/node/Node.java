package gloriaOptimizer.graph.node;

import gloriaOptimizer.graph.Transition.Transition;
import gloriaOptimizer.graph.pool.Pool;
import gloriaOptimizer.costModel.ExistingCostExpr;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
public abstract class Node implements Comparable<Node>, Cloneable, Serializable {

    protected Edge edge;
    protected ArrayList<Sset> ssets;
    protected ArrayList<Transition> incomingTransitions;
    protected ArrayList<Transition> outgoingTransitions;

    protected boolean visited = false;

    public void setExistingCost(Double existingCost) {
        this.existingCost = existingCost;
        this.existingCostExpr.setADouble(existingCost);
    }

    protected Double existingCost=0.0;
    protected ExistingCostExpr existingCostExpr = new ExistingCostExpr();
    protected Pool pool;

    protected boolean isStartNode = false;
    protected boolean isEndNode = false;

    public void setStartNode(boolean startNode) {
        isStartNode = startNode;
        this.existingCost = 0.0;
    }




    public Node(){
        this.incomingTransitions = new ArrayList<>();
        this.outgoingTransitions = new ArrayList<>();
        this.ssets = new ArrayList<>();
    }

    public Node(Edge edge){
        this.incomingTransitions = new ArrayList<>();
        this.outgoingTransitions = new ArrayList<>();
        this.edge = edge;
        this.ssets = new ArrayList<>();
    }

    /**
     * Search the MIT: minimal incoming transition
     */
    public void searchMIT_CostValue(){

        HashMap<String, Double> miniWeightForEachPredPool = new HashMap<>();
        HashMap<String, Integer> tranIdForEachPredPool = new HashMap<>();


        int index = 0;
        for (Transition transition: this.incomingTransitions){
            Double miniCost = miniWeightForEachPredPool.isEmpty()?Double.POSITIVE_INFINITY:
                    miniWeightForEachPredPool.getOrDefault(transition.getLeftNode().getEdge().toString(), Double.POSITIVE_INFINITY);

            if (transition.getWeight()+transition.getLeftNode().existingCost< miniCost){
                miniCost = transition.getWeight()+transition.getLeftNode().existingCost;
                String poolName = transition.getLeftNode().getEdge()==null?"(START, "+this.edge.getLeftEventType().getName()+")":
                        transition.getLeftNode().getEdge().toString();
                miniWeightForEachPredPool.put(poolName,miniCost);
                tranIdForEachPredPool.put(poolName, index);
            }
            index ++;

        }

        for (Double weight: miniWeightForEachPredPool.values()){
            this.existingCost +=weight;
        }

        //delete the trans
        ArrayList<Integer> deletedTrans = new ArrayList<>();

        //disconnect the transition with nodes
        for (int i = 0; i<getIncomingTransitions().size(); i++){
            if (!tranIdForEachPredPool.containsValue(i)) {
                deletedTrans.add(i);
            }
        }
//
        deleteTrans(deletedTrans);

    }

    public void computeWeight(){
        for (Transition transition: this.incomingTransitions){
            this.existingCost +=transition.getWeight()+transition.getLeftNode().getExistingCost();
        }
    }

    /**
     * Search the MIT: minimal incoming transition
     */
    public void searchMIT_CostExpr(){
        ExistingCostExpr miniCost = new ExistingCostExpr();
        miniCost.setInfinity(true);
        int bestTransitionIndex = -1;

        int index = 0;
        for (Transition transition: this.incomingTransitions){

            ExistingCostExpr tempCostExpr = transition.getLeftNode().existingCostExpr.sum(transition.getWeight());

            if (miniCost.isGreater(tempCostExpr)){
                miniCost = tempCostExpr;
                bestTransitionIndex = index;
            }
            index ++;

        }
        this.existingCostExpr = miniCost;

        //delete the trans
        ArrayList<Integer> deletedTrans = new ArrayList<>();

        //disconnect the transition with nodes
        for (int i = 0; i<getIncomingTransitions().size(); i++){
            if (i!=bestTransitionIndex) {
                deletedTrans.add(i);
            }
        }
//
        deleteTrans(deletedTrans);

    }


    public int compareTo(Node otherNode) {
        return this.existingCost.compareTo(otherNode.existingCost);
    }



    public boolean equals(Node node){
        if (!this.edge.equals(node.edge)||!(this.ssets.size()==node.ssets.size())){
            return false;
        }else {

            for (int i =0; i<this.ssets.size()-1; i++){
                if (!this.ssets.get(i).equals(node.ssets.get(i))){
                    return false;
                }
            }

            return true;
        }
    }

    //delete the node and its incoming/outgoing transitions
    public void recursiveDelete(){

        // delete the node from the pool
        this.pool.getNodeHash().remove(this.toString());
        this.outgoingTransitions = null;

        for (Transition transition: this.getIncomingTransitions()){
            if (transition.getLeftNode().getOutgoingTransitions().size()==1){
                transition.getLeftNode().recursiveDelete();
            }
            Node leftNode = transition.getLeftNode();
            if (leftNode.getOutgoingTransitions()!=null){
                leftNode.getOutgoingTransitions().remove(transition);
            }

        }
        this.incomingTransitions = null;

    }

    public void deleteTrans(ArrayList<Integer> indices){

        int deletedNum = 0;
        for (int d_index: indices ){
            d_index = d_index-deletedNum;
            if (this.incomingTransitions.get(d_index).getLeftNode().getOutgoingTransitions().size()==1){
                this.incomingTransitions.get(d_index).getLeftNode().recursiveDelete();
            }
            this.incomingTransitions.get(d_index).getLeftNode().removeTransitionToNode(this);
            this.incomingTransitions.remove(d_index);

            deletedNum++;
        }
    }

    public void removeTransitionToNode(Node node){
        int i =0;
        int d_index = 0;
        for (Transition transition: this.outgoingTransitions){

            if (transition.getRightNode().toString().equals(node.toString())){
                d_index = i;
            }
            i++;
        }
        this.getOutgoingTransitions().remove(d_index);
    }

    public int getSnapshotNum(){
        int snapNum = 0;
        for (Sset sset: this.ssets){
            if (!sset.isShared()){
                continue;
            }
            snapNum+= sset.getCheckpointNum();
        }
        return snapNum;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        this.ssets.forEach(sset -> {
            if (!sset.isShared()){
                sb.append(sset.getQueries()+"N, ");
            }else {
                sb.append(sset.getQueries());
                for (EventType chk: sset.getCheckpoints()){
                    sb.append(chk.toString()+"|");
                }
                sb.append(", ");
            }
        });

        return sb.toString();
    }

    public String nodeInfo(){
        StringBuilder sb = new StringBuilder();

        if (!this.isEndNode&&!this.isStartNode) {
            this.ssets.forEach(sset -> {
                if (!sset.isShared()) {
                    sb.append(sset.getQueries() + "N, ");
                } else {
                    sb.append(sset.getQueries());
                    for (EventType chk: sset.getCheckpoints()){
                        sb.append(chk.toString()+"|");
                    }
                    sb.append(", ");
                }
            });
        }else {
            sb.append(isEndNode?"End Node, ":"Start Node, ");
        }

        sb.append("Cost: ").append(this.existingCost).append("\n");

        for (Transition transition: this.incomingTransitions){
            sb.append("         Incoming Transition: ").append(transition.toString());
        }

        return sb.toString();
    }

    public String sharingPlanInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.edge.isKleene()?"Kleene ":" SEQ ").append(
                "Edge ("+this.pool.getEdge().getLeftEventType().getName()+","+this.pool.getEdge().getRightEventType().getName()+")"
        );

        if (!this.isEndNode&&!this.isStartNode) {
            this.ssets.forEach(sset -> {
                if (!sset.isShared()) {
                    sb.append(sset.getQueries() + "N, ");
                } else {
                    sb.append(sset.getQueries());
                    for (EventType chk: sset.getCheckpoints()){
                        sb.append(chk.toString()+"|");
                    }
                    sb.append(", ");
                }
            });
        }else {
            sb.append(isEndNode?"End Node, ":"Start Node, ");
        }

        sb.append("Cost: ").append(this.existingCost).append("\n");


        return sb.toString();
    }

    public String partitionString(){
        StringBuilder sb = new StringBuilder();
        this.ssets.forEach(sset -> {
            sb.append(sset.getQueries());
            if (!sset.isShared()){
                sb.append(sset.getQueries()).append("N");
            }
            sb.append(", ");
        });
        return sb.toString();
    }

    public ArrayList<Sset> getRelatedSsets(Edge edge){
        ArrayList<Sset> ssets = new ArrayList<>();
        for (Sset s: this.ssets){
            if (!CollectionUtils.intersection(s.getQueries(), edge.getEdgeQueries()).isEmpty()){
                ssets.add(s);
            }
        }

        return ssets;
    }

    public void setIncomingTransitions(ArrayList<Transition> transitions){
        this.incomingTransitions = transitions;
        for (Transition transition: this.incomingTransitions){
            transition.setRightNode(this);
        }

    }

    public void mergeSameSsets(){
        HashMap<String, Sset> stringSsetHashMap = new HashMap<>();
        for (Sset sset: this.getSsets()){
            if (stringSsetHashMap.containsKey(sset.getQueries().toString())){
                if (sset.getCheckpoints()!=stringSsetHashMap.get(sset.getQueries().toString()).getCheckpoints()){
                    stringSsetHashMap.get(sset.getQueries().toString()).getCheckpoints().addAll(sset.getCheckpoints());
                }
            }else {
                Sset newSset = new Sset();
                newSset.setEdge(sset.getEdge());
                newSset.setShared(sset.isShared());
                newSset.setQueries(sset.getQueries());
                newSset.getCheckpoints().addAll(sset.getCheckpoints());
                stringSsetHashMap.put(newSset.getQueries().toString(), newSset);
            }
        }

        this.ssets.clear();
        stringSsetHashMap.forEach((key,value)->this.ssets.add(value));
    }

}
