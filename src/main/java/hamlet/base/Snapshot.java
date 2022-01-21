package hamlet.base;

import hamlet.Graph.Graphlet.Graphlet;
import hamlet.Graph.tools.Utils;
import hamlet.query.aggregator.Value;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Snapshot {
    private HashMap<Integer, Value> values;
    private Levels snapshotLevel;
    private Event event;
    private ArrayList<Integer> allqids = Utils.getInstance().getQueryIds();

    private int eventIndex;

    //create an event-level snapshot
    public Snapshot(Event event, HashMap<Integer, Value> values){
        //pass the values to the event snapshot
        this.values = values;

        for (Integer qid: allqids){
            if (!values.containsKey(qid)){
                this.values.put(qid, Value.ZERO);
            }
        }
        this.event = event;
        this.eventIndex = event.getEventIndex();
    }

    //add the prefix counts to the old graphlet's total count to get a new graphlet snapshot
    public Snapshot(Graphlet lastKleeneGraphlet, int eventIndex, HashMap<Integer, Value> prefixValues, EventType eventType){

        this.values = new HashMap<>();
        this.eventIndex = eventIndex;

        ArrayList<Integer> startQueries = Utils.getInstance().getTemplate().getQueriesStartWithEventType(eventType);

        for (Integer qid : prefixValues.keySet()){
            Value value = this.values.getOrDefault(qid, new Value());
            //increment 1 for start
//            if (startQueries!=null){
//                value = Value.ONE;
//            }
            //increment from last graphlet's values, and the last graphlet snapshot
            if (lastKleeneGraphlet!=null&&lastKleeneGraphlet.getGraphletValues().containsKey(qid)){
                value = value.add(lastKleeneGraphlet.getGraphletValues().get(qid));

                if (Utils.getInstance().getSnapshotManager().getGraphletSnapshots().size()!=0){
                    if (!Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().values.containsKey(qid)){
                        System.out.println();
                    }
                    value = value.add(Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().values.get(qid));
                }

            }
            //increment from prefix between graphlets
            value = value.add(prefixValues.get(qid));
            this.values.put(qid, value);

        }


        for (Integer qid: allqids){
            if (!values.containsKey(qid)){
                values.put(qid, Value.ZERO);
            }
        }

        snapshotLevel = Levels.GRAPHLET;
    }


   public enum Levels{
        EVENT,
        GRAPHLET
    }
}
