package gloriaExecutor;

import base.Event;
import lombok.Data;
import query.predicate.Predicate;

import java.util.ArrayList;

@Data
public class PredicateManager {
    private ArrayList<Predicate> predicates;

    public PredicateManager(){
        this.predicates = new ArrayList<>();
    }

    public PredicateManager(ArrayList<Predicate> predicates){
        this.predicates = predicates;
    }

    public void setValidQueriesForEvent(Event event){

        ArrayList<Integer> validQueries = new ArrayList<>();
        if (isAPredicateEvent(event)) {
            ArrayList<Event> events = new ArrayList<>();
            events.add(event);


            for (int i = 0; i < this.predicates.size(); i++) {
                Predicate p = this.predicates.get(i);
                if (p.verify(events)) {
                    validQueries.add(i);
                }
            }
        }else {
            validQueries = event.getType().getQueries();
        }

        event.setValidQueries(validQueries);
    }

    public boolean isAPredicateEvent(Event event){
        return predicates.get(0).getEventTypeNames().get(0).equals(event.getType().getName());
    }
}
