package gloriaOptimizer.graph;

import gloriaOptimizer.graph.pool.Pool;
import gloriaOptimizer.template.EventType.EventType;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.StartEventType;

import java.util.ArrayList;

public class SEQGraph extends Graph {


    public SEQGraph(){
        super();
    }

    public void construct(){

        //randomly choose a start
        StartEventType startEventType = this.template.getStarts().values().iterator().next();

        createStartNode(startEventType);

        Edge edge = startEventType.getFirstEventType().getOutgoingEdgesToSuc().values().iterator().next();
        linearRecursiveConstructPool(edge,null);

    }

    public void constructWithStop(EventType stopET){

        for (StartEventType startEventType :this.template.getStarts().values()){

            createStartNode(startEventType);
            //todo: multiple edges
            Edge edge = startEventType.getFirstEventType().getOutgoingEdgesToSuc().values().iterator().next();
            linearRecursiveConstructPool(edge,stopET);
        }


    }



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for (Pool c: this.pools.values()){
            sb.append(c.toString()+"\n");
        }
        return sb.toString();
    }


}
