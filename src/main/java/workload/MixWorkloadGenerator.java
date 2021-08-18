package workload;

import base.DatasetSchema;

import java.util.ArrayList;

public class MixWorkloadGenerator extends WorkloadGenerator {
    private ArrayList<String> N_keys;
    private ArrayList<String> S_keys;

    public MixWorkloadGenerator(DatasetSchema schema){
        super(schema);
        this.N_keys = new ArrayList<>();
        this.S_keys = new ArrayList<>();
    }

    public void generate(int incrementTimes){
        for (int i =1; i<incrementTimes+1; i++){
            incrementalGenerate(10, 0.2);
            this.toFile("src/main/resources/"+this.schema.getDatasetName()+"/MixWorkload/workload_"+
                    i*12+""
                    +".txt");
        }
    }

    public void incrementalGenerate(int incrementalQueryNum, double KleenePercent){
        this.queryPerGroup = (int) Math.round(incrementalQueryNum*KleenePercent);
        this.K_percent = 0.2;
        this.groupNum = incrementalQueryNum/this.queryPerGroup;

        //one kleene group
        nestedKleeneWorkloadGenerator n_generator = new nestedKleeneWorkloadGenerator(this.schema);
        n_generator.getSeqkeys().addAll(this.N_keys);
        n_generator.generate(1,this.queryPerGroup, 2 );
        this.candidateQueries.addAll(n_generator.getCandidateQueries());
        this.N_keys = n_generator.getSeqkeys();

        //four seq groups
        SEQWorkloadGenerator s_generator = new SEQWorkloadGenerator(this.schema);
        s_generator.getSeqkeys().addAll(this.S_keys);
        s_generator.generate(1, this.queryPerGroup*5, 0.0);
        this.candidateQueries.addAll(s_generator.candidateQueries);
        this.S_keys = s_generator.getSeqkeys();

    }



}
