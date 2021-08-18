package gloriaOptimizer.graph.pathSearcher;


import gloriaOptimizer.graph.node.Node;
import lombok.Data;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * give nodes for pools, find all the combinations of one node from each pool
 */
@Data
public class NodeCombinator<T> {

    private Tuple originalCombination;
    private HashMap<String, ArrayList<T>> succeedingNodesForPool;

    public NodeCombinator(HashMap<String, ArrayList<T>> succeedingNodesForPool){
        this.succeedingNodesForPool = succeedingNodesForPool;
    }

    public ArrayList<Tuple> run() {
        Iterator<String> poolIterator = succeedingNodesForPool.keySet().iterator();
        if (!succeedingNodesForPool.isEmpty()) {
            Tuple initCom = new Tuple();
            initCom.setPrecedingTuple(originalCombination);
            ArrayList<Tuple> initComs = new ArrayList<>();
            initComs.add(initCom);

            return recursiveCombines(succeedingNodesForPool.get(poolIterator.next()), initComs, poolIterator);
        }else {
            return null;
        }
    }

    private ArrayList<Tuple> recursiveCombines(ArrayList<T> NodesForPool, ArrayList<Tuple> coms, Iterator<String> poolIterator) {

        ArrayList<Tuple> allCombinations = new ArrayList<>();

        for (T node : NodesForPool) {
            ArrayList<Tuple> newComs = new ArrayList<>();

            for (Tuple oldCom: coms){
                Tuple newCom = new Tuple(oldCom);
                newComs.add(newCom);
            }

            for (Tuple newCom : newComs) {
                if (newCom.getPrecedingTuple().doesConnectToNode((Node)node)){
                    newCom.addNode((Node)node);
                }
            }

            allCombinations.addAll(newComs);
        }

        if (poolIterator.hasNext()) {
            return recursiveCombines(succeedingNodesForPool.get(poolIterator.next()), allCombinations, poolIterator);
        }else {
            return allCombinations;
        }

    }
}

