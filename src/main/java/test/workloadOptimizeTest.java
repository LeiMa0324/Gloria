package test;

import gloria.base.DatasetSchema;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.workload.WorkloadAnalyzer;

public class workloadOptimizeTest {
    public static void main(String[] args){

        String workloadPath = "src/main/resources/stock/gloria.workload/varyQueryNum/workload_10.txt";
        WorkloadAnalyzer analyzer = new WorkloadAnalyzer(new DatasetSchema("stock"));
        analyzer.setType(OptimizerType.NOPRUNE);
        analyzer.analyze(workloadPath);
        analyzer.optimizeSingleCycleWorkloads();


    }

}
