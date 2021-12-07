package experiment;

import base.DatasetSchema;
import workload.workloadGenerator.MixWorkloadGenerator;

public class workloadGeneratorTest {
    public static void main(String[] args){

//        WorkloadGeneration generation = new WorkloadGeneration();
//        generation.setDataset("bus");
//
//        generation.SEQ_varyQueryNumWorkloadGeneration();
//        generation.Single_varyKleeneLength();
//        generation.neste_varyKleeneLayers();
//        generation.mixWorkload();
        DatasetSchema schema = new DatasetSchema("taxi");
        MixWorkloadGenerator generator = new MixWorkloadGenerator(schema);
        generator.generateDifferentKleeneRatio();


    }
}
