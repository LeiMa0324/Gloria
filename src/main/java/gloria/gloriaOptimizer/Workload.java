package gloria.gloriaOptimizer;


import gloria.gloriaExecutor.PredicateManager;
import gloria.gloriaOptimizer.graph.Graph;
import gloria.gloriaOptimizer.graph.GraphConstructor;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.pathSearcher.Path;
import gloria.gloriaOptimizer.graph.pathSearcher.PathSearcher;
import gloria.gloriaOptimizer.template.Template;
import gloria.base.DatasetSchema;
import gloria.base.Event;
import lombok.Data;
import gloria.query.Query;

import java.util.ArrayList;

@Data
/**
 * generate the gloria.workload
 */
public class Workload {

    private ArrayList<Query> queries;
    private DatasetSchema schema;
    private Path path;
    private Template template;
    private ArrayList<Event> substream;
    private PredicateManager predicateManager;
    private Double miniCost;
    private PathSearcher searcher;
    private long memory;

    private Graph graph;

    /**
     * the default constructor
     */
    public Workload(DatasetSchema schema){
        this.queries = new ArrayList<>();
        this.schema = schema;

        this.substream = new ArrayList<>();
        this.predicateManager = new PredicateManager();
    }

    public Workload(DatasetSchema schema, ArrayList<Query> queries){
        this.queries = queries;
        this.schema = schema;

        this.substream = new ArrayList<>();
        this.predicateManager = new PredicateManager();

    }

    /**
     * transform the gloria.workload into template, optimize and generate the sharing plan
     */
    public void optimize(OptimizerType type){
        Template template = new Template();
        template.loadFromWorkload(this);
        this.template = template;
        GraphConstructor constructor = new GraphConstructor(template);
        constructor.setType(type);
        Graph graph = constructor.constructGraph();
        this.graph = graph;
        this.searcher = new PathSearcher();
        this.setPath(searcher.pathSearch(graph));
        this.miniCost = searcher.getMinCost();
        graph.computeMemory();
        this.memory = graph.getMemory()+searcher.getMemory();

    }




    /**
     * add a gloria.query into the gloria.workload
     * @param query a given gloria.query
     */
    public void addQuery(Query query){
        query.setId(this.queries.size());
        this.queries.add(query);
        this.predicateManager.getPredicates().addAll(query.getPredicates());
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n***********Workload Start***********\n");
        builder.append("Workload Info:\n");
        builder.append("Query Num:").append(this.getQueries().size()).append("\n");
        for (Query q: this.getQueries()){
            builder.append(q.getPattern().getPatternString()).append("\n");
        }
        builder.append("\n Optimal Sharing Plan:\n");
        builder.append("\n Mini Cost:").append(this.miniCost);
        builder.append("\n Edge Num:").append(this.path.getNodeHashMap().values().size()).append("\n");

        for (Node node: path.getNodeHashMap().values()){
            if (!node.isStartNode()) {
                builder.append(node.sharingPlanInfo());
            }

        }

        builder.append("\n***********Workload End***********\n");

        return builder.toString();
    }

}

