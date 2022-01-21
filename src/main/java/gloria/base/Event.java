package gloria.base;

import gloria.gloriaExecutor.snapshot.Snapshot;
import gloria.gloriaExecutor.snapshot.ValueExpr;
import hamlet.query.aggregator.Value;
import lombok.Data;
import gloria.gloriaOptimizer.template.EventType.EventType;
import gloria.gloriaOptimizer.graph.node.Sset;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * the event class
 */
@Data
public class Event {

    private EventType type;
    private Object[] tuple;
    private long timeStamp;
    private ArrayList<Integer> validQueries;

    //<qids, expr>
    private HashMap<Sset, ValueExpr> snapshotExpressions;

    //todo: a fast way to find expressions by query
    private HashMap<Integer, Sset> queryToSset;
    private Metric metric;
    private Integer eventId;

    public Event(EventType type, Object[] tuple) throws ParseException {

        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMddhhmm");//2016-08-10 20:40

        this.type = type;
        this.tuple = tuple;
//        this.timeStamp = simpleFormat.parse((String)tuple[1]).getTime();
        this.validQueries = new ArrayList<>();
        this.snapshotExpressions = new HashMap<>();
        this.queryToSset = new HashMap<>();

    }

    /**
     * find the index of an attribute
     * @param attributeName the name of an attribute
     * @return the index
     */
    private int findAttributeIndexByName(String attributeName){
        for (int i=0; i< type.getSchema().getAttributes().size(); i++){
            if (type.getSchema().getAttributes().get(i).getName().equals(attributeName)){
                return i;
            }
        }
        return  -1;
    }

    /**
     * get the value of a given attribute
     * @param attributeName a given attribute
     * @return the value
     */
    public Object getAttributeValueByName(String attributeName){
        int index = this.findAttributeIndexByName(attributeName);

        return index == -1? null: this.tuple[index];

    }


    public enum Metric{
        SNAPSHOT,
        VAL
    }

    public ValueExpr getValueExprForQuery(int qid){
        for (Sset sset: this.snapshotExpressions.keySet()){
            if (sset.getQueries().contains(qid)){
                return this.snapshotExpressions.get(sset);
            }
        }
        return null;
    }

    public ValueExpr getValueExprForQueries(ArrayList<Integer> qids){
        for (Sset sset: this.snapshotExpressions.keySet()){
            if (sset.getQueries().containsAll(qids)){
                return this.snapshotExpressions.get(sset);
            }
        }
        return null;
    }

    public ValueExpr getValueExprForSset(Sset sset){
//        for (Sset sset1: this.snapshotExpressions.keySet()){
//            if (sset1.getQueries().containsAll(sset.getQueries())){
                return this.snapshotExpressions.get(sset);
//            }
//        }
//        return null;
    }

    public boolean overlappingSset(Sset sset){
        for (int qid: sset.getQueries()){
            for (Sset thisSset: this.snapshotExpressions.keySet()){
                if (thisSset.getQueries().contains(qid)){
                    return true;
                }
            }
        }

        return false;
    }

    public void resetExprForSset(Sset sset, Snapshot snapshot){
        ValueExpr expr = new ValueExpr();
        expr.setSset(sset);
        expr.setEvent(this);


        expr.getSnapIdToCountCoeffs().put(snapshot.getId(), BigInteger.ONE);
        expr.getSnapIdToSumCoeffs().put(snapshot.getId(), BigDecimal.ONE);

        HashMap<Sset, ValueExpr> copyExpressions = new HashMap<>();

        for (Sset sset1: this.snapshotExpressions.keySet()){
            ArrayList<Integer> queries = new ArrayList<>(sset1.getQueries());
            ValueExpr valueExpr= this.snapshotExpressions.get(sset1);

            for (int i=sset1.getQueries().size()-1;i>=0;i-- ){

                if (sset.getQueries().contains(sset1.getQueries().get(i))){
                    queries.remove(i);
                }

            }

            Sset sset2 = new Sset();
            sset2.setQueries(queries);
            sset2.setShared(sset1.isShared());
            sset2.setEdge(sset.getEdge());

            if (!queries.isEmpty()){
                copyExpressions.put(sset2, valueExpr);
            }
        }

        this.snapshotExpressions = copyExpressions;
        this.snapshotExpressions.put(sset, expr);
        for (int qid: sset.getQueries()){
            this.queryToSset.put(qid, sset);
        }

    }


    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\nEvent: "+ this.type.getName()+" ,id:"+this.eventId+"\n");
        for (Sset sset: this.snapshotExpressions.keySet()){
            builder.append(sset.getQueries().toString()+"|"+this.snapshotExpressions.get(sset).toString());
        }
        return builder.toString();
    }
}
