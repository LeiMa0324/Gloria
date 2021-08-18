package gloriaOptimizer.graph.pathSearcher;

import gloriaOptimizer.graph.Graph;
import gloriaOptimizer.graph.KleeneSubGraph.FullySharedKleeneGraph;
import gloriaOptimizer.graph.Transition.Transition;
import gloriaOptimizer.graph.node.EndNode;
import gloriaOptimizer.graph.node.Node;
import gloriaOptimizer.template.Edge;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Data
public class MultiEndsPathSearcher {

    private ArrayList<Path> paths;
    private Graph graph;
    private Double optimalPathCost;
    private ArrayList<Double> pathWeights;

    public MultiEndsPathSearcher(){
        this.paths = new ArrayList<>();
    }

    public Path search(Graph graph){
        this.graph = graph;
        DFSPathEnumeration(graph);
        return selectOptimalPath();
    }


    public void DFSPathEnumeration(Graph graph){
        Path initPath;
        Tuple initCombination;
        int poolNum;
        if (graph.isSEQGraph()) {
            initPath = new Path();
            initCombination = new Tuple();
            initCombination.setCombination(new ArrayList<>(graph.getStartNodes()));
            //end node pools +one start pool
            poolNum = graph.getPools().size() - graph.getEndNodes().size() - graph.getStartNodes().size();

            recursiveDFSForNodeCombination(initCombination, initPath, poolNum);
        }else {
            for (Path path: ((FullySharedKleeneGraph)graph).getCycleSharingPaths()){
                initPath = path;
                initCombination = new Tuple();
                ArrayList<Node> combination = new ArrayList<>();
                //assuming only one output edge
                Edge outputEdge = ((FullySharedKleeneGraph) graph).getFinder().getOutputEdges().get(0);
                combination.add(path.getNodeHashMap().get(outputEdge.toString()));

                //end node pools +one start pool
                poolNum = graph.getPools().size() - graph.getEndNodes().size() - graph.getStartNodes().size();
                initCombination.setCombination(combination);

                recursiveDFSForNodeCombination(initCombination, initPath, poolNum);
            }
        }


        // find all ending pools
        HashSet<String> endPools = new HashSet<>();

        for (EndNode endNode: this.graph.getEndNodes()){
            for (Transition transition: endNode.getIncomingTransitions()){
                endPools.add(transition.getLeftNode().getEdge().toString());

            }
        }
        for (Path path: this.paths){
            updateWeightForFinalEvaluation(path, endPools);
        }


    }
    /**
     * find the path from a combination to all ends, recursively
     * @param  combination to all ends, a combination could be one node or multiple nodes
     * @return
     */
    private void recursiveDFSForNodeCombination(Tuple combination, Path path, int poolNum){

        //including the starts
        if (path.isFull(poolNum)){
            this.paths.add(path);

        }else {

            ArrayList<Tuple> rightNodeCombinations = findNextCombinations(combination);

            if (rightNodeCombinations!=null) {
                for (Tuple com : rightNodeCombinations) {
                     Path newPath = new Path(path);
                     newPath.addCombination(com);
                     recursiveDFSForNodeCombination(com, newPath, poolNum);
                }
            }

        }

    }

    private Path selectOptimalPath(){

        //summing ending nodes' existing cost for all paths
        Double miniCost = Double.POSITIVE_INFINITY;
        int optimalPathIndex = -1;
        int i =0;
        if (this.paths.size()==0){
            System.out.println("hh");
        }
        for (Path path: this.paths) {

            if (path.getWeight()<miniCost){
                miniCost = path.getWeight();
                optimalPathIndex = i;
            }

            i++;
        }
        this.optimalPathCost = miniCost;


        return this.paths.get(optimalPathIndex);

    }

    private void updateWeightForFinalEvaluation(Path path, HashSet<String> endPools){
        //updates evaluation cost
        for (String endPool: endPools){
            for (Transition transition:path.getNodeHashMap().get(endPool).getOutgoingTransitions()){
                if (transition.getRightNode().isEndNode()){
                    path.setWeight(path.getWeight()+transition.getWeight());
                }
            }
        }
    }




    private ArrayList<Tuple> findNextCombinations(Tuple combination){

        if (combination.getCombination().size()==1) {

            HashMap<String, ArrayList<Node>> succeedingNodesForPool = new HashMap<>();

            //sort the right nodes for each pool
            for (Transition transition : combination.getCombination().get(0).getOutgoingTransitions()) {
                Node rightNode = transition.getRightNode();
                ArrayList<Node> nodes = succeedingNodesForPool.getOrDefault(rightNode.getEdge().toString(), new ArrayList<>());
                if (rightNode.getPool().getNodeHash().containsKey(rightNode.toString())
                && (!rightNode.getEdge().isKleene())
                ){
                    nodes.add(rightNode);
                    succeedingNodesForPool.put(rightNode.getEdge().toString(), nodes);
                }

            }

            //find combinations
            NodeCombinator<Node> combinator = new NodeCombinator<Node>(succeedingNodesForPool);
            combinator.setOriginalCombination(combination);
            return combinator.run();
        }else {

            HashMap<String, ArrayList<Node>> succeedingNodesForPool = new HashMap<>();

            //sort the right nodes for each pool
            for (Node node: combination.getCombination()) {

                for (Transition transition : node.getOutgoingTransitions()) {
                    Node rightNode = transition.getRightNode();
                    ArrayList<Node> nodes = succeedingNodesForPool.getOrDefault(rightNode.getEdge().toString(), new ArrayList<>());


                    if (!nodes.contains(rightNode)){
                        nodes.add(rightNode);
                    }

                    succeedingNodesForPool.put(rightNode.getEdge().toString(), nodes);
                }
            }
            //find combinations
            NodeCombinator<Node> combinator = new NodeCombinator<Node>(succeedingNodesForPool);
            combinator.setOriginalCombination(combination);
            return combinator.run();

        }
    }

}
