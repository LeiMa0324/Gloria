package gloriaOptimizer.template;

import base.Event;
import gloriaOptimizer.template.EventType.EventType;
import lombok.Data;

import java.util.ArrayList;

@Data
public class CycleFinder {
    private ArrayList<EventType> eventTypesInCycle;
    private Template template;
    private boolean isNested = false;
    private ArrayList<Edge> outputEdges;
    private int edgeNums;

    public CycleFinder (){
        this.eventTypesInCycle = new ArrayList<>();
        this.outputEdges = new ArrayList<>();
    }


    public void  findCycles(Template template){
        this.template = template;
        ArrayList<EventType> eventTypes = new ArrayList<>(this.template.getNameToEventTypes().values());

        OUTER:
        for (EventType et: eventTypes){
            for (Edge edge: et.getIncomingEdgesFromPred().values()){
                if (edge.isKleene()){
                    if (!contains(et)) {
                        this.eventTypesInCycle.add(et);
                    }

                    EventType lastEventTypeInCycle = edge.getLeftEventType();
                    this.isNested = findNestedCycle(et, lastEventTypeInCycle);
//                    if (this.isNested){
//                        break OUTER;
//                    }

                }
            }
        }
    }


    public boolean findNestedCycle(EventType startEventType, EventType lastEventType){

        boolean isNested = false;
        EventType et = startEventType;
        while (!et.getName().equals(lastEventType.getName())){
            et =et.getOutGoingSEQEdges().toArray().length>1?lastEventType:et.getOutGoingSEQEdges().iterator().next().getRightEventType();

            if (et.getIncomingKleeneEdge()!=null){
                isNested = true;

            }

            boolean contains = contains(et);
            if (!contains){
                this.eventTypesInCycle.add(et);
            }

        }

        return isNested;

    }
    //find all seq edges out from the cycle
    public ArrayList<Edge> findSucceedingSEQEdge(){
        ArrayList<Edge> SEQEdges = new ArrayList<>();
        for(EventType et: this.eventTypesInCycle){
            for (Edge seq_edge: et.getOutGoingSEQEdges()){
                if (!isEdgeInCycle(seq_edge)){
                    SEQEdges.add(seq_edge);
                    this.outputEdges.addAll(seq_edge.getPrecedingSEQEdges());
                }
            }

        }

        return SEQEdges;
    }

    public int edgeNums(){
        int edgeNum = 0;
        for (EventType et: this.eventTypesInCycle){
            for (Edge edge: et.getIncomingEdgesFromPred().values()){
                if (isEdgeInCycle(edge)){
                    edgeNum++;
                }
            }
        }
        this.edgeNums = edgeNum;
        return edgeNum;
    }

    public boolean isEdgeInCycle(Edge edge){
        boolean leftIn = false;
        boolean rightIn = false;
        for (EventType eventType: this.eventTypesInCycle){
            if (eventType.getName().equals(edge.getLeftEventType().getName())){
                leftIn = true;
            }
            if (eventType.getName().equals(edge.getRightEventType().getName())){
                rightIn = true;
            }
        }

        return leftIn&&rightIn;
    }

    public boolean contains(EventType eventType){
        for (EventType exist_et: this.eventTypesInCycle){
            if (exist_et.getName().equals(eventType.getName())){
                return true;
            }
        }

        return false;
    }
}
