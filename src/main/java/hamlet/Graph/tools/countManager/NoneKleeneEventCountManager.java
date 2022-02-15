package hamlet.Graph.tools.countManager;

import hamlet.Graph.Graphlet.Graphlet;
import hamlet.Graph.tools.GraphletManager.GraphletManager_StaticHamlet;
import hamlet.Graph.tools.Utils;
import hamlet.base.Event;
import hamlet.base.EventType;
import hamlet.query.aggregator.Value;
import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * this class manages the intermediate counts of a none-kleene event
 */
@Data
public class NoneKleeneEventCountManager extends EventCountManager{

    public NoneKleeneEventCountManager(){
        super();
    }


    public void firstUpdate(Event event){
        ArrayList<Integer> startQueries = Utils.getInstance().getTemplate().getQueriesStartWithEventType(event.getType());

        for (Integer qid: event.getValidQueries()){
            event.getValues().put(qid,  new Value());
            Value value = event.getValues().get(qid);

            if (startQueries!=null&&startQueries.contains(qid)){
                //prefix process
                updateSnapshotForStartEventPerQuery(event, qid);
            }
            else{
                Graphlet lastKleene;
                //suffix process, get the last kleene graphlet's values
                //static graphlet manager
                if (Utils.getInstance().getGraphType()== Utils.GraphType.STATIC){
                    GraphletManager_StaticHamlet staticGraphletManager = (GraphletManager_StaticHamlet) Utils.getInstance().getGraphletManager();
                    lastKleene = staticGraphletManager.getLastKleeneGraphlet();
                }
                else {

                        //assuming one event type only appear in one gloria.query in a template
                        EventType predEventType = Utils.getInstance().getTemplate().getWorkload().
                                getQueries().get(qid).getPattern().getNoneKleenePredecessor(event.getType());


                        //find all graphlet of predEventType
                        ArrayList<Graphlet> predGraphlets = Utils.getInstance().getGraphletManager().getGraphletHashMap().get(predEventType.getName());
                        //sum them all

                        if (predGraphlets!=null){
                            for (Graphlet p_graphlet: predGraphlets){
                                value = value.add(p_graphlet.getGraphletValues().get(qid));
                            }
                        }


                }
                event.setMetric(Event.Metric.VAL);

            }
            event.getValues().put(qid, value);
        }
    }

    /**
     * none klene start events are always in metric count
     * @param event
     * @param qid
     */
    public void updateSnapshotForStartEventPerQuery(Event event, Integer qid){

        event.getValues().put(qid, new Value(BigInteger.ONE));
        event.setMetric(Event.Metric.VAL); //set the metric to count

    }


}

