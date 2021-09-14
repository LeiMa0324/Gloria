package experiment;

public class OptimizerExperimentRun {
    public static void main(String[] args){

        OptimizerExperiments experiments = new OptimizerExperiments();
        experiments.setDataset("bus");

        experiments.SEQ_varyQueryNum();
        experiments.FullySharedSingleKleene_varyKleeneLength();
        experiments.FullySharedSingleKleene_varyKleeneLayers();
        experiments.mixWorkload_varyKRatio();

        OptimizerExperiments experiments1 = new OptimizerExperiments();
        experiments1.setDataset("taxi");
//
        experiments.SEQ_varyQueryNum();
        experiments.FullySharedSingleKleene_varyKleeneLength();
        experiments.FullySharedSingleKleene_varyKleeneLayers();
        experiments1.mixWorkload_varyKRatio();


    }
}
