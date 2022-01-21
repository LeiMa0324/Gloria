package gloria.workload.workloadGenerator;

import gloria.base.DatasetSchema;
import lombok.Data;
import gloria.users.FrequencyGetter;
import gloria.users.busUser.busAttributeEnum;
import gloria.users.busUser.busHighFrequencyEventEnum;
import gloria.users.busUser.busLowFrequencyEventEnum;
import gloria.users.stockUser.stockHighFrequencyEventTypeEnum;
import gloria.users.stockUser.stockLowFrequencyEventTypeEnum;
import gloria.users.stockUser.stockAttributeEnum;
import gloria.users.taxiUser.taxiAttributeEnum;
import gloria.users.taxiUser.taxiHighFrequencyEventEnum;
import gloria.users.taxiUser.taxiLowFrequencyEventEnum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Data
public class WorkloadGenerator {
    protected DatasetSchema schema;

    protected ArrayList<String> candidateQueries;

    protected double K_percent;
    protected int queryPerGroup;
    protected int groupNum;


    //set groupby
    private String groubyColumn = stockAttributeEnum.open_level.toString();

    //set aggregate column & type
    private String aggreColumn = stockAttributeEnum.open.toString();


    //keys for seq
    private ArrayList<String> seqkeys;

    public WorkloadGenerator(){

        this.seqkeys = new ArrayList<>();
    }


    public WorkloadGenerator(DatasetSchema schema){
        this.schema = schema;
        this.candidateQueries = new ArrayList<>();

        this.seqkeys = new ArrayList<>();
        if (schema.getDatasetName().equals("stock")){

            //set groupby
            groubyColumn = stockAttributeEnum.open_level.toString();

            //set aggregate column & type
            aggreColumn = stockAttributeEnum.open.toString();
        }

        if (schema.getDatasetName().equals("taxi")){


            //set groupby
;
            groubyColumn = taxiAttributeEnum.VendorID.toString();

            //set aggregate column & type
            aggreColumn = taxiAttributeEnum.total_amount.toString();
        }
        if (schema.getDatasetName().equals("bus")){
            //set groupby
            groubyColumn = busAttributeEnum.Line_ID.toString();

            //set aggregate column & type
            aggreColumn = busAttributeEnum.delay.toString();
        }
    }


    public String createQuery(String prefix, String suffix, String k_pattern, String predEvent, String preAggregator, String predicate){

        String pattern = "SEQ("+(prefix.equals("")?prefix:prefix+", ")+ k_pattern+", "+suffix+")";
        String aggregator = preAggregator.equals("")?randomAggregator():preAggregator;
        String returnString = returnString(predEvent, groubyColumn, aggreColumn, aggregator);
        String patternString = patternString(pattern);


        String whereString = whereString(predEvent, predicate.equals("")?predicate(predEvent):predicate,  groubyColumn);

        String[] windows = {"5 min", "10 min","15 min", "20 min"};
        String slide = "5 min";

        String windowString = windowString(randomWindow(windows), slide);

        return returnString+"\n"+
                patternString+"\n"+
                whereString+"\n"+
                windowString;

    }

    public ArrayList<Enum> getAvailableHighs(){
        ArrayList<Enum> availableHighs = new ArrayList<>();

        if (this.schema.getDatasetName().equals("stock")){
            availableHighs = new ArrayList<>(Arrays.asList(stockHighFrequencyEventTypeEnum.values()));
        }

        if (this.schema.getDatasetName().equals("taxi")){
            availableHighs = new ArrayList<>(Arrays.asList(taxiHighFrequencyEventEnum.values()));
        }


        if (this.schema.getDatasetName().equals("bus")){
            availableHighs = new ArrayList<>(Arrays.asList(busHighFrequencyEventEnum.values()));
        }

        return availableHighs;
    }

    public ArrayList<Enum> getAvailableLows(){
        ArrayList<Enum> availableLows = new ArrayList<>();
        if (this.schema.getDatasetName().equals("stock")){
            availableLows = new ArrayList<>(Arrays.asList(stockLowFrequencyEventTypeEnum.values()));
        }

        if (this.schema.getDatasetName().equals("taxi")){
            availableLows = new ArrayList<>(Arrays.asList(taxiLowFrequencyEventEnum.values()));
        }

        if (this.schema.getDatasetName().equals("bus")){
            availableLows = new ArrayList<>(Arrays.asList(busLowFrequencyEventEnum.values()));
        }

        return availableLows;
    }


