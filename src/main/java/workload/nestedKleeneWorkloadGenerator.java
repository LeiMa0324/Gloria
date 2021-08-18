package workload;

import base.DatasetSchema;
import users.stockUser.stockHighFrequencyEventTypeEnum;
import users.stockUser.stockLowFrequencyEventTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;

public class nestedKleeneWorkloadGenerator extends WorkloadGenerator {

    public nestedKleeneWorkloadGenerator(DatasetSchema schema) {
        super(schema);
    }

    public void generate(int groupNum, int groupSize,  int nestedLayers){
        int K_groupNum = (int) ((int)groupNum*K_percent);
        this.queryPerGroup = groupSize;
        this.K_percent = 1.0;
        this.groupNum = groupNum;

        //single kleene groups
        for (int j = K_groupNum; j < groupNum; j++){
            nestedKleeneMiniWorkload(nestedLayers);

        }
    }

    public void nestedKleeneMiniWorkload(int nestedLayers){

        ArrayList<Enum> availableHighs = getAvailableHighs();
        ArrayList<Enum> availableLows = getAvailableLows();

        //set aggregator
        String preAggregator = Math.random()<0.4?"COUNT":"";

        int KleeneLength = 10;
        //set nested event types
        String SEQ_pattern = generateDecreasingSEQ(availableHighs, availableLows, KleeneLength+1);

        String[] eventTypes = SEQ_pattern.split(",");
        StringBuilder k_patternBuilder = new StringBuilder();

        int leftLayers = 0;
        int rightLayers = nestedLayers;

        for (int i =0; i< eventTypes.length-1; i++){
            if (leftLayers<nestedLayers){
                k_patternBuilder.append("SEQ(");
                leftLayers++;
            }

            k_patternBuilder.append(eventTypes[i]);

            if (eventTypes.length-1-i==rightLayers){
                k_patternBuilder.append(")+");
                rightLayers--;
            }

            if (i!=eventTypes.length-2){
                k_patternBuilder.append(",");
            }


        }
        String k_pattern = k_patternBuilder.toString();
        String predEventType = SEQ_pattern.split(",")[0];
        String suffix_first =  eventTypes[eventTypes.length-1];

        System.out.println("=======================Mini Workload of Nested Kleene:=======================");


        for (int i =0; i < queryPerGroup ; i++){

            String prefix = "";
            String suffix = suffix_first+","+randomHigh(availableHighs);


            String query = createQuery(prefix, suffix, k_pattern, predEventType, preAggregator);
            this.candidateQueries.add(query);
            System.out.println("\n");
            System.out.println(query);
        }

    }
}
