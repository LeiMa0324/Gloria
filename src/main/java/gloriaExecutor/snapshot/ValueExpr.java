package gloriaExecutor.snapshot;

import base.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import gloriaOptimizer.graph.node.Sset;
import org.apache.commons.collections4.CollectionUtils;
import query.aggregator.Aggregator;

import javax.print.DocFlavor;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
public class ValueExpr {
    private Sset sset;
    private Event event;
    private Aggregator aggregator;
    //<snapshot_id, count_coeff> expr part
    private HashMap<Integer,BigInteger> snapIdToCountCoeffs;

    //<snapshot_id, sum_coeff> expr part
    private HashMap<Integer,BigDecimal> snapIdToSumCoeffs;

    //<qid, value> value part(count, sum)
    private HashMap<Integer, Value> valuesPart;


    public ValueExpr(){
        this.snapIdToCountCoeffs = new HashMap<>();
        this.snapIdToSumCoeffs = new HashMap<>();
        this.valuesPart = new HashMap<>();
    }

    public ValueExpr(Event event, Sset sset, Aggregator aggregator){
        new ValueExpr();
        this.event = event;
        this.sset = sset;
        this.aggregator = aggregator;
        this.snapIdToCountCoeffs = new HashMap<>();
        this.snapIdToSumCoeffs = new HashMap<>();
        this.valuesPart = new HashMap<>();

    }

    public ValueExpr(Sset sset, HashMap<Integer, BigInteger> snapCoeffs){
        new ValueExpr();
        this.snapIdToCountCoeffs = new HashMap<>();
        this.snapIdToCountCoeffs = snapCoeffs;
        this.sset = sset;
        this.valuesPart = new HashMap<>();
    }

    public void oneForQuery(int qid){
        this.valuesPart.put(qid, Value.ONE);
    }

    public int sumValueAndExprPartForCount(ValueExpr expr){
        if (expr==null){
            return -1;
        }
        if (!(this.valuesPart.isEmpty()&&expr.getValuesPart().isEmpty())){
            for (int qid: this.sset.getQueries()){
                Value thisValue = this.valuesPart.getOrDefault(qid, new Value());
                Value thatValue = expr.getValuesPart().getOrDefault(qid, new Value());
                thisValue.setCount(thisValue.getCount().add(thatValue.getCount()));
                if (!thisValue.equalsZero()){
                    this.valuesPart.put(qid, thisValue);
                }
            }
            return 0;
        }

        return -1;
    }

    public void sum(ValueExpr expr){
        //summing the value part, both count and sum
        sumValuePartWithCountAndSum(expr);


        //summing the expr part, both count and sum
        sumExprPartWithCountAndSum(expr);


    }

    public void startEventInit(int qid){
        this.valuesPart.put(qid, Value.ONE);
    }

    /**
     * sum the value part with another expression
     * @param expr the given expr
     */
    private int sumValuePartWithCountAndSum(ValueExpr expr){
        if (expr==null){
            return -1;
        }

        if (!(this.valuesPart.isEmpty()&&expr.getValuesPart().isEmpty())){
            for (int qid: this.sset.getQueries()){
                Value thisValue = this.valuesPart.getOrDefault(qid, new Value());
                Value thatValue = expr.getValuesPart().getOrDefault(qid, new Value());
                Value sum = thisValue.add(thatValue);
                if (!sum.equalsZero()){
                    this.valuesPart.put(qid, sum);
                }
            }
            return 0;
        }

        return -1;
    }
    /**
     * sum the expr part with another expression
     * @param expr
     */
    private int sumExprPartWithCountAndSum(ValueExpr expr){
        if (expr==null){
            return -1;
        }

        if (!(this.snapIdToCountCoeffs.isEmpty()&&expr.snapIdToCountCoeffs.isEmpty())){
            boolean doesAggregate = isSumAggregateEvent();

            for (int s_id: CollectionUtils.union(this.snapIdToCountCoeffs.keySet(), expr.snapIdToCountCoeffs.keySet())){
                //compute count coeffs
                computeCountCoeffs(s_id, expr);

                if (this.aggregator.getFunc()== Aggregator.Aggregfunctions.AVG||
                        this.aggregator.getFunc()== Aggregator.Aggregfunctions.SUM) {
                    //compute sum coeffs
                    computeSumCoeffs(s_id, expr, doesAggregate);
                }

            }

            return 0;
        }

        return -1;
    }