    public String generateDecreasingSEQ(ArrayList<Enum> availableHighs
            ,ArrayList<Enum> availableLows, int SEQLength){

        String second = lowest(availableHighs);
        StringBuilder key = new StringBuilder(second);

        for (int i = 0; i< SEQLength-1; i++) {
            String newEvent = highest(availableLows);
            key.append(",");
            key.append(newEvent);
        }

        while (this.seqkeys.contains(key.toString())){

            if (SEQLength==1){
                second = lowest(availableHighs);
                key = new StringBuilder(second);
            }else {
                String oldKey = key.toString();
                key = new StringBuilder(second);
                String[] ets = oldKey.split(",");

                for (int i = 2; i < SEQLength; i++) {

                    key.append(",");
                    key.append(ets[i]);
                }
                String newEvent = highest(availableLows);
                key.append(",");
                key.append(newEvent);
            }
        }

        this.seqkeys.add(key.toString());
        return key.toString();

    }
    public String lowest(ArrayList<Enum> availableHighs){
        int minFreq = Integer.MAX_VALUE;
        String lowest = "";
        int minIndex = 0;
        int i = 0;
        for (Enum eventTypeEnum: availableHighs){
            ;
            if (FrequencyGetter.get(eventTypeEnum.toString(), this.schema.getDatasetName())<minFreq){
                minFreq = FrequencyGetter.get(eventTypeEnum.toString(), this.schema.getDatasetName());
                lowest = eventTypeEnum.toString();
                minIndex = i;
            }
            i++;
        }
        availableHighs.remove(minIndex);
        return lowest;
    }

    public String highest(ArrayList<Enum> availableLows){
        int maxFreq = 0;
        String highest = "";
        int maxIndex = -1;
        int i = 0;
        for (Enum eventTypeEnum: availableLows){
            if (FrequencyGetter.get(eventTypeEnum.toString(), this.schema.getDatasetName())>maxFreq){
                maxFreq = FrequencyGetter.get(eventTypeEnum.toString(), this.schema.getDatasetName());
                highest = eventTypeEnum.toString();
                maxIndex = i;
            }
            i++;
        }
        if (maxIndex!=-1) {
            availableLows.remove(maxIndex);
        }
        return highest;
    }

    public String randomHigh(ArrayList<Enum> availableHighs){

        return randomEvent(availableHighs).toString();

    }

    public String predicate(String predEvent){
        String operator="";
        String predCol="";
        int value =0;
        if (this.schema.getDatasetName().equals("stock")){
            operator = ">";
            predCol = "vol";
            value =Math.random()>0.5?100:30;
        }

        if (this.schema.getDatasetName().equals("taxi")){
            operator = ">";
            predCol = "trip_distance";
            value =1;
        }

        if (this.schema.getDatasetName().equals("bus")){
            operator = "=";
            predCol = "congestion";
            value =0;
        }

        return  predEvent+"."+predCol+" "+operator+" "+value;

    }

    public String randomLow(ArrayList<stockLowFrequencyEventTypeEnum> availableLows){

        return randomEvent(availableLows).toString();
    }

    public String randomAggregator(){
        double random = Math.random();
        return random <0.5? "SUM":"AVG";
    }

    public String randomWindow(String[] windows){
        Random r = new Random();
        return windows[r.nextInt(windows.length)];
    }


    public String returnString(String low, String groubyColumn, String aggreColumn, String aggregator){

        String aggregationString = aggregator.equals("COUNT")?"COUNT(*)":aggregator+"("+low+"."+aggreColumn+")";
        return "RETURN "+low+"."+groubyColumn+", "+aggregationString;
    }

    public String patternString(String pattern){
        String[] events = pattern.split(",");
        return events.length==1 ? "PATTERN "+ pattern : "PATTERN "+pattern;
    }

    public String whereString(String kleene, String predicate, String groubyColumn){


        return "WHERE "+predicate+"\n"+
                "GROUP-BY " +kleene+"."+groubyColumn;
    }

    public String windowString(String window, String sliding){
        return "WITHIN "+ window+" SLIDE " +sliding;
    }

    /**
     * return a random event from an arraylist and remove it
     * @param array
     * @return
     */
    public<T> T randomEvent(ArrayList<T> array){

        Random r = new Random();
        int randomIndex = r.nextInt(array.size());
        T obj = array.get(randomIndex);
        array.remove(randomIndex);
        return obj;

    }

    public ArrayList<String> shuffle(ArrayList<String> queries){
        Collections.shuffle(queries);
        return queries;
    }

    /**
     * write the queries into a gloria.workload file
     */
    public void toFile(String workloadFile){

        ArrayList<String> shuffledQueries = shuffle(this.candidateQueries);

        try{
            File output_file = new File(workloadFile);
            if (!output_file.exists()){
                output_file.createNewFile();
            }
            BufferedWriter output = new BufferedWriter(new FileWriter(output_file));
            int i = 0;
            for(String q: shuffledQueries){
                output.append("q"+i+"\n");
                output.append(q+"\n\n");
                i++;
            }
            output.close();
        }catch (IOException e) { e.printStackTrace(); }


    }
}
