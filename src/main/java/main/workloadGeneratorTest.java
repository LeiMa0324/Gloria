package main;

import base.DatasetSchema;
import experiment.OptimizerExperiments;
import experiment.WorkloadGeneration;
import workload.WorkloadGenerator;

public class workloadGeneratorTest {
    public static void main(String[] args){

        WorkloadGeneration generation = new WorkloadGeneration();
        generation.setDataset("taxi");


        generation.neste_varyKleeneLayers();
//        generation.mixWorkload();


    }
}
