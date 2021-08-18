package experiment;

public class executorTest {
    public static void main(String[] args){
        ExecutorExperiments stock_exp = new ExecutorExperiments();
        stock_exp.setDataset("stock");
        stock_exp.varyQueryNum();

        ExecutorExperiments bus_exp = new ExecutorExperiments();
        bus_exp.setDataset("bus");
        bus_exp.varyQueryNum();

        ExecutorExperiments taxi_exp = new ExecutorExperiments();
        taxi_exp.setDataset("taxi");
        taxi_exp.varyQueryNum();

    }
}
