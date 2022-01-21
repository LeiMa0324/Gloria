package gloria.gloriaOptimizer.graph.node;

import java.util.ArrayList;

public class StartNode extends Node{
    //the queries that start from this node
    private ArrayList<Integer> queries;

    public StartNode(ArrayList<Integer> queries){
        super();
        this.isStartNode = true;
        this.queries = queries;
        this.existingCost = 0.0;
        Sset sset = new Sset();
        sset.setQueries(queries);
        sset.setShared(false);
        this.ssets.add(sset);

    }
}
