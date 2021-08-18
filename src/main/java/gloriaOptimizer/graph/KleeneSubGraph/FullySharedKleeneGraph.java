package gloriaOptimizer.graph.KleeneSubGraph;

import gloriaOptimizer.graph.Graph;
import gloriaOptimizer.graph.Transition.Transition;
import gloriaOptimizer.graph.node.Node;
import gloriaOptimizer.graph.node.Plan_Node;
import gloriaOptimizer.graph.node.Sset;
import gloriaOptimizer.graph.pathSearcher.Path;
import gloriaOptimizer.graph.pool.Pool;
import gloriaOptimizer.graph.pool.Pool_SinglePred;
import gloriaOptimizer.template.CycleFinder;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;
import gloriaOptimizer.template.EventType.StartEventType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class FullySharedKleeneGraph extends Graph {
    //the cycle paths
    private ArrayList<Path> cycleSharingPaths;
    private CycleFinder finder;




    public FullySharedKleeneGraph(){
         this.cycleSharingPaths = new ArrayList<>();
         this.isSEQGraph=false;
    }

    public void construct(){
        this.finder = new CycleFinder();
        finder.findCycles(this.template);

        switch (this.type){
            case NOPRUNE:
                normalConstruct();
                break;
            case NORMAL:
                normalConstruct();
                prune();
                break;
            case GREEDY:
                greedyConstruct();
                break;
            case NOTSHARE:
                greedyConstruct();
                break;
        }

        extendBySEQEdges();

    }

    public void extendBySEQEdges(){

        for (Edge seq_edge: this.finder.findSucceedingSEQEdge()){
            linearRecursiveConstructPool(seq_edge,null);
        }
    }

    public void normalConstruct(){

        for (EventType et: this.finder.getEventTypesInCycle()){
            HashMap<String, Boolean> poolVisited;
            Path path = new Path();
            //randomly choose a start
            StartEventType startEventType = this.template.getStarts().values().iterator().next();
            createStartNode(startEventType);
            Edge edge = startEventType.getFirstEventType().getOutgoingEdgesToSuc().values().iterator().next();
            ArrayList<Transition> pathTransitions = new ArrayList<>();
            cycleTraverse(edge, et, new HashMap<>(), path,  pathTransitions);

            //TRANSITIONS FROM KLEENE POOL TO SEQ POOL
            for (Node node:path.getNodeHashMap().values()){
                for (Edge p_edge:node.getEdge().getLeftEventType().getIncomingEdgesFromPred().values()){
                    boolean contains = false;
                    for (Transition transition: node.getIncomingTransitions()){
                        if (transition.getLeftNode().getEdge().toString().equals(p_edge.toString())){
                            contains=true;
                        }
                    }
                    if (!contains){
                        Node p_node = path.getNodeHashMap().get(p_edge.toString());
                        Transition newTransition = new Transition();
                        newTransition.setLeftNode(p_node);
                        newTransition.setRightNode(node);
                        newTransition.setWeight(weightCompute(newTransition));
                        path.setWeight(path.getWeight()+newTransition.getWeight());
                        pathTransitions.add(newTransition);
                    }
                }

                node.setExistingCost(path.getWeight());
            }

            this.cycleSharingPaths.add(path);

        }
    }

    private void prune(){
        int minSnapshotNum = Integer.MAX_VALUE;
        double minCost = Double.POSITIVE_INFINITY;
        int p_id = 0;
        int optimal_p_id = 0;

        for (Path path: this.cycleSharingPaths){
            if (path.getWeight()<minCost &&
                    path.getNodeHashMap().values().iterator().next().getSnapshotNum()<minSnapshotNum){
                optimal_p_id = p_id;
                for (Pool pool: this.pools.values()){
                    if (!pool.isStartPool()) {
                        pool.getNodeHash().clear();
                        pool.getNodeHash().put(pool.getEdge().toString(), path.getNodeHashMap().get(pool.getEdge().toString()));
                    }
                }
            }

            p_id++;
        }
        Path optimalPath = this.cycleSharingPaths.get(optimal_p_id);
        this.cycleSharingPaths.clear();
        this.cycleSharingPaths.add(optimalPath);
    }

    public void greedyConstruct(){

        //randomly choose a start
        StartEventType startEventType = this.template.getStarts().values().iterator().next();

        createStartNode(startEventType);

        Edge edge = startEventType.getFirstEventType().getOutgoingEdgesToSuc().values().iterator().next();
        linearRecursiveConstructPool(edge,null);

        Path path = new Path();
        this.pools.forEach((k,v)->{
            path.getNodeHashMap().put(k, v.getNodeHash().values().iterator().next());
        });

        this.cycleSharingPaths.add(path);
    }

    public Path cycleTraverse(Edge edge, EventType snapshotEventType, HashMap<String, Boolean> poolVisited,
                                Path path,
                                ArrayList<Transition> transitions){
        if (path.getNodeHashMap().values().size()==this.finder.edgeNums()){
            return path;
        }

        if ((!this.finder.isEdgeInCycle(edge))||path.getNodeHashMap().containsKey(edge.toString())){
            return null;
        }

        Pool pool = this.pools.getOrDefault(edge.toString(), new Pool_SinglePred(edge));
        pool.setType(this.type);
        //todo: assuming only single pre pools
        Pool p_pool = this.pools.get(edge.getLeftEventType().getIncomingSEQEdges().get(0).toString());
        if (!pool.getPredPools().contains(p_pool)){
            pool.getPredPools().add(p_pool);
        }
        ArrayList<EventType> snapshots = new ArrayList<>();
        snapshots.add(snapshotEventType);
        Sset sset = new Sset(edge.getEdgeQueries(), snapshots, true, edge);
        ArrayList<Sset> ssets = new ArrayList<>();
        ssets.add(sset);
        Plan_Node newNode = new Plan_Node(edge, ssets);
        newNode.setPool(pool);

        Node p_node = new Plan_Node();

        for (Node node: pool.getPredPools().get(0).getNodeHash().values()){

            if (node.isStartNode()||node.getSsets().get(0).getCheckpoints().get(0).getName().equals(snapshotEventType.getName())){
                p_node = node;
            }
        }

        //maintain transitions
        Transition transition = new Transition();
        transition.setLeftNode(p_node);
        transition.setRightNode(newNode);
        transition.setWeight(weightCompute(transition));
        p_node.getOutgoingTransitions().add(transition);
        newNode.getIncomingTransitions().add(transition);


        transitions.add(transition);

        pool.getNodeHash().put(newNode.toString(), newNode);
        path.getNodeHashMap().put(newNode.getEdge().toString(), newNode);
        path.setWeight(path.getWeight()+transition.getWeight());

        this.pools.put(edge.toString(), pool);
        poolVisited.put(edge.toString(), true);

        for (Edge edge1: edge.getRightEventType().getOutgoingEdgesToSuc().values()){
            cycleTraverse(edge1, snapshotEventType, poolVisited,path,  transitions);
        }

        return null;
    }

    private double weightCompute(Transition transition){
        double transitionWeight = 0.0;
        if (transition.getLeftNode().isStartNode()){
            transitionWeight = transition.getRightNode().getEdge().getLeftEventType().getFrequency();

        }else {
            if (transition.getLeftNode().getSsets().get(0).getCheckpoints().get(0).getName().
                    equals(transition.getLeftNode().getEdge().getLeftEventType().getName())){
                transitionWeight =  transition.getLeftNode().getSsets().get(0).getCheckpoints().get(0).getFrequency();
            }else {
                transitionWeight +=transition.getLeftNode().getSsets().get(0).getCheckpoints().get(0).getFrequency()*
                        transition.getLeftNode().getEdge().getLeftEventType().getFrequency();
                if (transition.getLeftNode().getSsets().get(0).getCheckpoints().get(0).getName().equals(
                        transition.getRightNode().getEdge().getLeftEventType().getName())){
                            transitionWeight+=transition.getRightNode().getEdge().getLeftEventType().getFrequency()*
                                    transition.getLeftNode().getSsets().get(0).getQueries().size();
                }

            }
        }

        return transitionWeight;
    }



}
