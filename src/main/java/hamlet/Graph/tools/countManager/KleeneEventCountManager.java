package hamlet.Graph.tools.countManager;

import hamlet.Graph.tools.Utils;
import hamlet.base.Event;
import hamlet.base.Snapshot;
import hamlet.query.aggregator.Aggregator;
import hamlet.query.aggregator.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class KleeneEventCountManager extends EventCountManager {

    public KleeneEventCountManager(){
        super();
    }


    public void update(Event event){
//        ArrayList<Integer> startQueries = event.getType().getQueriesStartWith();
//        ArrayList<Integer> endQueries = event.getType().getQueriesEndWith();
        int event_id = Utils.getInstance().getEvents().indexOf(event);

        //fast decision of the same predecessors for queries by predicates
        boolean samePredicates = this.getPredicateManager().areSamePredicates(event.getValidQueries());

        // have the same predecessors for all queries
        if (samePredicates){

            HashMap<Integer, BigInteger> coeffsum = new HashMap<>();

            boolean first = Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().getEventIndex()==event_id;

            if (first){
//                System.out.println("First event");
                //set to 1
                ArrayList<Integer> snapshotIds = new ArrayList<>();
                //the last graphlet snapshot
                snapshotIds.add(Utils.getInstance().getSnapshotManager().getSnapshots().indexOf(
                        Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot()));
                //coeff = 1
                coeffsum.put(snapshotIds.get(0), BigInteger.ONE);

                event.setSnapshotIds(snapshotIds);
                event.setSnapIdTocoeffs(coeffsum);
                event.setMetric(Event.Metric.SNAPSHOT);
//                System.out.println(coeffsum);

            }else {
//                System.out.println("Not First event");
                //use a boolean to indicate if this event can be propagating from last event. if it can not, find the right predecessors
                boolean isAbleToBePropagatedFromLastEvent = Utils.getInstance().getEvents().get(event_id-1).getValidQueries().equals(event.getValidQueries());
                if (!isAbleToBePropagatedFromLastEvent){
//                    System.out.println("cannot be propagated from last event");

                    //find all the predecessors
                    ArrayList<Event> preds = Utils.getInstance().getPredecessorManager().getPredecessorsAfterLastGraphletSnapshotForQuery(event, event.getValidQueries().get(0));
                    coeffsum = sumPredSnapshots(event, preds);

                    event.setSnapshotIds(new ArrayList<Integer>(coeffsum.keySet()));

                    for (Integer snapid: coeffsum.keySet()){
                        BigInteger coeff = coeffsum.get(snapid).add(BigInteger.ONE);
                        coeffsum.put(snapid, coeff);
                    }

                    event.setSnapIdTocoeffs(coeffsum);
                    event.setMetric(Event.Metric.SNAPSHOT);
                }else {
//                    System.out.println("can be propagated from last event");

                    //borrow the snapshot id and double the coeffs
                    ArrayList<Event> preds = new ArrayList<>();
                    Event lastE = Utils.getInstance().getEvents().get(event_id-1);
                    //to double the coeffs
                    preds.add(lastE);
                    preds.add(lastE);

                    coeffsum = sumPredSnapshots(event, preds);
                    event.setSnapshotIds(new ArrayList<Integer>(coeffsum.keySet()));
                    event.setSnapIdTocoeffs(coeffsum);
                    event.setMetric(Event.Metric.SNAPSHOT);
//                    System.out.println(coeffsum.toString());

                }
            }

        // have different predecessors for queries, create a event-level snapshot
        }else {
//            System.out.println("creating event snapshot");
            //get predecessors for each gloria.query and decide if they are the same
            HashMap<Integer, ArrayList<Event>> predecessorsForQueries = new HashMap<>();

            //get the predecessors for the first valid gloria.query
            ArrayList<Event> predsForFirstQuery = Utils.getInstance().getPredecessorManager().getPredecessorsAfterLastGraphletSnapshotForQuery(event, event.getValidQueries().get(0));
            predecessorsForQueries.put(event.getValidQueries().get(0), predsForFirstQuery);

            //get the predecessors for the other valid gloria.query
            for (Integer qid: event.getValidQueries()){
                ArrayList<Event> predsForCurrQuery = Utils.getInstance().getPredecessorManager().getPredecessorsAfterLastGraphletSnapshotForQuery(event, qid);
                predecessorsForQueries.put(qid, predsForCurrQuery);
            }



            HashMap<Integer, Value> values = new HashMap<>();

            ArrayList<Integer> validStartQueries = event.getValidQueries();
            validStartQueries.retainAll(Utils.getInstance().getTemplate().getQueriesStartWithEventType(event.getType()));

            // get the actual count for each gloria.query
            for (Integer qid: event.getValidQueries()){

                //evaluate actual count for a gloria.query
                ArrayList<Event> preds = predecessorsForQueries.get(qid);
                HashMap<Integer, BigInteger> coeffsum= sumPredSnapshots(event, preds);

                //evaluate the expression of snapshots
                Value countForQuery = Utils.getInstance().getSnapshotManager().evaluateSnapshotExpressionForQuery(coeffsum, qid);

                //increment the graphlet snapshot to the value for start queries
                if (validStartQueries.contains(qid)){
                    Snapshot lastGraphletSnapshot = Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot();
                    BigInteger newCount = countForQuery.getCount().add(lastGraphletSnapshot.getValues().get(qid).getCount());
                    BigDecimal newSum = countForQuery.getSum().add(lastGraphletSnapshot.getValues().get(qid).getSum());

                    countForQuery.setCount(newCount);
                    countForQuery.setSum(newSum);
                }

                values.put(qid, countForQuery);
            }

            // create a event level snapshot
            Utils.getInstance().getSnapshotManager().createEventSnapshot(event, values);
        }

    }

    /**
     * for the start gloria.query, the corresponding count in the graphlet snapshot + 1
     * @param event
     * @param qid
     */
    public void updateSnapshotForStartEventPerQuery(Event event, Integer qid){

        Value oldValue = Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().getValues().containsKey(qid)?
                Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().getValues().get(qid):
                Value.ZERO;

        Value added = new Value(BigInteger.ONE);
        Aggregator aggregator = Utils.getInstance().getAggregator();
        if (aggregator.getFunc()== Aggregator.Aggregfunctions.COUNT){
            added.setSum(BigDecimal.ZERO);

        }else {
            String attrValueStr = (String) event.getAttributeValueByName(aggregator.getAttributeName());
            added.setSum(new BigDecimal(attrValueStr));
        }

        // count +=1, sum += attribute value
        Value newValue = oldValue.add(added);

        Utils.getInstance().getSnapshotManager().getLastGraphletSnapshot().getValues().put(qid, newValue);

    }

}
