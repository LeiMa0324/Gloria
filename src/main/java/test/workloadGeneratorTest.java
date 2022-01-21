package test;

import experiment.WorkloadGeneration;

public class workloadGeneratorTest {
    public static void main(String[] args){

        WorkloadGeneration generation = new WorkloadGeneration();
        generation.setDataset("stock");


        generation.Single_varyKleeneLength();
//        generation.mixWorkload();


    }
}
