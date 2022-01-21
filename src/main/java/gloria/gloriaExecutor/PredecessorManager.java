package gloria.gloriaExecutor;

import gloria.base.Event;
import gloria.gloriaOptimizer.template.EventType.EventType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PredecessorManager {
    //<event name, <timestamp, event>> for quick predecessor search
    protected HashMap<String, ArrayList<Event>> storedEvents;

    public PredecessorManager(){
        this.storedEvents = new HashMap<>();
    }


    public Event findLatestSameEventWithSamePredicate(Event event){
        if (this.storedEvents.isEmpty()||!this.storedEvents.containsKey(event.getType().getName())){
            return null;
        }
        //reverse search the latest same event which satisfies the same predicate
        for (int i= this.storedEvents.get(event.getType().getName()).size()-1; i>=0; i--){
            Event lastE = this.storedEvents.get(event.getType().getName()).get(i);
            if (CollectionUtils.intersection(lastE.getValidQueries(), event.getValidQueries())!=null){
                return lastE;
            }
        }
        return null;
    }

    public ArrayList<Event> findPredecessorsOfEventTypeAfterId(EventType p_et, int id){
        ArrayList<Event> p_events = new ArrayList<>();

        if (this.storedEvents.isEmpty()||!this.storedEvents.containsKey(p_et.getName())){
            return null;
        }
        for (int i = this.storedEvents.get(p_et.getName()).size()-1; i>=0; i--){
            Event event = this.storedEvents.get(p_et.getName()).get(i);
            if (event.getEventId()>id){
                p_events.add(event);
            }else {
                break;
            }
        }

        return p_events;
    }
}
