package gloria.workload.workloadGenerator;

import gloria.base.DatasetSchema;

import java.util.ArrayList;
import java.util.HashMap;

public class MixWorkloadGenerator extends WorkloadGenerator {
    private ArrayList<String> N_keys;
    private ArrayList<String> S_keys;
    private double kleeneRatio;

    public MixWorkloadGenerator(DatasetSchema schema){
        super(schema);
        this.N_keys = new ArrayList<>();
        this.S_keys = new ArrayList<>();
    }

    public void generate(int incrementTimes){
        for (int i =1; i<incrementTimes+1; i++){
            incrementalGenerate(10, kleeneRatio);
            this.toFile("src/main/resources/"+this.schema.getDatasetName()+"/MixWorkload/workload_"+
                    (int)(i*(10+10*kleeneRatio))+""
                    +".txt");
        }
    }

    public void generateDifferentKleeneRatio(){
        int seqNum = 9;

        HashMap<String, ArrayList<String>> nestedQueries = new HashMap<>();
        HashMap<String, ArrayList<String>> seqQueries = new HashMap<>();
//
        for (int i =1; i<11; i++){

            nestedKleeneWorkloadGenerator n_generator = new nestedKleeneWorkloadGenerator(this.schema);
            n_generator.getSeqkeys().addAll(this.N_keys);
            n_generator.generate(1,10, 2 );
            this.candidateQueries.addAll(n_generator.getCandidateQueries());
            this.N_keys = n_generator.getSeqkeys();
            nestedQueries.put(N_keys.get(N_keys.size()-1), n_generator.getCandidateQueries());


            SEQWorkloadGenerator s_generator = new SEQWorkloadGenerator(this.schema);
            s_generator.getSeqkeys().addAll(this.S_keys);
            s_generator.generate(1, 10, 0.0);
            this.candidateQueries.addAll(s_generator.candidateQueries);
            this.S_keys = s_generator.getSeqkeys();
            seqQueries.put(S_keys.get(S_keys.size()-1), s_generator.getCandidateQueries());
        }

        ArrayList<String> nestedQ = new ArrayList<>();

        for (double kratio=0.1; kratio<0.7; kratio+=0.1){
            ArrayList<String> seqQ = new ArrayList<>();

            //remove one seq gloria.query from each group
            for (String key: seqQueries.keySet()) {
                int last = seqQueries.get(key).size() - 1;
                seqQueries.get(key).remove(last);
                seqQ.addAll(seqQueries.get(key));
            }
            //add one kleene gloria.query from each group
            for (String key: nestedQueries.keySet()){
                int last = nestedQueries.get(key).size() - 1;
                nestedQ.add(nestedQueries.get(key).get(last));
                nestedQueries.get(key).remove(last);
            }

            this.candidateQueries.clear();
            this.candidateQueries.addAll(seqQ);
            this.candidateQueries.addAll(nestedQ);

            this.toFile("src/main/resources/"+this.schema.getDatasetName()+"/MixWorkload/kleeneRatio/workload_"+
                    (int)(10*kratio)+""
                    +".txt");
        }


    }

    public void incrementalGenerate(int incrementalQueryNum, double KleenePercent){
        this.queryPerGroup = (int) Math.round(incrementalQueryNum*KleenePercent);

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
