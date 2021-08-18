package main;

import base.Attribute;
import base.DatasetSchema;
import stream.streamLoader;
import users.stockUser.stockAttributeEnum;
import workload.WorkloadAnalyzer;

public class streamLoaderTest {
    public static void main(String[] args ){
        String workloadPath = "src/main/resources/workload_100.txt";
        String streamFile = "src/main/resources/stock/stream.csv";
        DatasetSchema schema = new DatasetSchema("stock");

        for (stockAttributeEnum a: stockAttributeEnum.values()){
            schema.addAttribute(new Attribute(a.toString()));
        }

        WorkloadAnalyzer analyzer = new WorkloadAnalyzer(schema);
        analyzer.analyzeAndOptimize(workloadPath);

        analyzer.optimizeWorkloads();


        streamLoader loader = new streamLoader(streamFile,schema, analyzer,100000 );
        loader.load();
        System.out.println("load finished!");


    }
}
