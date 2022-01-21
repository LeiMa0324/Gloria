package hamlet.Graph.tools;

import hamlet.base.Event;
import hamlet.query.predicate.Predicate;
import hamlet.query.predicate.SingleValuePredicate;
import lombok.Data;

import java.util.ArrayList;

@Data
public class PredicateManager {
    private ArrayList<SingleValuePredicate> predicates;

    public PredicateManager(ArrayList<SingleValuePredicate> predicates){
        this.predicates = predicates;
    }

    public ArrayList<Integer> getValidQueriesForPredicate(Event event){
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        ArrayList<Integer> res = new ArrayList<>();

        for (int i=0; i<this.predicates.size(); i++) {
            Predicate p = this.predicates.get(i);
            if (p.getEventTypes().contains(event.getType())&&p.verify(events)) {
                res.add(i);
            }
        }

        return res;
    }

    public boolean isAPredicateEvent(Event event){
        return predicates.get(0).getEventTypes().get(0).equals(event.getType());
    }

    public boolean areSamePredicates(ArrayList<Integer> queries){
        SingleValuePredicate firstPredicate = this.predicates.get(queries.get(0));

        for (Integer qid: queries){
            SingleValuePredicate p = this.predicates.get(qid);
            if (!p.equalsPredicate(firstPredicate)) return false;
        }

        return true;
    }
}
