package gloriaOptimizer.graph;

import gloriaOptimizer.graph.KleeneSubGraph.NestedKleeneGraph;
import gloriaOptimizer.graph.KleeneSubGraph.SingleKleeneGraph;
import gloriaOptimizer.graph.pool.Pool;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;
import gloriaOptimizer.template.Template;
import lombok.Data;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Set;

/**
 * read the GloriaOptimizer.template and construct the graph
 * if only seq, just construct seq graph
 * if seq+kleene, construct SEQ/KLEENE graph and concat them
 */
@Data
public class GraphConstructor {
    private Template template;
    private OptimizerType type;

    private ArrayList<EventType> eventTypesInCycle;
    private  EventType stopET;

    public GraphConstructor(Template template){
        this.template = template;
        this.eventTypesInCycle = new ArrayList<>();
        this.type = OptimizerType.NORMAL;
    }

    public Graph constructGraph(){

        ArrayList<EventType> eventTypes = new ArrayList<>(this.template.getNameToEventTypes().values());
        boolean isNested = false;
        OUTER:
        for (EventType et: eventTypes){
            for (Edge edge: et.getIncomingEdgesFromPred().values()){
                if (edge.isKleene()){
                    this.eventTypesInCycle.add(et);
                    this.template.getNameToEventTypes().remove(et.getName());
                    EventType lastEventTypeInCycle = edge.getLeftEventType();
                    isNested = findNestedCycle(et, lastEventTypeInCycle);
                    if (isNested){
                        break OUTER;
                    }

                }
            }
        }

        Graph graph = new Graph();
        //SEQ Graph
        if (!hasCycle()){
            graph = seqGraphBuilding(null);
        }
        //SEQ + SINGLE CYCLE GRAPH
        if (hasCycle()&&(!isNested)){

            SEQGraph seqGraph = seqGraphBuilding(this.eventTypesInCycle.get(0).getOutgoingEdgesToSuc().values().iterator().next());

//            SingleKleeneGraph SGraph = new SingleKleeneGraph(this.eventTypesInCycle, seqGraph);
//            SGraph.setType(this.type);
//            SGraph.construct();

            graph.mergeGraph(seqGraph);
//            graph.mergeGraph(SGraph);
        }
        //SEQ + NESTED CYCLE GRAPH
        if (hasCycle()&&isNested){
            SEQGraph seqGraph = seqGraphBuilding(this.eventTypesInCycle.get(0).getOutgoingEdgesToSuc().values().iterator().next());

            NestedKleeneGraph NGraph = new NestedKleeneGraph(this.eventTypesInCycle,this.stopET, seqGraph);
            NGraph.construct();

            graph.mergeGraph(seqGraph);
            graph.mergeGraph(NGraph);
        }
        return graph;
    }

    public boolean findNestedCycle(EventType startEventType, EventType lastEventType){

        boolean isNested = false;
        EventType et = startEventType;
        while (!et.getName().equals(lastEventType.getName())){
            et =et.getOutGoingSEQEdges().toArray().length>1?lastEventType:et.getOutGoingSEQEdges().iterator().next().getRightEventType();

            if (et.getIncomingKleeneEdge()!=null){
                isNested = true;
                this.stopET = startEventType;

            }

            this.template.getNameToEventTypes().remove(et.getName());

            boolean contains = false;
            for (EventType exist_et: this.eventTypesInCycle){
                if (exist_et.getName().equals(et.getName())){
                    contains = true;
                }
            }
            if (!contains){
                this.eventTypesInCycle.add(et);
            }

        }

        this.template.getNameToEventTypes().remove(lastEventType.getName());

        return isNested;

    }

    private boolean hasCycle(){
        return this.eventTypesInCycle.size()!=0;
    }

    private SEQGraph seqGraphBuilding(Edge stopEdge){
        SEQGraph seqGraph = new SEQGraph();

        if (stopEdge==null){
            seqGraph.setTemplate(template);
            seqGraph.setType(this.type);
            seqGraph.construct();

        }else {

            seqGraph.setTemplate(template);
            seqGraph.setType(this.type);
            seqGraph.constructWithStop(this.stopET);
        }

        return seqGraph;
    }


}
