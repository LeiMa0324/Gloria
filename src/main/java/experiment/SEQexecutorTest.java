package experiment;

import gloria.base.DatasetSchema;
import gloria.gloriaExecutor.Executor;
import gloria.gloriaOptimizer.Workload;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.stream.streamLoader;
import gloria.workload.WorkloadAnalyzer;

public class SEQexecutorTest {

    public static void main(String[] args ){
        String workloadPath = "src/main/resources/bus/MixWorkload/workload_120.txt";
        String streamFile = "src/main/resources/bus/bus_stream.csv";
        DatasetSchema schema = new DatasetSchema("bus");
        int epw = 800000;


        for (int i =0;i<1; i++) {

            WorkloadAnalyzer analyzer = new WorkloadAnalyzer(schema);
            analyzer.setType(OptimizerType.NOTSHARE);
            analyzer.setDoesPrint(false);
            analyzer.analyzeAndOptimize(workloadPath);


            streamLoader loader = new streamLoader(streamFile, schema, analyzer, epw);
            loader.load();
            System.out.println("load finished!");
            System.out.println("\n======================NOT SHARE START=============================\n");

            long start = System.currentTimeMillis();
            int loopTimes = 0;

            for (Workload seqw : analyzer.getSEQWorkloads().values()) {
                Executor executor = new Executor(seqw);
                executor.setDoesPrint(true);
                executor.run();
//                System.out.println("\nNon share execution, non share function time: "+executor.getNonshareExecutionTime());
//                System.out.println("\nNon share execution, share function time: "+executor.getShareExecutionTime());
//                System.out.println("\nNon share execution, nonShare times:"+executor.getNonshareTimes());
//                System.out.println("\nNon share execution, share times:"+executor.getShareTimes());
            }



            long end = System.currentTimeMillis();

            System.out.println("NotShare Execution: " + (end - start));


//            System.out.println("\n======================Normal START=============================\n");
//
//            WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(schema);
//            greedy_analyzer.setType(OptimizerType.NORMAL);
//            greedy_analyzer.setDoesPrint(false);
//            greedy_analyzer.analyzeAndOptimize(workloadPath);
//
//
//            streamLoader g_loader = new streamLoader(streamFile, schema, greedy_analyzer, epw);
//            g_loader.load();
//            System.out.println("load finished!");
//
//            long g_start = System.currentTimeMillis();
//            int n_loopTimes =0;
//
//            for (Workload seqw : greedy_analyzer.getSEQWorkloads().values()) {
//                Executor executor = new Executor(seqw);
//                executor.setDoesPrint(false);
//                executor.run();
////                System.out.println("\nNormal execution, non share function time: "+executor.getNonshareExecutionTime());
////                System.out.println("\nNormal execution, share function time: "+executor.getShareExecutionTime());
////                System.out.println("\nNormal execution, nonShare times:"+executor.getNonshareTimes());
////                System.out.println("\nNormal execution, share times:"+executor.getShareTimes());
//            }
//
//
//            long g_end = System.currentTimeMillis();
//
//            System.out.println("Normal Execution: " + (g_end - g_start));

        }

    }
}
