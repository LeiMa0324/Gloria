package gloriaOptimizer.graph.KleeneSubGraph;

import gloriaOptimizer.graph.OptimizerType;
import gloriaOptimizer.graph.PruningEngine.PruneEngine;
import gloriaOptimizer.graph.SEQGraph;
import gloriaOptimizer.graph.Transition.Transition;
import gloriaOptimizer.graph.TransitionOperation.LocalOperation;
import gloriaOptimizer.graph.node.Node;
import gloriaOptimizer.graph.node.Plan_Node;
import gloriaOptimizer.graph.node.Sset;
import gloriaOptimizer.graph.pool.Pool;
import gloriaOptimizer.graph.pool.Pool_MultiPred;
import gloriaOptimizer.graph.pool.Pool_SinglePred;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;

import lombok.Data;

import java.util.ArrayList;
@Data
public class NestedKleeneGraph extends SingleKleeneGraph{
    private EventType stopET;

    public NestedKleeneGraph(ArrayList<EventType> eventTypesInCycle, EventType stopET,SEQGraph seqGraph){
        super(eventTypesInCycle, seqGraph);
        this.stopET = stopET;
    }

    public void construct(){
        EventType snapshotET = findMostInnerCycleEventType();
        Edge edge = snapshotET.getOutGoingSEQEdges().iterator().next();
        constructFirstPoolOfInnerCycle(edge);

        for (Edge edge1: edge.getRightEventType().getOutgoingEdgesToSuc().values()){
            linearRecursiveConstructPool(edge1,stopET);

        }

        // finish the left seq of the cycles
        leftPoolsProcess(snapshotET);


    }

    private EventType findMostInnerCycleEventType(){

        ArrayList<EventType> cycleEvents = new ArrayList<>();
        cycleEvents.add(this.eventTypesInCycle.get(0));

        for (EventType et: this.eventTypesInCycle){
            if (et.getIncomingKleeneEdge()!=null){
                cycleEvents.add(et);
            }

            if (et.getOutgoingKleeneEdge()!=null){
                break;
            }
        }
        return cycleEvents.get(cycleEvents.size()-1);
    }

    private Pool constructFirstPoolOfInnerCycle(Edge edge){
        Pool_SinglePred pool = new Pool_SinglePred(edge);
        pool.setType(type);
        Plan_Node node = new Plan_Node();
        node.setEdge(edge);
        ArrayList<EventType> checkpoints = new ArrayList<>();
        checkpoints.add(edge.getLeftEventType());
        Sset sset = new Sset(edge.getEdgeQueries(), checkpoints, true, edge);
        node.getSsets().add(sset);
        node.setExistingCost(0.0);
        node.setPool(pool);

        pool.getNodeHash().put(node.toString(), node);
        this.addPool(pool);
        return pool;
    }

    private void leftPoolsProcess(EventType snapshotET){
        Edge edge = this.stopET.getOutGoingSEQEdges().iterator().next();


        while (!edge.getLeftEventType().toString().equals(snapshotET.toString())) {
            Pool_MultiPred pool_multiPred = new Pool_MultiPred(edge);
            pool_multiPred.setType(this.type);

            for (Edge predEdge: edge.getLeftEventType().getIncomingEdgesFromPred().values()){
                Pool predPool = this.pools.containsKey(predEdge.toString())?this.pools.get(predEdge.toString()):
                        this.seqGraph.getPools().get(predEdge.toString());
                pool_multiPred.getPredPools().add(predPool);
            }
            pool_multiPred.construct();

            if (type==OptimizerType.NORMAL){
                PruneEngine.SEQPrune((Pool)pool_multiPred);
            }

            addPool(pool_multiPred);
            edge = edge.getRightEventType().getOutGoingSEQEdges().iterator().next();
        }

        //connect the p_pools with the innermost one, update the transition and weights
        ArrayList<Pool> p_pools = new ArrayList<>();
        for (Edge p_edge: snapshotET.getIncomingEdgesFromPred().values()){
            p_pools.add(this.pools.containsKey(p_edge.toString())?this.pools.get(p_edge.toString()):
                    this.seqGraph.getPools().get(p_edge.toString()));
        }
        for (Pool p_pool: p_pools) {
            for (Node node : p_pool.getNodeHash().values()) {
                LocalOperation localOperation = new LocalOperation();
                localOperation.setLeftNode(node);
                Node rightNode = this.pools.get(snapshotET.getOutGoingSEQEdges().iterator().next().toString()).getNodeHash().values().iterator().next();
                localOperation.setRightNode(rightNode);
                localOperation.setLeftNodeSset(node.getSsets().get(0));
                localOperation.setRightNodeSset(rightNode.getSsets().get(0));

                Transition transition = new Transition();
                transition.setLeftNode(node);
                transition.setRightNode(rightNode);
                transition.addOperation(localOperation);
                transition.finish();

            }
        }
        
        if (type==OptimizerType.NORMAL) {
            //prune the innermost pool
            PruneEngine.SEQPrune(this.pools.get(snapshotET.getOutGoingSEQEdges().iterator().next().toString()));
        }

    }

    private ArrayList<EventType> getLeftCycleEventTypes(EventType snapshotET){
        int index = 0;
        for (int i=0; i<this.eventTypesInCycle.size(); i++){
            if (this.eventTypesInCycle.get(i).getName().equals(snapshotET.getName())){
                index = i;
                break;
            }
        }

        return new ArrayList<>(this.eventTypesInCycle.subList(0, index));

    }
}
