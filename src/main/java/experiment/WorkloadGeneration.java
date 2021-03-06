package experiment;

import gloria.base.DatasetSchema;
import lombok.Data;
import gloria.workload.workloadGenerator.MixWorkloadGenerator;
import gloria.workload.workloadGenerator.SEQWorkloadGenerator;
import gloria.workload.workloadGenerator.FlatKleeneWorkloadGenerator;
import gloria.workload.workloadGenerator.nestedKleeneWorkloadGenerator;

import java.util.ArrayList;

@Data
public class WorkloadGeneration {
    private String dataset;

    public void SEQ_varyQueryNumWorkloadGeneration(){
        DatasetSchema schema = new DatasetSchema(dataset);
        ArrayList<String> seqkeys = new ArrayList<>();
        ArrayList<String> candidateQueries = new ArrayList<>();

        //vary gloria.query num
        double K_percent = 0.0;
        for (int groupNum = 2; groupNum<21; groupNum+=2){
            SEQWorkloadGenerator generator = new SEQWorkloadGenerator(schema);
            generator.setSeqkeys(seqkeys);
            generator.generate(2, 20, K_percent);

            candidateQueries.addAll(generator.getCandidateQueries());
            seqkeys.addAll(generator.getSeqkeys());
            generator.setCandidateQueries(candidateQueries);
            generator.toFile("src/main/resources/"+dataset+"/SEQWorkload/workload_"+groupNum*10+".txt");
        }
    }

    public void Single_varyKleeneLength(){

        //vary gloria.query num
        int queryNum = 100;
        for (int K_length = 1; K_length<2; K_length+=1){
            DatasetSchema schema = new DatasetSchema(dataset);
            FlatKleeneWorkloadGenerator generator = new FlatKleeneWorkloadGenerator(schema);
            generator.generate(queryNum/10, 10, K_length);


            generator.toFile("src/main/resources/"+dataset+"/KleeneWorkload/kleeneLength/workload_"+K_length+".txt");
        }
    }

    public void neste_varyKleeneLayers(){

        //vary gloria.query num
        int queryNum = 100;
        for (int nestedLayers = 1; nestedLayers<6; nestedLayers+=1){
            DatasetSchema schema = new DatasetSchema(dataset);
            nestedKleeneWorkloadGenerator generator = new nestedKleeneWorkloadGenerator(schema);

            generator.generate(queryNum/10, 10, nestedLayers);

            generator.toFile("src/main/resources/"+dataset+"/KleeneWorkload/kleeneLayers/workload_"+nestedLayers+".txt");
        }
    }

    public void mixWorkload(){
        DatasetSchema schema = new DatasetSchema(dataset);
        MixWorkloadGenerator generator = new MixWorkloadGenerator(schema);
        generator.generate(10);
    }
}
