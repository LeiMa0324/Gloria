package main;

import base.DatasetSchema;
import gloriaOptimizer.graph.OptimizerType;
import workload.WorkloadAnalyzer;

public class workloadOptimizeTest {
    public static void main(String[] args){

        String workloadPath = "src/main/resources/stock/workload/varyQueryNum/workload_100.txt";
        WorkloadAnalyzer analyzer = new WorkloadAnalyzer(new DatasetSchema("stock"));
        analyzer.setType(OptimizerType.NOPRUNE);
        analyzer.analyze(workloadPath);
        analyzer.optimizeSingleCycleWorkloads();


    }

}
