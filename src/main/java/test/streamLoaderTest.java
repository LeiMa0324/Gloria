package test;

import gloria.base.Attribute;
import gloria.base.DatasetSchema;
import gloria.stream.streamLoader;
import gloria.users.stockUser.stockAttributeEnum;
import gloria.workload.WorkloadAnalyzer;

public class streamLoaderTest {
    public static void main(String[] args ){
        String workloadPath = "src/main/resources/workload_10.txt";
        String streamFile = "src/main/resources/stock/gloria.stream.csv";
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
