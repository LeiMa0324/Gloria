package gloriaExecutor;

import gloriaExecutor.snapshot.SnapshotManager;
import gloriaExecutor.snapshot.Value;
import gloriaExecutor.snapshot.ValueExpr;
import gloriaOptimizer.Workload;
import gloriaOptimizer.graph.OptimizerType;
import gloriaOptimizer.graph.node.Node;
import gloriaOptimizer.graph.node.Sset;
import gloriaOptimizer.graph.node.StartNode;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;
import base.Event;
import lombok.Data;
import lombok.NoArgsConstructor;
import query.aggregator.Aggregator;

import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;

@Data
public class Executor {

    protected long memory;
    protected long latency;
    protected Workload workload;
    protected HashMap<Integer, Value> finalValue;

    protected PredecessorManager predecessorManager;
    protected Aggregator aggregator;
    protected SnapshotManager snapshotManager;
    private boolean doesPrint = false;

    private long nonshareExecutionTime;
    private long shareExecutionTime;

    private int nonshareTimes;
    private int shareTimes;

    public Executor(Workload workload){
        this.workload = workload;
        this.finalValue = new HashMap<>();
        this.aggregator = this.workload.getQueries().get(0).getAggregator();
        this.snapshotManager = new SnapshotManager();
        this.snapshotManager.setAggregator(this.aggregator);
        this.predecessorManager = new PredecessorManager();
        this.nonshareExecutionTime = 0;
        this.shareExecutionTime = 0;

        this.nonshareTimes = 0;
        this.shareTimes = 0;
    }

    public void run(){

        for (Event event: this.workload.getSubstream()){


            Event lastE = this.predecessorManager.findLatestSameEventWithSamePredicate(event);

            for (Node p_node: searchSharingPlansOfIncomingEdge(event)){
                applyPNodeSharingPlan(event, p_node,  lastE);
            }

            for (Node s_node: searchSharingPlansOfOutgoingEdge(event)){
                applySNodeSharingPlan(event, s_node, lastE);
            }

//            System.out.println(event.toString());
            finalValueProcess(event);
            ArrayList<Event> events = this.predecessorManager.storedEvents.getOrDefault(event.getType().getName(), new ArrayList<>());
            events.add(event);
            this.predecessorManager.storedEvents.put(event.getType().getName(), events);

        }

        StringBuilder sb = new StringBuilder();
        sb.append(workload.toString());
        sb.append("============Final Value============");
        for (int qid: this.finalValue.keySet()){
            sb.append("\nqid :").append(qid).append(", value: \n    ").append(this.finalValue.get(qid).toString());
        }
        if (doesPrint) {
        System.out.println(sb.toString());
        }
    }


    private void applyPNodeSharingPlan(Event event, Node p_node, Event last_E){


        if (p_node!=null) {
            for (Sset sset : p_node.getSsets()) {
                //apply the sharing plan of the incoming edge
                SsetExecution(event, sset, p_node, last_E);
            }
        }
    }

    private void applySNodeSharingPlan(Event event, Node node, Event last_E) {

        if (node != null) {
        for (Sset sset : node.getSsets()) {
            if (sset.isShared() && isSharedSsetLocal(event, sset)&&event.overlappingSset(sset)) {
                this.snapshotManager.createLocalSnapshot(event, sset);
            }
        }
    }
    }

    private void SsetExecution(Event event, Sset sset, Node p_node, Event last_E){
        if (!sset.isShared()){
            nonShareExecution(event, sset,p_node,  last_E);
        }else {
            sharedExecution(event, sset, p_node,  last_E);
        }
    }


    private ArrayList<Node> searchSharingPlansOfIncomingEdge(Event event){
        ArrayList<Node> nodes = new ArrayList<>();

        //get the sharing plan of the the incoming edge of the event
        for (Edge edge:event.getType().getIncomingEdgesFromPred().values()){

            if (!this.workload.getPath().getNodeHashMap().containsKey(edge.toString())){
                for (StartNode startNode:this.workload.getGraph().getStartNodes()){
                    if (startNode.getEdge().getRightEventType().getName().equals(
                            event.getType().getName()
                    )){
                        nodes.add(startNode);
                    }
                }
            }else {
             nodes.add(this.workload.getPath().getNodeHashMap().get(edge.toString()));
            };
        }


        return nodes;
    }

