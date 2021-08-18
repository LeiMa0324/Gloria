package experiment;

import base.DatasetSchema;
import gloriaOptimizer.Workload;
import gloriaOptimizer.graph.Graph;
import gloriaOptimizer.graph.GraphConstructor;
import gloriaOptimizer.graph.KleeneSubGraph.FullySharedKleeneGraph;
import gloriaOptimizer.graph.OptimizerType;
import gloriaOptimizer.graph.pathSearcher.PathSearcher;
import gloriaOptimizer.template.CycleFinder;
import gloriaOptimizer.template.Template;
import query.Query;
import query.pattern.Pattern;
import workload.WorkloadAnalyzer;

public class greedyOptimizerTest {
    public static void main(String[] args){
//
        String workloadPath = "src/main/resources/bus/MixWorkload/workload_100.txt";

        System.out.println("===============NOT SHARE optimizer========");
        WorkloadAnalyzer analyzer1 = new WorkloadAnalyzer(new DatasetSchema("bus"));
        analyzer1.setType(OptimizerType.GREEDY);
        analyzer1.analyzeAndOptimize(workloadPath);

//        System.out.println("===============Normal optimizer========");
//        WorkloadAnalyzer analyzer = new WorkloadAnalyzer(new DatasetSchema("stock"));
//        analyzer.setType(OptimizerType.NORMAL);
//        analyzer.analyzeAndOptimize(workloadPath);





//
//        DatasetSchema schema = new DatasetSchema("stock");
//        Workload workload = new Workload(schema);
//
//        Pattern p1 = new Pattern("SEQ(A, B, C, D, E)");
//        Query q1 = new Query(p1);
//        q1.setId(1);
//
//        Pattern p2 = new Pattern("SEQ(A, B, C, D, F)");
//        Query q2 = new Query(p2);
//        q2.setId(2);
//
//        Pattern p3 = new Pattern("SEQ(A, B, C, D, G)");
//        Query q3 = new Query(p3);
//        q3.setId(3);
//
//        Pattern p4 = new Pattern("SEQ(A, B, C, D, H)");
//        Query q4 = new Query(p4);
//        q4.setId(4);
//
//        workload.addQuery(q1);
//        workload.addQuery(q2);
//        workload.addQuery(q3);
//        workload.addQuery(q4);
//
//        Template template = new Template();
//        template.loadFromWorkload(workload);
//
//        GraphConstructor constructor = new GraphConstructor(template);
//        constructor.setType(OptimizerType.NORMAL);
//        Graph graph =  constructor.constructGraph();
//
//
//        PathSearcher searcher = new PathSearcher();
//        searcher.pathSearch(graph);
//        System.out.println(searcher.toString());



//        DatasetSchema schema = new DatasetSchema("stock");
//        Workload workload = new Workload(schema);
//
//        Pattern p1 = new Pattern("SEQ((A, (B, C)+, D)+, E)");
//        Query q1 = new Query(p1);
//        q1.setId(1);
//
//        Pattern p2 = new Pattern("SEQ((A, (B, C)+, D)+, F)");
//        Query q2 = new Query(p2);
//        q2.setId(2);
//
//        Pattern p3 = new Pattern("SEQ((A, (B, C)+, D)+, G)");
//        Query q3 = new Query(p3);
//        q3.setId(3);
//
//        Pattern p4 = new Pattern("SEQ((A, (B, C)+, D)+, H)");
//        Query q4 = new Query(p4);
//        q4.setId(4);
//
//        workload.addQuery(q1);
//        workload.addQuery(q2);
//        workload.addQuery(q3);
//        workload.addQuery(q4);
//
//        Template template = new Template();
//        template.loadFromWorkload(workload);
//
////
////        GraphConstructor constructor = new GraphConstructor(template);
////        constructor.setType(OptimizerType.NORMAL);
//        FullySharedKleeneGraph kleeneGraph = new FullySharedKleeneGraph();
//        kleeneGraph.setTemplate(template);
//        kleeneGraph.setType(OptimizerType.GREEDY);
//        kleeneGraph.construct();
//        PathSearcher searcher = new PathSearcher();
//        searcher.pathSearch(kleeneGraph);
//        System.out.println(searcher.toString());






    }

}