    private void computeCountCoeffs(int s_id, ValueExpr expr){

        BigInteger thisCountCoeff= this.snapIdToCountCoeffs.getOrDefault(s_id, BigInteger.ZERO);
        BigInteger thatCountCoeff= expr.snapIdToCountCoeffs.getOrDefault(s_id, BigInteger.ZERO);
        BigInteger sumCountCoeff = thisCountCoeff.add(thatCountCoeff);

        if (!sumCountCoeff.equals(BigInteger.ZERO)){
            this.snapIdToCountCoeffs.put(s_id, sumCountCoeff);
        }

    }

    private void computeSumCoeffs(int s_id, ValueExpr expr, boolean doesAggregate){

        BigDecimal sumCoeff = BigDecimal.ZERO;

        //not a aggregate event, sum coeff = sigma(p.sumCoeff)
        if (!doesAggregate) {
            BigDecimal thisSumCoeff = this.snapIdToSumCoeffs.getOrDefault(s_id, BigDecimal.ZERO);
            BigDecimal thatSumCoeff = expr.snapIdToSumCoeffs.getOrDefault(s_id, BigDecimal.ZERO);
            sumCoeff = thisSumCoeff.add(thatSumCoeff);
        }else {
            //an aggregate event, sum coeff = attrValue*countCoeff
            sumCoeff = new BigDecimal(this.snapIdToCountCoeffs.get(s_id)).multiply(
                    new BigDecimal((String) this.event.getAttributeValueByName(this.aggregator.getAttributeName()))
            );
        }

        if (!sumCoeff.equals(BigDecimal.ZERO)){
            this.snapIdToSumCoeffs.put(s_id, sumCoeff);
        }

    }

    public int sumPredecessorsForSingletonQuery(ArrayList<Event> p_events, int qid){
        if (p_events==null||p_events.isEmpty()){
            return -1;
        }

        for (Event p_event: p_events){
            //todo: commented
            this.sum(p_event.getValueExprForQuery(qid));
        }
        if (isSumAggregateEvent()){

            this.valuesPart.get(qid).setSum(new BigDecimal((String) this.event.getAttributeValueByName(this.aggregator.getAttributeName()))
                    .multiply(new BigDecimal(this.valuesPart.get(qid).getCount())));
        }

        return 0;
    }

    /**\
     * assume the s-set has the same set of predecessors
     * @param p_events the predecessors of the s-set

     */
    public int sumPredecessorsForOwnSset(ArrayList<Event> p_events){
        if (p_events ==null||p_events.isEmpty()){
            return -1;
        }
        for (Event p_event: p_events){
            if (p_event.getSnapshotExpressions().isEmpty()){
                continue;
            }

            if (p_event.getValueExprForSset(this.sset)!=null){
                this.sum(p_event.getValueExprForSset(this.sset));
            }else {
                ArrayList<Integer> queriesOfThisSset = new ArrayList<>(this.sset.getQueries());
                for (Sset p_sset: p_event.getSnapshotExpressions().keySet()){
                    for (Integer qid: p_sset.getQueries()){
                        if (queriesOfThisSset.contains(qid)){
                            this.sum(p_event.getValueExprForSset(p_sset));
                            queriesOfThisSset.removeAll(p_sset.getQueries());
                        }
                    }
                }
            }

        }
        return 0;
    }


    private boolean isSumAggregateEvent(){
        return (aggregator.getFunc()== Aggregator.Aggregfunctions.AVG||aggregator.getFunc()==Aggregator.Aggregfunctions.SUM
        )&&this.event.getType().getName().equals(aggregator.getEventTypeName());
    }

    public boolean isEmpty(){

        if (this.snapIdToCountCoeffs.isEmpty()&&this.snapIdToSumCoeffs.isEmpty()&&this.valuesPart.isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("expr part: ").append(this.snapIdToCountCoeffs);
        sb.append("\nvalue part: ").append(this.valuesPart).append("\n");
        return sb.toString();
    }
}
