package main;

import gloriaOptimizer.graph.Graph;
import gloriaOptimizer.graph.GraphConstructor;
import gloriaOptimizer.graph.pathSearcher.PathSearcher;
import base.DatasetSchema;
import query.Query;
import query.pattern.Pattern;
import gloriaOptimizer.template.Template;
import gloriaOptimizer.Workload;

public class singleKleeneTest {

    public static void main(String[] args){
        DatasetSchema schema = new DatasetSchema("stock");
        Workload workload = new Workload(schema);

        Pattern p1 = new Pattern("SEQ(A, B, SEQ(C, E, F)+, G)");
        Query q1 = new Query(p1);
        q1.setId(1);

        Pattern p2 = new Pattern("SEQ(A, B, SEQ(C, E, F)+), G");
        Query q2 = new Query(p2);
        q2.setId(2);

        Pattern p3 = new Pattern("SEQ(A, B, SEQ(C, E, F)+), G");
        Query q3 = new Query(p3);
        q3.setId(3);

        Pattern p4 = new Pattern("SEQ(A, B, SEQ(C, E, F)+), G");
        Query q4 = new Query(p4);
        q4.setId(4);

        workload.addQuery(q1);
        workload.addQuery(q2);
        workload.addQuery(q3);
        workload.addQuery(q4);

        Template template = new Template();
        template.loadFromWorkload(workload);

        GraphConstructor constructor = new GraphConstructor(template);
        Graph graph =  constructor.constructGraph();

        StringBuilder sb = new StringBuilder();

        System.out.println(sb.toString());
        System.out.println(graph.toString());


        PathSearcher searcher = new PathSearcher();
        searcher.pathSearch(graph);
        System.out.println(searcher.toString());
    }
}
