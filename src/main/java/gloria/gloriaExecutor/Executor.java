package gloria.gloriaExecutor;

import gloria.gloriaExecutor.snapshot.SnapshotManager;
import gloria.gloriaExecutor.snapshot.Value;
import gloria.gloriaExecutor.snapshot.ValueExpr;
import gloria.gloriaOptimizer.Workload;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import gloria.gloriaOptimizer.graph.node.StartNode;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.template.EventType.EventType;
import gloria.base.Event;
import hamlet.Graph.tools.Utils;
import lombok.Data;
import gloria.query.aggregator.Aggregator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

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
    private long firtSharingHalf;

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


        long start = System.currentTimeMillis();

        for (Event event: this.workload.getSubstream()){

            long s1 = System.currentTimeMillis();
            Event lastE = this.predecessorManager.findLatestSameEventWithSamePredicate(event);
            long s2 = System.currentTimeMillis();

            for (Node p_node: searchSharingPlansOfIncomingEdge(event)){
                applyPNodeSharingPlan(event, p_node,  lastE);
            }
            long s3 = System.currentTimeMillis();


            for (Node s_node: searchSharingPlansOfOutgoingEdge(event)){
                applySNodeSharingPlan(event, s_node, lastE);
            }

            long s4 = System.currentTimeMillis();


            finalValueProcess(event);
            ArrayList<Event> events = this.predecessorManager.storedEvents.getOrDefault(event.getType().getName(), new ArrayList<>());
            events.add(event);
            this.predecessorManager.storedEvents.put(event.getType().getName(), events);
            long s5 = System.currentTimeMillis();


        }
        long end = System.currentTimeMillis();
        this.latency = end-start;
//        System.out.println("=====================");
//        System.out.println("last e search time:"+lastEtime);
//        System.out.println("sharingP_Plantime time:"+sharingP_Plantime);
//        System.out.println("sharingS_Plantime time:"+sharingS_Plantime);
//        System.out.println("finishingTime time:"+finishingTime);
//
//        System.out.println("shared execution time:"+this.shareExecutionTime);
//        System.out.println("first Sharing half:"+this.firtSharingHalf);

//        StringBuilder sb = new StringBuilder();
//        sb.append(workload.toString());
//        sb.append("============Final Value============");
//        for (int qid: this.finalValue.keySet()){
//            sb.append("\nqid :").append(qid).append(", value: \n    ").append(this.finalValue.get(qid).toString());
//        }
//        if (doesPrint) {
//            System.out.println(sb.toString());
//        }

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
     * search for predecessors for each gloria.query in the S-set
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

                    boolean isSelfKleene = event.getType().isSelfKleene();
                    expr.sum(lastE == null ? null : lastE.getValueExprForSset(sset));

                    if (isSelfKleene){
                        //if E+, lastE's value should be doubled
                        expr.sum(lastE == null ? null : lastE.getValueExprForSset(sset));
                    }

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
        ValueExpr expr = event.getValueExprForSset(sset)==null?new ValueExpr(event, sset, this.aggregator):event.getValueExprForSset(sset);

        ArrayList<Integer> startQueries = event.getType().getStartQueries();

        //increment for start

        if (startQueries!=null) {
            for (int qid: startQueries){
                Value value = expr.getValuesPart().get(qid)==null?new Value():expr.getValuesPart().get(qid);
                value = value.add(Value.ONE);
                expr.getValuesPart().put(qid, value);
            }
        }

        if (last_E!=null){
            ValueExpr lastE_expr = last_E.getValueExprForSset(sset);
            if (lastE_expr!=null){
                expr.sum(lastE_expr);
            }
        }
        long quarter = System.currentTimeMillis();

        //if E+, lastE's value should be doubled
        boolean isSelfKleene = event.getType().isSelfKleene();

        if (isSelfKleene&&!expr.getSnapIdToCountCoeffs().isEmpty()){
            //if E+, lastE's value should be doubled
            for (Integer snapId: expr.getSnapIdToCountCoeffs().keySet()){
                BigInteger newCoeff = expr.getSnapIdToCountCoeffs().get(snapId).multiply(new BigInteger("2"));
                expr.getSnapIdToCountCoeffs().put(snapId, newCoeff);
            }
        }


        long middle = System.currentTimeMillis();

        //<qid, p_et>
        EventType p_et = p_node.getEdge().getLeftEventType();

        //exclude the E+ case
        if (!p_et.getName().equals(event.getType().getName())){
            ArrayList<Event> predecessors = this.predecessorManager.findPredecessorsOfEventTypeAfterId(p_et, last_E == null ? -1 : last_E.getEventId());
            expr.sumPredecessorsForOwnSset(predecessors);
        }

        // set the expression for this s-set
        if (!expr.isEmpty()) {
            event.getSnapshotExpressions().put(sset, expr);
            //if the create a snapshot for e
            if (sset.getCheckpoints().get(0).getName().equals(event.getType().getName())){
                    this.snapshotManager.createLocalSnapshot(event, sset);

            }

        }
        long end = System.currentTimeMillis();

        this.shareExecutionTime +=(end-start);
        this.shareTimes++;
        this.firtSharingHalf +=(end-middle);

    }


    private boolean isSharedSsetLocal(Event event, Sset sset){
        return sset.getCheckpoints().get(0).getName().equals(event.getType().getName());
    }

    private void finalValueProcess(Event event){
        ArrayList<Integer> endQueries = event.getType().getEndQueries();

        if (!endQueries.isEmpty()){
            for (int qid: endQueries){
                Value value = this.snapshotManager.evaluateSnapshotExprsForQuery(event.getValueExprForQuery(qid), qid);
                this.finalValue.put(qid, this.finalValue.getOrDefault(qid, new Value()).add(value));

            }

        }

    }

}
