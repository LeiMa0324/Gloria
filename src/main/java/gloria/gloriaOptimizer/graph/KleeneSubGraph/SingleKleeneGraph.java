package gloria.gloriaOptimizer.graph.KleeneSubGraph;

import gloria.gloriaOptimizer.graph.Graph;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.PruningEngine.PruneEngine;
import gloria.gloriaOptimizer.graph.SEQGraph;
import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Plan_Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.gloriaOptimizer.graph.pool.Pool;
import gloria.gloriaOptimizer.graph.pool.Pool_MultiPred;
import gloria.gloriaOptimizer.graph.pool.Pool_SinglePred;
import gloria.gloriaOptimizer.costModel.ExistingCostExpr;
import lombok.Data;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.template.EventType.EventType;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SingleKleeneGraph extends Graph {
    protected SEQGraph seqGraph;
    protected Pool pseudoFeedbackPool;
    protected ArrayList<EventType> eventTypesInCycle;

    public SingleKleeneGraph(){
        this.pseudoFeedbackPool = new Pool_SinglePred();
        this.pools = new HashMap<>();
    }

    public SingleKleeneGraph(ArrayList<EventType> eventTypesInCycle, SEQGraph seqGraph){
        this.pseudoFeedbackPool = new Pool_SinglePred();
        this.eventTypesInCycle = eventTypesInCycle;
        this.seqGraph = seqGraph;
        this.pools = new HashMap<>();

    }

    public void construct(){
        constructPseudoPool();

        //first pool construction
        Pool_MultiPred firstPool = new Pool_MultiPred((Edge) eventTypesInCycle.get(0).getOutgoingEdgesToSuc().values().toArray()[0]);
        firstPool.setType(this.type);
        for (Edge p_edge: eventTypesInCycle.get(0).getIncomingSEQEdges()){
            firstPool.getPredPools().add(seqGraph.getPools().get(p_edge.toString()));

        }
        firstPool.getPredPools().add(pseudoFeedbackPool);


        constructFirstPool(firstPool);

        if (this.type== OptimizerType.NORMAL) {
            PruneEngine.KleenePrune(firstPool);
        }
        addPool(firstPool);

        Pool predPool = firstPool;

        //following pools in the cycle
        for (int i =1; i<eventTypesInCycle.size();i++) {
            Pool_SinglePred pool = new Pool_SinglePred(eventTypesInCycle.get(i).getOutgoingEdgesToSuc().values().iterator().next());
            pool.setSinglePredPool(predPool);
            pool.setType(this.type);
            pool.construct();

            if (this.type == OptimizerType.NORMAL){
                PruneEngine.KleenePrune(pool);
            }
            addPool(pool);
            predPool = pool;
        }

        feedbackPoolProcess(predPool);
        buildNextSEQPools();

    }

    public void constructPseudoPool(){

        Edge feedbackEdge = this.eventTypesInCycle.get(0).getIncomingKleeneEdge();

        int j =0;
        for (EventType et: eventTypesInCycle){
            Plan_Node pseudoNode = new Plan_Node();
            ArrayList<EventType> checkpoints = new ArrayList<>();
            checkpoints.add(et);
            Sset sset = new Sset(feedbackEdge.getEdgeQueries(), checkpoints, true, feedbackEdge);
            pseudoNode.setEdge(feedbackEdge);
            pseudoNode.getSsets().add(sset);

            pseudoNode.setExistingCost(0.0);
            pseudoNode.setExistingCostExpr(initiateCostExpr(j));

            pseudoNode.setPool(this.pseudoFeedbackPool);
            this.pseudoFeedbackPool.getNodeHash().put(pseudoNode.toString(), pseudoNode);
            this.pseudoFeedbackPool.setEdge(feedbackEdge);
            j++;
        }
        this.pseudoFeedbackPool.setPrunable(false);
    }

    public void constructFirstPool(Pool_MultiPred firstPool){
        firstPool.construct();

        for (Node node: firstPool.getNodeHash().values()){
            for (Transition transition: node.getIncomingTransitions()){
                transition.setRightNode(node);
                node.setExistingCostExpr(node.getExistingCostExpr().sum(transition.getLeftNode().getExistingCostExpr()));
            }
        }
        System.out.println(firstPool.getNodeHash().values());
        firstPool.setPrunable(true);
    }

    /**
     * initiate cost expressions for pseudo nodes
     * @param index
     * @return
     */
    public ExistingCostExpr initiateCostExpr(int index){
        //set up cost functions
        ArrayList<Boolean> variables = new ArrayList<>();
        ArrayList<Integer> coeffs = new ArrayList<>();
        for (int i =0; i<eventTypesInCycle.size(); i++){
            variables.add(false);
            coeffs.add(0);
        }
        variables.set(index, true);
        coeffs.set(index, 1);

        return new ExistingCostExpr(0.0, variables, coeffs);
    }

    private void feedbackPoolProcess(Pool feedbackPool){
        for (Node node: feedbackPool.getNodeHash().values()){
            node.setExistingCost(node.getExistingCostExpr().evaluateWithZeros());
        }

        PruneEngine.nodePrune_SEQ(feedbackPool);
        //replace pseudo pool
    }

    protected void buildNextSEQPools(){


        for (Edge edge: this.eventTypesInCycle.get(eventTypesInCycle.size()-1).getOutGoingSEQEdges()) {
            while (edge != null) {
                Pool_SinglePred pool = new Pool_SinglePred(edge);
                pool.setType(this.type);
                pool.setSinglePredPool(this.pools.get(edge.getLeftEventType().getIncomingSEQEdges().get(0).toString()));
                pool.construct();

                if (type==OptimizerType.NORMAL) {
                    PruneEngine.SEQPrune(pool);
                }
                addPool(pool);
                edge = edge.getRightEventType().getOutgoingEdgesToSuc().values().isEmpty() ? null :
                        edge.getRightEventType().getOutgoingEdgesToSuc().values().iterator().next();
            }
        }
    }

    protected boolean isEventInCycle(EventType eventType){
        if (eventType==null){
            return false;
        }
        for (EventType et: this.eventTypesInCycle){
            if (et.getName().equals(eventType.getName())){
                return true;
            }
        }

        return false;
    }
}
