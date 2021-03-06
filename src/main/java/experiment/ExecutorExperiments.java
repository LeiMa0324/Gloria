package experiment;

import gloria.base.DatasetSchema;
import gloria.gloriaExecutor.Executor;
import gloria.gloriaOptimizer.Workload;
import gloria.gloriaOptimizer.graph.OptimizerType;
import lombok.Data;
import gloria.stream.streamLoader;
import gloria.workload.WorkloadAnalyzer;

import java.io.File;

@Data
public class ExecutorExperiments extends OptimizerExperiments{

    private String dataset;
    private int iterNum= 5;
    private int greedySnapshots=0;
    private int normalSnapshots = 0;

    public void varyQueryNum(){
        String streamFile = "src/main/resources/"+dataset+"/"+dataset+"_stream.csv";
        int epw = 500000;
        for (int queryNum = 12; queryNum<121; queryNum+=12) {

            for (int iter = 0; iter<6; iter++){

                this.greedySnapshots = 0;
                this.normalSnapshots = 0;

                String workloadPath = "src/main/resources/"+dataset+"/MixWorkload/workload_"+queryNum+".txt";

                System.out.println("\n==================Query Num:"+queryNum+", iter:"+iter+", Greedy Optimizer==================\n");
                long greedyTime = run(OptimizerType.GREEDY, streamFile, workloadPath, epw);
                int greedyThroughput = (int) (epw/greedyTime);


                System.out.println("\n==================Query Num:"+queryNum+", iter:"+iter+", Normal Optimizer==================\n");
                long normalTime = run(OptimizerType.NORMAL, streamFile, workloadPath, epw);
                int normalThroughput = (int) (epw/normalTime);

                System.out.println("\n==================Query Num:"+queryNum+", iter:"+iter+", NotShare Optimizer==================\n");
                long notshareTime = run(OptimizerType.NOTSHARE, streamFile, workloadPath, epw);
                int notshareThroughput = (int) (epw/notshareTime);

                String[] header={"epw","query_num","iter",
                        "greedy_time","greedy_throu","greedy_snap",
                        "normal_time","normal_throu","normal_snap",
                        "notshare_time","notshare_throu",
                };
                String[] data = {epw+"", queryNum+"",iter+"",
                        greedyTime+"", greedyThroughput+"", this.greedySnapshots+"",

                        normalTime+"", normalThroughput+"", this.normalSnapshots+"",
                notshareTime+"", notshareThroughput+""};
                String logFile = "output/executor/"+dataset+"_varyQueryNum11.csv";

                logging(logFile, header, data);
            }
        }


    }

    public long run(OptimizerType type, String streamFile, String workloadFile, int epw ){
        DatasetSchema schema = new DatasetSchema(dataset);

        WorkloadAnalyzer analyzer = new WorkloadAnalyzer(schema);
        analyzer.setType(type);
        analyzer.analyzeAndOptimize(workloadFile);

        streamLoader loader = new streamLoader(streamFile, schema, analyzer, epw);
        loader.load();

        long insertionTime = 0;

        long start = System.currentTimeMillis();

        for (Workload seqw : analyzer.getSEQWorkloads().values()) {
            Executor executor = new Executor(seqw);
            executor.run();
            if (type==OptimizerType.GREEDY){
                this.greedySnapshots+=executor.getSnapshotManager().getSnapshots().size();
            }

            if (type==OptimizerType.NORMAL){
                this.normalSnapshots +=executor.getSnapshotManager().getSnapshots().size();
                insertionTime+=executor.getSnapshotManager().getSnapShotInsertionTime();
            }
        }

        for (Workload nestedW : analyzer.getNestedWorkloads().values()) {
            Executor executor = new Executor(nestedW);
            executor.run();

            if (type==OptimizerType.GREEDY){
                this.greedySnapshots+=executor.getSnapshotManager().getSnapshots().size();
            }

            if (type==OptimizerType.NORMAL){
                this.normalSnapshots +=executor.getSnapshotManager().getSnapshots().size();
                insertionTime+=executor.getSnapshotManager().getSnapShotInsertionTime();
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("snaposhot insertion time: "+insertionTime);

        return end-start;

    }


    public void logging(String logFile,String[] header, String[] data){
        File file = new File(logFile);
        writeCSV(file, header, data);
    }

    public static void main(String[] args){

        ExecutorExperiments stock_exp = new ExecutorExperiments();
        stock_exp.setDataset("stock");
        stock_exp.varyQueryNum();

        ExecutorExperiments bus_exp = new ExecutorExperiments();
        bus_exp.setDataset("bus");
        bus_exp.varyQueryNum();

        ExecutorExperiments taxi_exp = new ExecutorExperiments();
        taxi_exp.setDataset("taxi");
        taxi_exp.varyQueryNum();
    }

}
