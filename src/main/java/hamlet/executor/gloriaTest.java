package hamlet.executor;

import hamlet.base.Attribute;
import hamlet.base.DatasetSchema;
import hamlet.users.stockUser.stockAttributeEnum;

public class gloriaTest {
    public static void main(String[] args){
        //non-bursty
        String streamFile = "src/main/resources/Gloria/stock_stream_concat.csv";
//        //bursty
//        String streamFile = "src/main/resources/Nasdaq/Nasdaq_bursty_most.csv";
        String logFile = "output/gloria.csv";
        int epw = 100000;

        DatasetSchema schema = new DatasetSchema();
        for (stockAttributeEnum a: stockAttributeEnum.values()){
            if (a!=stockAttributeEnum.open_level){
                schema.addAttribute(new Attribute(a.toString()));
            }

        }

        int qNum = 100;
        //simple gloria.workload
        String workloadFile = "src/main/resources/Gloria/shortPattern_SamePredicates/workload_"+qNum+".txt";

//        //complex gloria.workload
//        String workloadFile = "src/main/resources/Gloria/longPatterns/workload_"+qNum+".txt";

        Executor executor = new Executor(schema, epw, workloadFile, streamFile);
        executor.workloadAnalysis(workloadFile);
        executor.streamPartitioning(streamFile);


        executor.dynamicRun();

        System.out.printf("\ngloria.query Num:"+qNum+",Dynamic took:"+executor.getDynamicExcTime()+", opt time:"+executor.getDynamicOptTime()+"\n");
        System.out.printf("\nSnapshot Num:"+executor.getDynamicSnapshotNum());

    }

}
