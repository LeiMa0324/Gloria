package experiment;

import gloria.base.DatasetSchema;
import gloria.gloriaExecutor.Executor;
import gloria.gloriaOptimizer.Workload;
import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.stream.streamLoader;
import gloria.workload.WorkloadAnalyzer;
import hamlet.base.Attribute;
import hamlet.users.stockUser.stockAttributeEnum;

public class GloriaVSHamletExperiments {

    public static void main(String[] args){
//
        int[] burstSizes={50, 150, 100, 200};
        for (int b: burstSizes){
            System.out.println("Burst Size: "+b);
            simpleWorkload_BurstyStream(b);
            complexWorkload_BurstyStream(b);

        }

//        varyWorkloadComplexity();


    }

    public static void simpleWorkload_BurstyStream(int burstSize){
        String streamFile = "src/main/resources/stock/GloriaVSHamletWorkload/Nasdaq_bursty_"+burstSize+".csv";
        String workloadFile = "src/main/resources/stock/GloriaVSHamletWorkload/MixWorkload/workload_0.txt";
        DatasetSchema schema = new DatasetSchema("stock");
        int epw = 100000;

        System.out.println("===================Simple Workload, Bursty Stream===================");
        run(workloadFile, streamFile, epw);

    }


    public static void complexWorkload_BurstyStream(int burstSize){
        String streamFile = "src/main/resources/stock/GloriaVSHamletWorkload/Nasdaq_bursty_"+burstSize+".csv";
        String workloadFile = "src/main/resources/stock/GloriaVSHamletWorkload/MixWorkload/workload_100.txt";
        DatasetSchema schema = new DatasetSchema("stock");
        int epw = 100000;

        System.out.println("===================Complex Workload, Bursty Stream===================");
        run(workloadFile, streamFile, epw);



    }


    public static void run(String workloadFile, String streamFile, int epw){
        DatasetSchema schema = new DatasetSchema("stock");
        int iters = 5;

        int H_snapshotNum = 0;
        //Hamlet
        long H_duration = 0;
        for (int i = 0; i<iters; i++){
            hamlet.base.DatasetSchema hamlet_schema = new hamlet.base.DatasetSchema();
            for (stockAttributeEnum a: stockAttributeEnum.values()){
                if (a!=stockAttributeEnum.open_level){
                    hamlet_schema.addAttribute(new Attribute(a.toString()));
                }
            }

            hamlet.executor.Executor executor = new hamlet.executor.Executor(hamlet_schema, epw, workloadFile, streamFile);
            executor.workloadAnalysis(workloadFile);
            executor.streamPartitioning(streamFile);

            executor.dynamicRun();
            H_duration+=executor.getDynamicExcTime();
            if (i==0){
                H_snapshotNum += executor.getDynamicSnapshotNum();
            }

        }
        int G_snapshotNum = 0;

        //Gloria
        long G_duration = 0;
        for (int i = 0; i<iters; i++){
            WorkloadAnalyzer analyzer = new WorkloadAnalyzer(schema);

            OptimizerType type =OptimizerType.NORMAL;
            analyzer.setType(type);
//            System.out.println("Gloria Start to optimize....");
            analyzer.analyzeAndOptimize(workloadFile);

            streamLoader loader = new streamLoader(streamFile, schema, analyzer, epw);
            loader.load();

//            System.out.println("Gloria Start to execute....");

            for (Workload singleW : analyzer.getSingleWorkloads().values()) {
                Executor executor = new Executor(singleW);
                executor.run();
                G_duration+= executor.getLatency();
                if (i==0){
                    G_snapshotNum+= executor.getSnapshotManager().getSnapshots().size();
                }
            }
        }


        System.out.println("******************Exp results******************");
        System.out.println("Gloria avg time:"+ ((double)G_duration/iters));
        System.out.println("Gloria Snapshot Num:"+G_snapshotNum);
        System.out.println("Hamlet avg time:"+ H_duration/iters);
        System.out.println("Hamlet Snapshot Num:"+H_snapshotNum);
    }

    public static void varyWorkloadComplexity(){
        String streamFile = "src/main/resources/stock/GloriaVSHamletWorkload/Nasdaq_bursty_100.csv";
        DatasetSchema schema = new DatasetSchema("stock");
        int epw = 100000;
        for (int i = 0; i<=100; i+=20){
            String workloadFile = "src/main/resources/stock/GloriaVSHamletWorkload/MixWorkload/workload_"+i+".txt";
            System.out.println("complex workload percent: "+i+"%");
            run(workloadFile, streamFile, epw);
        }
    }
}
