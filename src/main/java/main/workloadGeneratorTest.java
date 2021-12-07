package main;

import experiment.WorkloadGeneration;

public class workloadGeneratorTest {
    public static void main(String[] args){

        WorkloadGeneration generation = new WorkloadGeneration();
        generation.setDataset("taxi");


        generation.neste_varyKleeneLayers();
//        generation.mixWorkload();


    }
}
