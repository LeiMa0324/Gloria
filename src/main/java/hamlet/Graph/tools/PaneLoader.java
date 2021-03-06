package hamlet.Graph.tools;

import hamlet.base.Event;
import hamlet.base.EventType;
import hamlet.base.Template;
import hamlet.query.aggregator.Aggregator;
import hamlet.users.stockUser.stockAttributeEnum;
import lombok.Data;

import java.util.ArrayList;

@Data
public class PaneLoader {

    private ArrayList<Event> events;
    private final PredicateManager predicateManager;
    private Aggregator aggregator;
    private Template template;
    private String kleene;

    public PaneLoader(PredicateManager predicateManager, Template template){
        this.predicateManager = predicateManager;
        this.aggregator = aggregator;
        this.template = template;

    }

    public ArrayList<ArrayList<Event>> load(ArrayList<Event> events){

        ArrayList<ArrayList<Event>> panes = new ArrayList<>();
        ArrayList<Event> validEvents = new ArrayList<>();

        String latestEvent = "";
        String lastTimeStamp = "";
        ArrayList<Event> tempPane = new ArrayList<>();

        for (int i =0; i<events.size(); i++) {


            Event event = setValidQueries(events.get(i), this.kleene);

            if (event.getValidQueries().isEmpty()) {
                continue;
            }

            //get the last event
            latestEvent =latestEvent.equals("")? event.getType().getName():latestEvent;

            //get the last time stamp
            lastTimeStamp = lastTimeStamp.equals("")?(String) event.getAttributeValueByName(stockAttributeEnum.date.toString()):lastTimeStamp;

            event.setEventIndex(validEvents.size());


            if (event.getType().getName().equals(latestEvent)&&
                    ((String) event.getAttributeValueByName(stockAttributeEnum.date.toString())).equals(lastTimeStamp)) {
                tempPane.add(event);
            } else {
                latestEvent = event.getType().getName();
                lastTimeStamp = (String)event.getAttributeValueByName(stockAttributeEnum.date.toString());
                ArrayList<Event> burst = (ArrayList<Event>) tempPane.clone();

                panes.add(burst);
                tempPane.clear();
                tempPane.add(event);
            }

            validEvents.add(event);

        }
        panes.add(tempPane);
        this.events = validEvents;
        return panes;

    }

    /**
     * set the valid queries for event
     * @param event
     */
    private Event setValidQueries(Event event, String kleene){

        if (event.getType().isKleene()||event.getType().getName().equals(kleene)){
            event.setValidQueries(this.predicateManager.getValidQueriesForPredicate(event));

        }
        // none kleene events, get the queries
        if (!event.getType().isKleene()){
            //get the qid for this event
            event.setValidQueries(this.template.getQueriesOfEventType(event.getType()));
        }
        return event;
    }


}