    private ArrayList<Node> searchSharingPlansOfOutgoingEdge(Event event){
        ArrayList<Node> nodes = new ArrayList<>();

        //get the sharing plan of the the incoming edge of the event
        for (Edge edge:event.getType().getOutgoingEdgesToSuc().values()){
            nodes.add(this.workload.getPath().getNodeHashMap().get(edge.toString()));
        }

        return nodes;
    }

    /**
     * search for predecessors for each query in the S-set
     * @param event
     * @param sset
     */
    private void nonShareExecution(Event event, Sset sset, Node p_node,Event lastE) {

        //<qid, p_et>

        long start = System.currentTimeMillis();

        if (this.workload.getGraph().getType()== OptimizerType.NORMAL){
            for (int qid : sset.getQueries()) {

                Sset newSset = new Sset();
                newSset.getQueries().add(qid);

                ValueExpr expr = new ValueExpr(event, newSset, this.aggregator);
                EventType p_et = p_node.getEdge().getLeftEventType();

                if (p_et.isStart()){
                    expr.startEventInit(qid);
                }else {

                    expr.sum(lastE == null ? null : lastE.getValueExprForSset(sset));

                    ArrayList<Event> p_events = this.predecessorManager.findPredecessorsOfEventTypeAfterId(p_et, lastE==null?-1:lastE.getEventId());
                    expr.sumPredecessorsForSingletonQuery(p_events, qid);
                }

                if (!expr.isEmpty()) {
                    event.getSnapshotExpressions().put(newSset, expr);
                }
            }

        }else {

            for (int qid : sset.getQueries()) {

                Sset newSset = new Sset();
                newSset.getQueries().add(qid);

                ValueExpr expr = new ValueExpr(event, newSset, this.aggregator);

                EventType p_et = p_node.getEdge().getLeftEventType();

                if (p_et.isStart()){
                    expr.startEventInit(qid);
                }else {

                    ArrayList<Event> p_events = this.predecessorManager.findPredecessorsOfEventTypeAfterId(p_et, -1);
                    expr.sumPredecessorsForSingletonQuery(p_events, qid);
                }


                if (!expr.isEmpty()) {
                    event.getSnapshotExpressions().put(newSset, expr);
                }

            }
        }

        long end = System.currentTimeMillis();
        this.nonshareExecutionTime+=(end-start);
        this.nonshareTimes++;
    }


    /**
     * the event is shared by the s-set
     * @param event
     * @param sset
     */
    private void sharedExecution(Event event, Sset sset, Node p_node, Event last_E){

        long start = System.currentTimeMillis();

        ValueExpr expr = event.getValueExprForQueries(sset.getQueries())==null?
                new ValueExpr(event, sset, this.aggregator):event.getValueExprForSset(sset);

        if (event.getValueExprForQueries(sset.getQueries())==null) {
            expr.sum(last_E == null ? null : last_E.getValueExprForSset(sset));
        }

        //<qid, p_et>
        EventType p_et = p_node.getEdge().getLeftEventType();

        ArrayList<Event> predecessors = this.predecessorManager.findPredecessorsOfEventTypeAfterId(p_et, last_E == null ? -1 : last_E.getEventId());
        expr.sumPredecessorsForOwnSset(predecessors);


        // set the expression for this s-set
        if (!expr.isEmpty()&&event.getValueExprForQueries(sset.getQueries())==null) {
            event.getSnapshotExpressions().put(sset, expr);
        }
        long end = System.currentTimeMillis();

        this.shareExecutionTime +=(end-start);
        this.shareTimes++;
    }


    private boolean isSharedSsetLocal(Event event, Sset sset){
        return sset.getCheckpoints().get(0).getName().equals(event.getType().getName());
    }

    private void finalValueProcess(Event event){
        for (Edge edge: event.getType().getOutgoingEdgesToSuc().values()){
            if (edge.getRightEventType().isEnd()){
                for (int qid: edge.getEdgeQueries()){

                    Value value = this.snapshotManager.evaluateSnapshotExprsForQuery(event.getValueExprForQuery(qid), qid);
                    this.finalValue.put(qid, this.finalValue.getOrDefault(qid, new Value()).add(value));

                }
            }
        }
    }

}
