package test;

import gloria.gloriaExecutor.Executor;
import gloria.gloriaOptimizer.Workload;
import gloria.base.DatasetSchema;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.stream.streamLoader;
import gloria.workload.WorkloadAnalyzer;

public class SEQexecutorTest {

    public static void main(String[] args ){
        String workloadPath = "src/main/resources/stock/SEQWorkload/workload_60.txt";
        String streamFile = "src/main/resources/stock/stock_stream.csv";
        DatasetSchema schema = new DatasetSchema("stock");
        int epw = 500000;


        for (int i =0;i<2; i++) {

            WorkloadAnalyzer analyzer = new WorkloadAnalyzer(schema);
            analyzer.setType(OptimizerType.NOTSHARE);
            analyzer.setDoesPrint(false);
            analyzer.analyzeAndOptimize(workloadPath);


            streamLoader loader = new streamLoader(streamFile, schema, analyzer, epw);
            loader.load();
            System.out.println("load finished!");

            long start = System.currentTimeMillis();

            for (Workload seqw : analyzer.getSEQWorkloads().values()) {
                Executor executor = new Executor(seqw);
                executor.setDoesPrint(false);
                executor.run();
            }

            for (Workload nestedW : analyzer.getNestedWorkloads().values()) {
                Executor executor = new Executor(nestedW);
                executor.setDoesPrint(false);
                executor.run();
            }

            long end = System.currentTimeMillis();

            System.out.println("NotShare Execution: " + (end - start));

            System.out.println("\n======================GREEDY START=============================\n");

            WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(schema);
            greedy_analyzer.setType(OptimizerType.NORMAL);
            greedy_analyzer.setDoesPrint(false);
            greedy_analyzer.analyzeAndOptimize(workloadPath);


            streamLoader g_loader = new streamLoader(streamFile, schema, greedy_analyzer, epw);
            g_loader.load();
            System.out.println("load finished!");

            long g_start = System.currentTimeMillis();

            for (Workload seqw : greedy_analyzer.getSEQWorkloads().values()) {
                Executor executor = new Executor(seqw);
                executor.setDoesPrint(false);
                executor.run();
            }

            for (Workload seqw : greedy_analyzer.getNestedWorkloads().values()) {
                Executor executor = new Executor(seqw);
                executor.setDoesPrint(false);
                executor.run();
            }

            long g_end = System.currentTimeMillis();

            System.out.println("Normal Execution: " + (g_end - g_start));
        }

    }
}
