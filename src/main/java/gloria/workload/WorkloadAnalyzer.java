package gloria.workload;

import gloria.gloriaOptimizer.Workload;
import gloria.base.DatasetSchema;
import gloria.gloriaOptimizer.graph.KleeneSubGraph.FullySharedKleeneGraph;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.pathSearcher.PathSearcher;
import gloria.gloriaOptimizer.template.Template;
import gloria.query.Query;
import gloria.query.QueryParser;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * this gloria.workload analyzer analyzes the sharing opportunity of a multi-gloria.query gloria.workload
 * it reads a gloria.workload file, turns it into a list of queries and
 * decides which queries could be shared together and partition the queries into mini-workloads
 */
@Data
public class WorkloadAnalyzer {
    private DatasetSchema schema;
    private HashMap<String, Workload> nestedWorkloads;
    private HashMap<String, Workload> singleWorkloads;
    private HashMap<String, Workload> SEQWorkloads;
    private OptimizerType type;
    private long optimizeTime=0;
    private long memory;
    private boolean doesPrint=false;

    public WorkloadAnalyzer(DatasetSchema schema) {
        this.schema = schema;
        nestedWorkloads = new HashMap<>();
        singleWorkloads = new HashMap<>();
        SEQWorkloads = new HashMap<>();
        this.type = OptimizerType.NORMAL;
    }


    public void analyzeAndOptimize(String workloadFile){
        long start = System.currentTimeMillis();

        analyze(workloadFile);
        optimizeWorkloads();
        long end = System.currentTimeMillis();

        this.optimizeTime = end-start;
    }

    public void optimizeWorkloads(){


        for (Workload SEQW: this.getSEQWorkloads().values()) {
            SEQW.optimize(this.type);
//            print(SEQW);
            this.memory +=SEQW.getMemory();
        }

        for (Workload singleW: this.getSingleWorkloads().values()){
            Template template = new Template();
            template.loadFromWorkload(singleW);
            FullySharedKleeneGraph KleeneGraph = new FullySharedKleeneGraph();
            KleeneGraph.setType(type);
            KleeneGraph.setTemplate(template);
            KleeneGraph.construct();
            singleW.setGraph(KleeneGraph);

            //todo: path search is wrong with single kleene event type, nothing is shared
            PathSearcher searcher = new PathSearcher();
            singleW.setPath(searcher.pathSearch(KleeneGraph));
            singleW.setMiniCost(searcher.getMinCost());
            singleW.setTemplate(template);
            KleeneGraph.computeMemory();

//            print(singleW);

            this.memory +=singleW.getMemory();
        }

        for (Workload nestedW: this.getNestedWorkloads().values()){
            Template template = new Template();
            template.loadFromWorkload(nestedW);
            FullySharedKleeneGraph KleeneGraph = new FullySharedKleeneGraph();
            KleeneGraph.setType(type);
            KleeneGraph.setTemplate(template);
            KleeneGraph.construct();
            nestedW.setGraph(KleeneGraph);

            PathSearcher searcher = new PathSearcher();
            nestedW.setPath(searcher.pathSearch(KleeneGraph));
            nestedW.setMiniCost(searcher.getMinCost());
            nestedW.setTemplate(template);
            KleeneGraph.computeMemory();

            this.memory +=nestedW.getMemory();

//            print(nestedW);
        }

    }

    public void print(Workload workload){
        if (doesPrint){
            System.out.println(workload.toString());
        }
    }

    public void optimizeSEQWorkloads(){
        for (Workload SEQW: this.getSEQWorkloads().values()) {
            SEQW.optimize(this.type);
        }
    }

    public void optimizeSingleCycleWorkloads(){
        int i =0;
        for (Workload SingleW: this.getSingleWorkloads().values()) {

            SingleW.optimize(this.type);
            i++;
        }
    }

    public void optimizeNestedWorkloads(){
        for (Workload Nestedw: this.getNestedWorkloads().values()) {
            Nestedw.optimize(this.type);
        }
    }



