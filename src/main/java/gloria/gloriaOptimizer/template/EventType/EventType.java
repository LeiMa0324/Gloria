package gloria.gloriaOptimizer.template.EventType;

import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.base.DatasetSchema;
import lombok.Data;
import gloria.gloriaOptimizer.template.Edge;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * an event type
 * contains the event type name, a list of attributes
 */
@Data
public abstract class EventType {

    protected String name;
    protected DatasetSchema schema;
    protected ArrayList<Integer> queries;
    protected boolean isStart=false;
    protected boolean isEnd = false;

    protected HashMap<String, Edge> incomingEdgesFromPred;
    protected HashMap<String, Edge> outgoingEdgesToSuc;
    protected ArrayList<Integer> endQueries;
    protected ArrayList<Integer> startQueries;

    private Integer frequency;

    public EventType(){
        this.queries = new ArrayList<>();
        this.incomingEdgesFromPred = new HashMap<>();
        this.outgoingEdgesToSuc = new HashMap<>();
        this.endQueries = new ArrayList<>();
        this.startQueries = new ArrayList<>();
    }


    public EventType(DatasetSchema schema){
        this.queries = new ArrayList<>();
        this.incomingEdgesFromPred = new HashMap<>();
        this.outgoingEdgesToSuc = new HashMap<>();
        this.schema = schema;
        this.endQueries = new ArrayList<>();
        this.startQueries = new ArrayList<>();
    }

    public Edge getIncomingKleeneEdge(){
        for (Edge edge: this.incomingEdgesFromPred.values()){
            if (edge.isKleene()){
                return edge;
            }
        }

        return null;
    }

    public Edge getOutgoingKleeneEdge(){
        for (Edge edge: this.outgoingEdgesToSuc.values()){
            if (edge.isKleene()){
                return edge;
            }
        }

        return null;
    }

    public ArrayList<Edge> getIncomingSEQEdges(){
        ArrayList<Edge> SEQEdges = new ArrayList<>();
        this.getIncomingEdgesFromPred().values().forEach(e -> {
            if (!e.isKleene()){
                SEQEdges.add(e);
            }
        });

        return SEQEdges;
    }

    public ArrayList<Edge> getOutGoingSEQEdges(){
        ArrayList<Edge> SEQEdges = new ArrayList<>();
        this.getOutgoingEdgesToSuc().values().forEach(e -> {
            if (!e.isKleene()){
                SEQEdges.add(e);
            }
        });

        return SEQEdges;
    }

    public boolean isSelfKleene(){
        for (Edge edge: this.incomingEdgesFromPred.values()){
            if (edge.isKleene()&&edge.getLeftEventType().getName().equals(this.getName())){
                return true;
            }
        }

        return false;
    }


    public Edge getIncomingEdgeToPred(String predName){
        return this.incomingEdgesFromPred.get(predName);
    }

    public Edge getOutgoingEdgeToSuc(String sucName){
        return this.outgoingEdgesToSuc.get(sucName);
    }

    public void addSEQPredecessor(EventType pred, int qid){

        //find the incoming edge
        Edge edge = getIncomingEdgeToPred(pred.getName())==null?new Edge():getIncomingEdgeToPred(pred.getName());
        edge.connect(pred, this, qid);

        //add the qid
        this.addQuery(qid);
        pred.addQuery(qid);

        //maintain this, pred edges
        this.incomingEdgesFromPred.put(pred.getName(), edge);
        pred.outgoingEdgesToSuc.put(this.getName(), edge);
    }

    public void addSEQSuccessor(EventType suc, int qid){

        //find the outgoing edge
        Edge edge = getOutgoingEdgeToSuc(suc.getName())==null?new Edge():getOutgoingEdgeToSuc(suc.getName());
        edge.connect(this, suc, qid);

        //add the qid
        this.addQuery(qid);
        suc.addQuery(qid);

        //maintain this, suc edges
        this.outgoingEdgesToSuc.put(suc.getName(), edge);
        suc.incomingEdgesFromPred.put(this.getName(), edge);
    }

    public void addKleeneSuccessor(EventType suc, int qid){
        if (suc==null) return;
        //find the kleene edge
        Edge kEdge = this.outgoingEdgesToSuc.get(suc.getName())==null||(!this.outgoingEdgesToSuc.get(suc.getName()).isKleene())?
                new Edge():this.outgoingEdgesToSuc.get(suc.getName());
        kEdge.setKleene(true);
        kEdge.connect(this, suc, qid);

        //add the qid
        this.addQuery(qid);
        suc.addQuery(qid);

        //maintain this, suc edges
        this.outgoingEdgesToSuc.put(suc.getName(), kEdge);
        suc.incomingEdgesFromPred.put(this.getName(), kEdge);
    }


    private void addQuery(int qid){
        if (!this.queries.contains(qid)){
            this.queries.add(qid);
        }
    }

    public HashMap<Integer, EventType> getMultiPrecedingEventTypesForSset(Sset sset){
        HashMap<Integer, EventType> qidToPredET = new HashMap<>();
        for (int qid: sset.getQueries()){
            EventType p_et = getPrecedingEventTypeForQuery(qid);
            if (p_et!=null){
                qidToPredET.put(qid, p_et);
            }
        }
        return qidToPredET;
    }

    public ArrayList< EventType> getMultiPrecedingEventTypesWithKleenesForSset(Sset sset){
        ArrayList< EventType>  predETs = new ArrayList<>();

            for (Edge edge: this.incomingEdgesFromPred.values()){
                if (edge.getEdgeQueries().containsAll(sset.getQueries())){
                    predETs.add(edge.getLeftEventType());
                }
            }


        return predETs;
    }


    public EventType getSinglePrecedingEventTypesForSset(Sset sset){
        EventType p_et = getPrecedingEventTypeForQuery(sset.getQueries().get(0));
        if (this.getIncomingEdgesFromPred().get(p_et.getName()).getEdgeQueries().containsAll(sset.getQueries())){
            return p_et;
        }else {
            System.out.println("EventType:getSinglePrecedingEventTypesForSset");
            return null;
        }

    }


    public EventType getPrecedingEventTypeForQuery(int qid){

        for (Edge edge: this.getIncomingEdgesFromPred().values()){
            if (edge.getEdgeQueries().contains(qid)){
                return edge.getLeftEventType();
            }
        }
        return null;
    }

    @Override
    public String toString(){
        return this.name;
    }

}
