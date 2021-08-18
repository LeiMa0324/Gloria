package workload;

import base.DatasetSchema;
import lombok.Data;
import users.stockUser.stockHighFrequencyEventTypeEnum;
import users.stockUser.stockLowFrequencyEventTypeEnum;
import users.taxiUser.taxiHighFrequencyEventEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IllegalFormatCodePointException;

@Data
public class SEQWorkloadGenerator<T> extends WorkloadGenerator{

    public SEQWorkloadGenerator(DatasetSchema schema) {
        super(schema);
    }

    public void generate(int groupNum, int groupSize, double K_percent){
        int K_groupNum = (int) ((int)groupNum*K_percent);
        this.queryPerGroup = groupSize;
        this.K_percent = K_percent;
        this.groupNum = groupNum;
        //seq groups
        for (int j = K_groupNum; j < groupNum; j++){
            SEQMiniWorkload();

        }
    }

    public void SEQMiniWorkload(){

        ArrayList<Enum> availableHighs = getAvailableHighs();
        ArrayList<Enum> availableLows = getAvailableLows();


        //set aggregator
        String preAggregator = Math.random()<0.4?"COUNT":"";

        //four event types, decreasing order
        String SEQ_pattern = generateDecreasingSEQ(availableHighs, availableLows, 4);
        String k_pattern = SEQ_pattern;
        String predEventType = SEQ_pattern.split(",")[0];

        System.out.println("=======================Mini Workload of SEQ:=======================");

        for (int i =0; i < queryPerGroup ; i++){

            String first = randomHigh(availableHighs);
            String fifth = randomHigh(availableHighs);

            String query = createQuery(first, fifth, k_pattern, predEventType, preAggregator);
            this.candidateQueries.add(query);
            System.out.println("\n");

            System.out.println(query);
        }

    }
}
