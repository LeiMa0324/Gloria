package gloriaOptimizer.graph.node;

import gloriaOptimizer.graph.pool.Pool;
import lombok.Data;

import java.util.ArrayList;


@Data
public class EndNode extends Node{

    private ArrayList<Integer> queries;
    public EndNode(ArrayList<Integer> queries, Pool pool){
        super();
        this.queries = queries;
        this.isEndNode = true;
    }


}
