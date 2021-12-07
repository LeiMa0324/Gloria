package workload.workloadGenerator;

import base.DatasetSchema;
import lombok.Data;

import java.util.ArrayList;

@Data
public class SingleKleeneWorkloadGenerator extends WorkloadGenerator {


    public SingleKleeneWorkloadGenerator(DatasetSchema schema) {
        super(schema);
    }

    public void generate(int groupNum, int groupSize,  int KleeneLength){
        int K_groupNum = (int) ((int)groupNum*K_percent);
        this.queryPerGroup = groupSize;
        this.K_percent = 1.0;
        this.groupNum = groupNum;

        //single kleene groups
        for (int j = K_groupNum; j < groupNum; j++){
            singleKleeneMiniWorkload(KleeneLength);

        }
    }

    public void singleKleeneMiniWorkload(int KleeneLength){

        ArrayList<Enum> availableHighs = getAvailableHighs();
        ArrayList<Enum> availableLows = getAvailableLows();

        //set aggregator
        String preAggregator = Math.random()<0.4?"COUNT":"";

        //set nested event types
        String SEQ_pattern = generateDecreasingSEQ(availableHighs, availableLows, KleeneLength+1);
        String[] eventTypes = SEQ_pattern.split(",");
        StringBuilder k_patternBuilder = new StringBuilder();
        for (int i =0; i< eventTypes.length-1; i++){
            k_patternBuilder.append(eventTypes[i]);
            if (i!=eventTypes.length-2){
                k_patternBuilder.append(",");
            }
        }
        String k_pattern = eventTypes.length==2?k_patternBuilder.toString()+"+": "SEQ("+k_patternBuilder.toString()+")+";
        String predEventType = SEQ_pattern.split(",")[0];
        String suffix_first =  eventTypes[eventTypes.length-1];

        System.out.println("=======================Mini Workload of Single Kleene:=======================");


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
