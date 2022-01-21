package gloria.gloriaOptimizer.graph.KleeneSubGraph;

import gloria.gloriaOptimizer.graph.Graph;
import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Plan_Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.gloriaOptimizer.graph.pathSearcher.Path;
import gloria.gloriaOptimizer.graph.pool.Pool;
import gloria.gloriaOptimizer.graph.pool.Pool_SinglePred;
import gloria.gloriaOptimizer.template.CycleFinder;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.template.EventType.EventType;
import gloria.gloriaOptimizer.template.EventType.StartEventType;
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

        //support the hamlet case when there is only one event type in the cycle
        if (this.finder.getEventTypesInCycle().size()==1){
            this.isFlatKleeneWithOneEventType = true;

            Path path = new Path();

            //get the start of the template
            StartEventType startEventType = this.template.getStarts().values().iterator().next();
            //create a start node in the gloria graph
            createStartNode(startEventType);

            //get the first edge from the start to the first event type in the cycle
            Edge k_edge = startEventType.getFirstEventType().getOutgoingKleeneEdge();

            //create a new node for the A+
            ArrayList<EventType> snapshots = new ArrayList<>();
            snapshots.add(k_edge.getLeftEventType());
            Sset sset = new Sset(k_edge.getEdgeQueries(), snapshots, true, k_edge);
            ArrayList<Sset> ssets = new ArrayList<>();
            ssets.add(sset);
            Plan_Node newNode = new Plan_Node(k_edge, ssets);

            Pool pool = this.pools.getOrDefault(k_edge.toString(), new Pool_SinglePred(k_edge));
            pool.setType(this.type);


            //connect the new node with the start node by a transition
            Transition transition = new Transition();
            transition.setLeftNode(this.startNodes.get(0));
            transition.setRightNode(newNode);
            transition.setWeight(weightCompute(transition));
            this.startNodes.get(0).getOutgoingTransitions().add(transition);
            newNode.getIncomingTransitions().add(transition);
            newNode.setExistingCost(transition.getWeight());

            this.pools.put(k_edge.toString(), pool);
            newNode.setPool(this.pools.get(k_edge.toString()));
            pool.getNodeHash().put(newNode.toString(), newNode);


            //add the node into the path and compute its weight
            path.getNodeHashMap().put(k_edge.toString(), newNode);
            //add the node into the path as the first pool
            path.getNodeHashMap().put(startEventType.getOutgoingEdgesToSuc().get(k_edge.getRightEventType().getName()).toString(), this.startNodes.get(0));

            path.setWeight(transition.getWeight());

            this.cycleSharingPaths.add(path);

            return;
        }


        for (EventType et: this.finder.getEventTypesInCycle()){
            HashMap<String, Boolean> poolVisited;
            Path path = new Path();
            //ONLY ONE START EVENT TYPE
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

        //nothing to prune
        if (this.cycleSharingPaths.size()<2)
            return;

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

        //put the node into the path
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
