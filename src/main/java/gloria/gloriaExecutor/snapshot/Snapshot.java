package gloria.gloriaExecutor.snapshot;

import gloria.base.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import gloria.gloriaOptimizer.graph.node.Sset;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class Snapshot {
    private Integer id;
    private HashMap<Integer, Value> qidToValue;
    private Event event;
    private Sset sset;

    private int eventIndex;

    public Snapshot(){
        this.qidToValue = new HashMap<>();
    }

    public Snapshot(Event event, Sset sset){
        this.qidToValue = new HashMap<>();
        this.event = event;
        this.sset = sset;
    }

    protected Value getValueForQuery(int qid){
        return this.qidToValue.get(qid);
    }

}
