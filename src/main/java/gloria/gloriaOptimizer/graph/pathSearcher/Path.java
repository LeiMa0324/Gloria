package gloria.gloriaOptimizer.graph.pathSearcher;

import gloria.gloriaOptimizer.graph.node.Node;
import lombok.Data;

import java.util.HashMap;

@Data
public class Path {
    private HashMap<String, Node> nodeHashMap;
    private double weight =0.0;

    public Path(){
        this.nodeHashMap = new HashMap<>();
    }

    public Path(Path path){
        this.nodeHashMap = new HashMap<>(path.getNodeHashMap());
        this.weight = path.weight;
    }

    public void addCombination(Tuple tuple){
        for (Node node : tuple.getCombination()) {
            if (!node.isEndNode()) {
                this.nodeHashMap.put(node.getEdge().toString(), node);
                node.setVisited(true);
            }
        }

        this.weight+=tuple.getSumWeight();
    }

    public boolean isFull(int nodeNum){
        return this.nodeHashMap.values().size()==nodeNum;
    }

    public void finish(){

    }
}