    /**
     * read & parse queries from the gloria.workload file
     * @param workloadFile the gloria.workload file
     */
    private ArrayList<Query> readQueriesFromFile(String workloadFile) {
        ArrayList<Query> queries = new ArrayList<>();

        try {
            Scanner query_scanner = new Scanner(new File(workloadFile));
            while (query_scanner.hasNextLine()) {

                ArrayList<String> lines = new ArrayList<>();
                for (int i =0; i < 7; i++){
                    lines.add(query_scanner.nextLine());
                }
                QueryParser parser = new QueryParser(this.schema);
                Query query = parser.parse(lines);
                query.setId(queries.size());
                queries.add(query);

            }
            query_scanner.close();

        }catch(IOException e){}

        return queries;
    }

    /**
     * analyze to a whole gloria.workload
     * @param workloadFile
     * @return
     */
    public Workload analyzeToWorkload(String workloadFile){
        ArrayList<Query> queries = readQueriesFromFile(workloadFile);
        return new Workload(schema, queries);
    }

    public void analyze(String workloadFile){
        ArrayList<Query> queries = readQueriesFromFile(workloadFile);

        long start = System.currentTimeMillis();

        for (Query q: queries){
            String patternString = q.getPattern().getPatternString();
            int plusNum = patternString.length() - patternString.replace("+","").length();

            if (plusNum==0){
                addQueryToSEQWorkload(q);
            }
            if (plusNum==1){
                addQueryToSingleWorkload(q);
            }

            if (plusNum>1){
                addQueryToNestedWorkload(q);
            }
        }

        ArrayList<String> commonKeys = new ArrayList<>();

        for (String s_key: singleWorkloads.keySet()){
            for (String n_key: nestedWorkloads.keySet()){
                if (s_key.equals(n_key)){
                    commonKeys.add(s_key);
                }
            }
        }


        if (!commonKeys.isEmpty()){

            for (String key:commonKeys){
                nestedWorkloads.get(key).getQueries().addAll(singleWorkloads.get(key).getQueries());
                singleWorkloads.remove(key);
            }

        }
    }


    private void addQueryToSEQWorkload(Query query){
        String key = getSEQKey(query);

        Workload workload;
        if (!SEQWorkloads.containsKey(key)){
            workload = new Workload(schema);

        }else {
            workload = SEQWorkloads.get(key);

        }

        workload.addQuery(query);
        SEQWorkloads.put(key, workload);
    }

    private String getSEQKey(Query query){
        String second = query.getPattern().getPatternString().split(",")[1];
        String third = query.getPattern().getPatternString().split(",")[2];
        String fourth = query.getPattern().getPatternString().split(",")[3];
        String fifth = query.getPattern().getPatternString().split(",")[4];
        return second+","+third+","+fourth+","+fifth;
    }

    private void addQueryToSingleWorkload(Query query){
        String key = query.getPattern().getPatternString().split("\\+")[0].
                replace("SEQ(","").replace(")","").
        trim();


        Workload workload;
        if (!singleWorkloads.containsKey(key)){
            workload = new Workload(schema);
        }else {
            workload = singleWorkloads.get(key);
        }
        workload.addQuery(query);
        singleWorkloads.put(key, workload);
    }

    private void addQueryToNestedWorkload(Query query){
        String plainSequence = query.getPattern().getPatternString().replace("SEQ(","").
                replace(")","").replace("+","")
                .trim();
        String[] eventTypes = plainSequence.split(",");
        StringBuilder keyBuilder = new StringBuilder();
        int keyLength = 10;
        for (int i=0; i<keyLength; i++){
            keyBuilder.append(eventTypes[i]);
            if (i!=keyLength-1) {
                keyBuilder.append(",");
            }
        }

        Workload workload;
        if (!nestedWorkloads.containsKey(keyBuilder.toString())){
            workload = new Workload(schema);
        }else {
            workload = nestedWorkloads.get(keyBuilder.toString());
        }
        workload.addQuery(query);
        nestedWorkloads.put(keyBuilder.toString(), workload);
    }
}
