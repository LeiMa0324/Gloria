package gloria.workload.workloadGenerator;

import gloria.base.DatasetSchema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Random;

@Data
public class FlatKleeneWorkloadGenerator extends WorkloadGenerator {


    public FlatKleeneWorkloadGenerator(DatasetSchema schema) {
        super(schema);
    }

    public void generate(int groupNum, int groupSize,  int KleeneLength){
        int K_groupNum = (int) ((int)groupNum*K_percent);
        this.queryPerGroup = groupSize;
        this.K_percent = 1.0;
        this.groupNum = groupNum;

        if (KleeneLength!=1){
            //flat kleene groups
            for (int j = K_groupNum; j < groupNum; j++){
                flatKleeneMiniWorkload(KleeneLength);

            }
        }else {
            System.out.println("Please use singleKleeneMiniWorkload function!");
        }

    }

    /**
     * generate flat kleene patterns with single event type, hamlet
     * @param groupNum
     * @param groupSize
     * @param patternLength
     * @param fixedPredicate
     */
    public void singleKleeneMiniWorkload(int groupNum, int groupSize, int patternLength, boolean fixedPredicate){

        this.queryPerGroup = groupSize;

        ArrayList<Enum> kleenes = new ArrayList<>();


        for (int i = 0; i<groupNum; i++){


            //set aggregator
            String preAggregator = Math.random()<0.4?"COUNT":"";

            ArrayList<Enum> availableLows = getAvailableLows();
            ArrayList<Enum> availableHighs = getAvailableHighs();

            //set prefix
            String prefix = "";

            Random random = new Random();
            Enum kleene = availableHighs.get(random.nextInt(availableHighs.size()));

            while (kleenes.contains(kleene)){
                kleene = availableHighs.get(random.nextInt(availableHighs.size()));
            }

            kleenes.add(kleene);
            availableHighs.remove(kleene);

            String SEQ_pattern = kleene.toString()+","+highest(availableLows);
            String[] eventTypes = SEQ_pattern.split(",");


            StringBuilder k_patternBuilder = new StringBuilder();

            for (int j =0; j< eventTypes.length-1; j++){
                k_patternBuilder.append(eventTypes[j]);
                if (j!=eventTypes.length-2){
                    k_patternBuilder.append(",");
                }
            }

            String k_pattern = eventTypes.length==2?k_patternBuilder.toString()+"+": "SEQ("+k_patternBuilder.toString()+")+";
            String predEventType = SEQ_pattern.split(",")[0];
            String predicate ="";

            if (fixedPredicate){
                predicate = predicate(predEventType);
            }

            String suffix_first =  eventTypes[eventTypes.length-1];

            //concat the prefix, kleene pattern and suffix for each gloria.query
            for (int k =0; k < queryPerGroup ; k++){


                StringBuilder suffix = new StringBuilder(suffix_first + ",");

                for (int j = 0; j<patternLength-2; j++){
                    Enum suffix_event = availableHighs.get(random.nextInt(availableHighs.size()));

                    while (kleenes.contains(suffix_event)){
                        suffix_event = availableHighs.get(random.nextInt(availableHighs.size()));
                    }

                    availableHighs.remove(suffix_event);

                    suffix.append(suffix_event.name());

                    if (j!=(patternLength-3)){
                        suffix.append(",");
                    }
                }


                String query = createQuery(prefix, suffix.toString(), k_pattern, predEventType, preAggregator, predicate);
                this.candidateQueries.add(query);
                System.out.println("\n");
                System.out.println(query);
            }

        }

    }

    public void flatKleeneMiniWorkload(int KleeneLength){

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

            String query = createQuery(prefix, suffix, k_pattern, predEventType, preAggregator,"");
            this.candidateQueries.add(query);
            System.out.println("\n");
            System.out.println(query);
        }

    }
}
