package gloria.gloriaOptimizer.graph;

import gloria.gloriaOptimizer.graph.PruningEngine.PruneEngine;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.pool.Pool;
import gloria.gloriaOptimizer.graph.node.EndNode;
import gloria.gloriaOptimizer.graph.node.StartNode;
import gloria.gloriaOptimizer.graph.pool.Pool_MultiPred;
import gloria.gloriaOptimizer.graph.pool.Pool_SinglePred;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.template.EventType.EventType;
import gloria.gloriaOptimizer.template.EventType.StartEventType;
import lombok.Data;
import gloria.gloriaOptimizer.template.Template;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;

@Data
public class Graph {

    protected ArrayList<StartNode> startNodes;
    protected ArrayList<EndNode> endNodes;
    protected HashMap<String, Pool> pools;
    protected Template template;
    private ArrayList<Pool> poolArrayList;
    protected OptimizerType type;
    protected long memory =0;
    protected boolean isSEQGraph = true;
    protected boolean isFlatKleeneWithOneEventType=false;  // if it's A+

    public Graph(){
        this.startNodes = new ArrayList<>();
        this.endNodes = new ArrayList<>();
        this.pools = new HashMap<>();
        this.poolArrayList = new ArrayList<>();
        this.type = OptimizerType.NORMAL;
    }

    public Graph( Template template){
        new Graph();
        this.template = template;
    }

    public void addPool(Pool pool){
        if (!this.pools.containsKey(pool.getEdge().toString())) {
            pools.put(pool.getEdge().toString(), pool);
            this.poolArrayList.add(pool);

            if (pool.isEndPool()){
                this.endNodes.add((EndNode) pool.getNodeHash().values().toArray()[0]);
            }
        }
    }

    public void  mergeGraph(Graph graph){
        this.startNodes.addAll(graph.startNodes);
        this.endNodes.clear();
        graph.pools.forEach((String, pool) -> {
            this.pools.put(String, pool);
            if (pool.isEndPool()){
                this.endNodes.add((EndNode) pool.getNodeHash().values().toArray()[0]);
            }
        });

    }


    public void createStartNode(StartEventType startEventType){
        StartNode startNode = new StartNode(startEventType.getQueries());
        startNode.setEdge(startEventType.getOutgoingEdgesToSuc().values().iterator().next());

        if (!this.pools.containsKey(startNode.getEdge().toString())) {
            this.startNodes.add(startNode);

            Pool_SinglePred startPool = new Pool_SinglePred();
            startPool.setStartPool(true);
            startPool.setEdge(startEventType.getOutgoingEdgesToSuc().values().iterator().next());
            startPool.getNodeHash().put(startNode.toString(), startNode);
            addPool(startPool);
        }
    }

    public int linearRecursiveConstructPool(Edge currentEdge, EventType stopET){


        if (this.pools.containsKey(currentEdge.toString())||
                (stopET!=null&& shouldStop(currentEdge, stopET))) {
            return 0;
        }


        ArrayList<Edge> p_edges = new ArrayList<>();

        //if the preceding edge is a self kleene one
        Edge preceding_selfKleeneEdge = currentEdge.getPrecedingSelfKleeneEdge();

        if (preceding_selfKleeneEdge!=null){
            p_edges.add(preceding_selfKleeneEdge);
        }else {
            p_edges = currentEdge.getPrecedingSEQEdges();
        }

        Pool pool;

        //single preceding edge
        if (p_edges.size()==1){

            pool = new Pool_SinglePred(currentEdge);


            ((Pool_SinglePred) pool).setSinglePredPool(this.pools.get(p_edges.get(0).toString()));
            pool.setType(this.type);
            pool.construct();

        }

        //multiple preceding edges
        else {

            pool = new Pool_MultiPred(currentEdge);
            for (Edge p_edge: p_edges){

                if (this.pools.containsKey(p_edge.toString())){
                    pool.getPredPools().add(this.pools.get(p_edge.toString()));
                }else {
                    for (StartEventType startEventType: this.template.getStarts().values()){
                        if (!CollectionUtils.intersection(startEventType.getQueries(), p_edge.getEdgeQueries()).isEmpty()
                                ){
                            createStartNode(startEventType);

                            Edge edge = startEventType.getFirstEventType().getOutgoingEdgesToSuc().values().iterator().next();
                            linearRecursiveConstructPool(edge, stopET);
                        }
                    }
                }
            }
            //construct pool of multi p_clusters
            if (pool.getPredPools().size()==p_edges.size()){
                pool.setType(this.type);
                pool.construct();

            }
        }

        if (type==OptimizerType.NORMAL){
            //prune
            PruneEngine.SEQPrune(pool);
        }

        //add pool to hashmap
        addPool(pool);


        //recursively do for next edges
        for (Edge next: currentEdge.getRightEventType().getOutgoingEdgesToSuc().values()) {
            linearRecursiveConstructPool(next, stopET);
        }

        return 1;

    }


    public void computeMemory(){
        for (Pool pool: this.pools.values()){
            for (Node node: pool.getNodeHash().values()){
                //Node memory: S-set and cost memory
                this.memory += 24;
                //transition memory:

                if (!node.isStartNode()){
                    this.memory += node.getIncomingTransitions().size()* 12L;
                }

            }
        }
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for (Pool p: this.poolArrayList){
            if (!p.getEdge().getLeftEventType().isStart()) {
                sb.append(p.toString() + "\n");
            }
        }
        return sb.toString();
    }

    protected boolean shouldStop(Edge currentEdge, EventType stopET){

        if (stopET.getName().equals(currentEdge.getLeftEventType().getName())){
                return true;
            }


        return false;

    }


}
