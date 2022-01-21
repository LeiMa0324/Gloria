package hamlet.base;

import hamlet.Graph.tools.PredicateManager;
import hamlet.Graph.tools.WindowManager;
import hamlet.query.Query;
import hamlet.query.aggregator.Aggregator;
import hamlet.query.predicate.SingleValuePredicate;
import hamlet.query.window.Window;
import hamlet.workload.Workload;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Template {
    private Workload workload;
    private Aggregator aggregator;
    private PredicateManager predicateManager;
    private WindowManager windowManager;
    // store the positions of event for each queries
    private HashMap<String, HashMap<PosType, ArrayList<Integer>>> positions;

    public enum PosType{
        START,
        END,
        STARTANDEND,
        OTHER,
    }


    public Template(Workload workload){
        this.workload = workload;
        this.aggregator = workload.getQueries().get(0).getAggregator();
        this.positions = new HashMap<>();


        ArrayList<Window> windows = new ArrayList<>();
        ArrayList<SingleValuePredicate> predicates = new ArrayList<>();



        for (int i=0; i<workload.getQueries().size(); i++){

            predicates.add((SingleValuePredicate)workload.getQueries().get(i).getPredicates().get(0));
            windows.add(workload.getQueries().get(i).getWindow());

            //set event types
            setEventTypes(i);
        }
        this.windowManager = new WindowManager(windows);
        this.predicateManager = new PredicateManager(predicates);

    }


    public EventType getNoneKleenePredecessorByEventTypeAndQueryId(EventType eventType, Integer qid){
        return workload.getQueries().get(qid).getPattern().getNoneKleenePredecessor(eventType);
    }


    public ArrayList<EventType> getAllPredecessorsByEventTypeAndQueryId(EventType eventType, Integer qid){
        return workload.getQueries().get(qid).getPattern().getAllPredecessors(eventType);
    }

    private void setEventTypes(int qid){
        Query query = this.workload.getQueries().get(qid);

        if (this.workload.getQueries().get(qid).getPattern().getEventTypes().size()==1){
            EventType et = this.workload.getQueries().get(qid).getPattern().getEventTypes().get(0);
            HashMap<PosType, ArrayList<Integer>> posOfEt = this.positions.getOrDefault(et.getName(), new HashMap<>());
            ArrayList<Integer> queries = posOfEt.getOrDefault(PosType.STARTANDEND, new ArrayList<>());
            queries.add(qid);

            posOfEt.put(PosType.STARTANDEND, queries);
            this.positions.put(et.getName(),  posOfEt);

        }else {
            int endIndex = this.workload.getQueries().get(qid).getPattern().getEventTypes().size()-1;

            for (int i=0;i< this.workload.getQueries().get(qid).getPattern().getEventTypes().size(); i++){

                EventType et = this.workload.getQueries().get(qid).getPattern().getEventTypes().get(i);
                HashMap<PosType, ArrayList<Integer>> posOfEt = this.positions.getOrDefault(et.getName(), new HashMap<>());


                if (i ==0 ){
                    ArrayList<Integer> queries = posOfEt.getOrDefault(PosType.START, new ArrayList<>());
                    queries.add(qid);
                    posOfEt.put(PosType.START, queries);
                }
                if (i == endIndex){
                    ArrayList<Integer> queries = posOfEt.getOrDefault(PosType.END, new ArrayList<>());
                    queries.add(qid);
                    posOfEt.put(PosType.END, queries);
                }
                if (i!=0 && i!=endIndex){
                    ArrayList<Integer> queries = posOfEt.getOrDefault(PosType.OTHER, new ArrayList<>());
                    queries.add(qid);
                    posOfEt.put(PosType.OTHER, queries);
                }

                this.positions.put(et.getName(), posOfEt);

            }
        }
    }

    public ArrayList<Integer> getQueriesStartWithEventType(EventType eventType){
        return this.positions.get(eventType.getName()).get(PosType.START);
    }

    public ArrayList<Integer> getQueriesEndWithEventType(EventType eventType){
        return this.positions.get(eventType.getName()).get(PosType.END);
    }

    public ArrayList<Integer> getQueriesOfEventType(EventType eventType){
        ArrayList<Integer> queries = new ArrayList<>();
        for (PosType type: this.positions.get(eventType.getName()).keySet()){
            queries.addAll(this.positions.get(eventType.getName()).get(type));
        }

        return queries;
    }
}

