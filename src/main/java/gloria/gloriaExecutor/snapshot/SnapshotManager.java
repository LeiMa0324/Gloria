package gloria.gloriaExecutor.snapshot;

import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.base.Event;
import lombok.Data;
import gloria.query.aggregator.Aggregator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SnapshotManager {
    private ArrayList<Snapshot> snapshots;
    private Aggregator aggregator;
    private long snapShotInsertionTime =0;

    public SnapshotManager(){
        this.snapshots = new ArrayList<>();
    }


    /**
     * evaluate the count expr for a gloria.query
     * @param expr expression
     * @param qid gloria.query id
     * @return the evaluated value
     */
    public Value evaluateSnapshotExprsForQuery(ValueExpr expr, int qid){
        if (expr==null){
            return new Value();
        }

        Value value = new Value();
        BigInteger count = BigInteger.ZERO;
        BigDecimal sum = BigDecimal.ZERO;

        assert expr.getSnapIdToCountCoeffs().keySet().equals(expr.getSnapIdToSumCoeffs().keySet());

        if (!expr.getSnapIdToCountCoeffs().isEmpty()) {
            for (Integer snapId : expr.getSnapIdToCountCoeffs().keySet()) {
                count = count.add(this.snapshots.get(snapId).getValueForQuery(qid).getCount().
                        multiply(expr.getSnapIdToCountCoeffs().get(snapId)));
                if (expr.getSnapIdToSumCoeffs().containsKey(snapId)) {
                    sum = sum.add(this.snapshots.get(snapId).getValueForQuery(qid).getSum().
                            multiply(expr.getSnapIdToSumCoeffs().get(snapId)));
                }
            }
        }

        if (!expr.getValuesPart().isEmpty()){

            count =expr.getValuesPart().containsKey(qid)?count.add(expr.getValuesPart().get(qid).getCount()):BigInteger.ZERO;
            sum = expr.getValuesPart().containsKey(qid)?sum.add(expr.getValuesPart().get(qid).getSum()):BigDecimal.ZERO;
        }


        value.setCount(count);
        value.setSum(sum);
        return value;
    }



    /**
     * evaluate the expr for a s-set
     * @param expr expression
     * @param sset S-set
     * @return the evaluated value
     */
    public HashMap<Integer, Value> evaluateSnapshotExprForSset(ValueExpr expr, Sset sset){
        HashMap<Integer, Value> values=new HashMap<>();
        for (int qid: sset.getQueries()){
            values.put(qid, evaluateSnapshotExprsForQuery(expr, qid));
        }

        return values;
    }

    public void createLocalSnapshot(Event event, Sset sset){

        Snapshot snapshot = new Snapshot();
        snapshot.setEvent(event);
        HashMap<Integer, Value> qidToValue = new HashMap<>();
        //(1, 2)->(1, 2)
        if (event.getValueExprForSset(sset)!=null) {
            qidToValue = evaluateSnapshotExprForSset(event.getValueExprForSset(sset), sset);
            snapshot.setQidToValue(qidToValue);
        }else {
            //(1)(2) ->(1,2)
            for (int qid: sset.getQueries()){
                Sset sset1 = new Sset();
                sset1.getQueries().add(qid);
                Value value = evaluateSnapshotExprsForQuery(event.getValueExprForSset(sset1), qid);

                long start = System.currentTimeMillis();
                qidToValue.put(qid, value);
                long end = System.currentTimeMillis();
                this.snapShotInsertionTime+=(end-start);

            }
            snapshot.setQidToValue(qidToValue);
        }
        snapshot.setId(this.snapshots.size());

        event.resetExprForSset(sset, snapshot);
        event.getValueExprForSset(sset).setAggregator(this.aggregator);

        long start = System.currentTimeMillis();
        this.snapshots.add(snapshot);
        long end = System.currentTimeMillis();
        this.snapShotInsertionTime+=(end-start);

    }
}
