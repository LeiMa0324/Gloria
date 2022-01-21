package gloria.gloriaExecutor;


import gloria.base.Event;
import lombok.Data;
import gloria.query.aggregator.Aggregator;
import gloria.users.stockUser.stockAttributeEnum;

import java.util.ArrayList;

@Data
public class PaneLoader {

    private ArrayList<Event> events;
    private final PredicateManager predicateManager;
    private Aggregator aggregator;

    public PaneLoader(PredicateManager predicateManager){
        this.predicateManager = predicateManager;
        this.aggregator = aggregator;

    }

    /**
     * load the events into panes
     * @param events
     * @return
     */

    public ArrayList<ArrayList<Event>> load(ArrayList<Event> events){

        ArrayList<ArrayList<Event>> panes = new ArrayList<>();
        ArrayList<Event> validEvents = new ArrayList<>();

        String latestEvent = "";
        String lastTimeStamp = "";
        ArrayList<Event> tempPane = new ArrayList<>();

        for (int i =0; i<events.size(); i++) {

            Event event = events.get(i);
            predicateManager.setValidQueriesForEvent(event);

            if (event.getValidQueries().isEmpty()) {
                continue;
            }

            //get the last event
            latestEvent =latestEvent.equals("")? event.getType().getName():latestEvent;

            //get the last time stamp
            lastTimeStamp = lastTimeStamp.equals("")?(String) event.getAttributeValueByName(stockAttributeEnum.date.toString()):lastTimeStamp;

            event.setEventId(validEvents.size());

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

}
