package experiment;

import base.DatasetSchema;
import com.opencsv.CSVWriter;
import gloriaOptimizer.graph.OptimizerType;
import lombok.Data;
import workload.WorkloadAnalyzer;
import workload.WorkloadGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Data
public class OptimizerExperiments {
    private String dataset;
    private int iterNum= 16;



    public void SEQ_varyQueryNum(){

        int K_length = 0;
        for (int queryNum = 200; queryNum>=20; queryNum-=20){

            for (int iter = 0; iter<this.iterNum; iter++) {
                String workloadPath = "src/main/resources/" + dataset + "/SEQWorkload/workload_" + queryNum + ".txt";


                WorkloadAnalyzer noprune_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                noprune_analyzer.setType(OptimizerType.NOPRUNE);
                noprune_analyzer.analyzeAndOptimize(workloadPath);


                WorkloadAnalyzer normal_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));

                System.out.printf("\n==============NORMAL ANALYZER BEGIN!=================\n");
                normal_analyzer.setType(OptimizerType.NORMAL);
                normal_analyzer.analyzeAndOptimize(workloadPath);


                System.out.printf("\n==============NORMAL ANALYZER END!=================\n\n");

                System.out.printf("==============GREEDY ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                greedy_analyzer.setType(OptimizerType.GREEDY);
                greedy_analyzer.analyzeAndOptimize(workloadPath);
                System.out.printf("\n==============GREEDY ANALYZER END!=================\n");




                String[] data = {queryNum+"", K_length+"",0+"", iter+"",
                        noprune_analyzer.getOptimizeTime()+"",
                        noprune_analyzer.getMemory()+"",
                        normal_analyzer.getOptimizeTime()+"",
                        normal_analyzer.getMemory()+"",
                        greedy_analyzer.getOptimizeTime()+"",
                        greedy_analyzer.getMemory()+"",
                };

                logging(this.dataset+"_varyQueryNum", data);
            }
        }
    }

    public void FullySharedSingleKleene_varyKleeneLength(){

        int queryNum = 100;
        int K_layers = 1;
        for (int Kleene_len = 2; Kleene_len<11; Kleene_len++){
            for (int iter = 0; iter < this.iterNum; iter++) {
                String workloadPath = "src/main/resources/"+dataset+"/KleeneWorkload/kleeneLength/workload_"+Kleene_len+".txt";

                WorkloadAnalyzer noprune_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                noprune_analyzer.setType(OptimizerType.NOPRUNE);
                noprune_analyzer.analyzeAndOptimize(workloadPath);


                System.out.printf("\n==============NORMAL ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer normal_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                normal_analyzer.setType(OptimizerType.NORMAL);
                normal_analyzer.analyzeAndOptimize(workloadPath);
                System.out.printf("\n==============NORMAL ANALYZER END!=================\n\n");


                System.out.printf("==============GREEDY ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                greedy_analyzer.setType(OptimizerType.GREEDY);
                greedy_analyzer.analyzeAndOptimize(workloadPath);

                System.out.printf("\n==============GREEDY ANALYZER END!=================\n");





                String[] data = {queryNum+"", Kleene_len+"",K_layers+"", iter+"",

                        noprune_analyzer.getOptimizeTime()+"",
                        noprune_analyzer.getMemory()+"",
                        normal_analyzer.getOptimizeTime()+"",
                        normal_analyzer.getMemory()+"",
                        greedy_analyzer.getOptimizeTime()+"",
                        greedy_analyzer.getMemory()+""
                        };

                logging(this.dataset+"_varyKleeneLength", data);
            }
        }
    }

    public void FullySharedSingleKleene_varyKleeneLayers(){

        int queryNum = 100;
        int K_length = 10;
        for (int Kleene_layers = 1; Kleene_layers<6; Kleene_layers++){
            for (int iter = 0; iter < iterNum; iter++) {
                String workloadPath = "src/main/resources/"+dataset+"/KleeneWorkload/kleeneLayers/workload_"+Kleene_layers+".txt";

                WorkloadAnalyzer noprune_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                noprune_analyzer.setType(OptimizerType.NOPRUNE);
                noprune_analyzer.analyzeAndOptimize(workloadPath);


                System.out.printf("\n==============NORMAL ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer normal_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                normal_analyzer.setType(OptimizerType.NORMAL);
                normal_analyzer.analyzeAndOptimize(workloadPath);
                System.out.printf("\n==============NORMAL ANALYZER END!=================\n\n");


                System.out.printf("==============GREEDY ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                greedy_analyzer.setType(OptimizerType.GREEDY);
                greedy_analyzer.analyzeAndOptimize(workloadPath);

                System.out.printf("\n==============GREEDY ANALYZER END!=================\n");





                String[] data = {queryNum+"", K_length+"",Kleene_layers+"", iter+"",

                        noprune_analyzer.getOptimizeTime()+"",
                        noprune_analyzer.getMemory()+"",
                        normal_analyzer.getOptimizeTime()+"",
                        normal_analyzer.getMemory()+"",
                        greedy_analyzer.getOptimizeTime()+"",
                        greedy_analyzer.getMemory()+""
                };

                logging(this.dataset+"_varyKleeneLayers", data);
            }
        }
    }


    public void mixWorkload_varyQueryNum(){

        int K_length = 10;
        int Kleene_layers= 2;
        for (int queryNum = 1; queryNum<11; queryNum++){
            for (int iter = 0; iter < iterNum; iter++) {
                String workloadPath = "src/main/resources/"+dataset+"/MixWorkload/workload_"+queryNum*12+".txt";

                System.out.println("Workload File: "+"workload"+queryNum+".txt");

                System.out.printf("\n==============NONPRUNE ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer noprune_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                noprune_analyzer.setType(OptimizerType.NOPRUNE);
                noprune_analyzer.analyzeAndOptimize(workloadPath);
                System.out.printf("\n==============NONPRUNE ANALYZER END!=================\n");



                System.out.printf("\n==============NORMAL ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer normal_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                normal_analyzer.setType(OptimizerType.NORMAL);
                normal_analyzer.analyzeAndOptimize(workloadPath);
                System.out.printf("\n==============NORMAL ANALYZER END!=================\n\n");


                System.out.printf("==============GREEDY ANALYZER BEGIN!=================\n");
                WorkloadAnalyzer greedy_analyzer = new WorkloadAnalyzer(new DatasetSchema(dataset));
                greedy_analyzer.setType(OptimizerType.GREEDY);
                greedy_analyzer.analyzeAndOptimize(workloadPath);

                System.out.printf("\n==============GREEDY ANALYZER END!=================\n");



                //todo:bug about memory

                String[] data = {queryNum*12+"", K_length+"",Kleene_layers+"", iter+"",

                        noprune_analyzer.getOptimizeTime()+"",
                        noprune_analyzer.getMemory()+"",
                        normal_analyzer.getOptimizeTime()+"",
                        normal_analyzer.getMemory()+"",
                        greedy_analyzer.getOptimizeTime()+"",
                        greedy_analyzer.getMemory()+""
                };

                logging(this.dataset+"_mixWorkload_varyQueryNum", data);
            }
        }
    }

    public void logging(String expName, String[] data){

        String[] header = {"QueryNum","K_length","K_layers","iter",
                "NoPrune Optimizer","NoPrune Memory",
                "Normal Optimizer", "Normal Memory",
                "Greedy Optimizer","Greedy Memory"};
        String logFile = expName+".csv";
        File file = new File("output/"+ logFile);
        writeCSV(file, header, data);
    }

    public void writeCSV(File file, String[] header, String[] data){
        try {
            if(!file.exists()){
                file.createNewFile();
                FileWriter outputfile = new FileWriter(file, true);
                CSVWriter writer = new CSVWriter(outputfile);
                writer.writeNext(header);
                writer.close();
            }
            FileWriter fileWriter = new FileWriter(file, true);
            CSVWriter writer = new CSVWriter(fileWriter);
            //write the data
            writer.writeNext(data);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
