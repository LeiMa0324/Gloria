package gloria.gloriaOptimizer.graph.pathSearcher;

import gloria.gloriaOptimizer.graph.Graph;
import gloria.gloriaOptimizer.graph.Transition.Transition;
import gloria.gloriaOptimizer.graph.node.EndNode;
import gloria.gloriaOptimizer.graph.node.Node;
import lombok.Data;

@Data
public class PathSearcher {
    private Path path;
    private Double minCost =Double.POSITIVE_INFINITY;
    private long memory=0;

    public PathSearcher(){
        this.path = new Path();

    }
//TODO: DISTINGUISH KLEENE GRAPH VS SEQ GRAPH
    public Path pathSearch(Graph graph){

        if (hasOnePath(graph)){

            for (EndNode endNode: graph.getEndNodes()){
                //if each end node only has one incoming transition from the preceding pool
                //direct pick the node

                for (Transition transition: endNode.getIncomingTransitions()){
                    path.getNodeHashMap().put(transition.getLeftNode().getEdge().toString(), transition.getLeftNode());
                    reverseDFSTraverse(transition.getLeftNode());
                }


            }

            this.memory = this.path.getNodeHashMap().values().size()* 24L;

        }
        //multiple paths
        else {
            MultiEndsPathSearcher searcher = new MultiEndsPathSearcher();
            this.path = searcher.search(graph);
            this.minCost = searcher.getOptimalPathCost();
            //
            this.memory = (long) searcher.getPaths().size() *this.path.getNodeHashMap().values().size()*24;

        }

        return this.path;

    }

    private boolean hasOnePath(Graph graph){
        for (EndNode endNode: graph.getEndNodes()){
            if (endNode.getIncomingTransitions().size()!=endNode.getPool().getPredPools().size()){
                return false;
            }
        }

        return true;
    }

    private int reverseDFSTraverse(Node node){
        if (node.isStartNode()){
            return 1;
        }

        for (Transition transition: node.getIncomingTransitions()){
            if (!path.getNodeHashMap().containsKey(transition.getLeftNode().getEdge().toString())) {
                path.getNodeHashMap().put(transition.getLeftNode().getEdge().toString(), transition.getLeftNode());
                reverseDFSTraverse(transition.getLeftNode());
            }
        }

        return 1;
    }

    public String toString(){

        StringBuilder sb = new StringBuilder();
        sb.append("Optimal Workload Sharing Plan Cost:").append(minCost).append("\n");


        for (Node node: path.getNodeHashMap().values()){
            if (!node.isStartNode()) {
                sb.append(node.sharingPlanInfo());
            }

        }

        return sb.toString();

    }

}
