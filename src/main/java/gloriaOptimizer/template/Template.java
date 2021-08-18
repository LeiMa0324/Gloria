package gloriaOptimizer.template;

import lombok.Data;
import query.Query;
import users.FrequencyGetter;
import gloriaOptimizer.template.EventType.EndEventType;
import gloriaOptimizer.template.EventType.EventType;
import gloriaOptimizer.template.EventType.RealEventType;
import gloriaOptimizer.template.EventType.StartEventType;
import gloriaOptimizer.Workload;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Data

public class Template {
    private HashMap<String, RealEventType> nameToEventTypes;

    private HashMap<String, StartEventType> starts;
    private HashMap<String, EndEventType> ends;
    private Workload workload;



    public Template(){
        this.nameToEventTypes = new HashMap<>();
        this.starts = new HashMap<>();
        this.ends = new HashMap<>();
    }

    /**
     * load the GloriaOptimizer.template from a workload
     * @param workload
     */
    public void loadFromWorkload(Workload workload){
        this.workload = workload;
        for (Query q: this.workload.getQueries()){

            String simplified = q.getPattern().getPatternString().replace("SEQ","");
            String[] data = simplified.replace(" ","").split(",");

            //first event type
            String firstETName = data[0].replace("(","").replace(")","").replace("+","");

            //find the start
            StartEventType startEventType = this.starts.containsKey(firstETName)?this.starts.get(firstETName):new StartEventType(workload.getSchema());

            //connect with start
            EventType last = recursiveParsePattern(startEventType, data, q.getId());

            //maintain start
            this.starts.put(firstETName, startEventType);

            //find the end
            EndEventType endEventType = this.ends.containsKey(last.getName())?this.ends.get(last.getName()):new EndEventType(workload.getSchema());

            //connect with end
            last.addSEQSuccessor(endEventType, q.getId());

            //maintain end
            this.ends.put(last.getName(), endEventType);
        }

    }

    /**
     * parse a nested pattern recursively
     * @param p_et the predecessor event type of the pattern
     * @param pattern the pattern
     * @param qid the query id
     * @return the last event type of the pattern
     */
    public EventType recursiveParsePattern(EventType p_et, String[] pattern, int qid){
        ArrayList<String[]> res = breakDownNestedPattern(pattern);
        String[] prefix = res.get(0);
        String[] innerPattern = res.get(1);
        String[] suffix = res.get(2);


        EventType last;

        //flat pattern
        if (Arrays.equals(innerPattern, pattern)){
            last = parseFlatPattern(p_et,pattern,qid );
        }else {
            EventType lastOfPrefix = parseFlatPattern(p_et, prefix, qid);
            EventType lastOfInner = recursiveParsePattern(lastOfPrefix, innerPattern, qid);
            last = parseFlatPattern(lastOfInner, suffix, qid);

        }

        //add kleene feedback edge
        if (pattern[pattern.length-1].endsWith("+")){
            EventType first = this.getNameToEventTypes().get(pattern[0].replace("(",""));
            last.addKleeneSuccessor(first, qid);

            //strip the "+" after its processed
            String lastString = pattern[pattern.length-1];
            lastString = lastString.substring(0, lastString.length()-1);
            pattern[pattern.length-1] = lastString;
        }

         return last;
    }

    /**
     * parse a flat pattern
     * @param p_et the predecessor event type of the pattern
     * @param flatPattern the flat pattern
     * @param qid the query id
     * @return the last event type of the pattern
     */
    public EventType parseFlatPattern(EventType p_et,String[] flatPattern, int qid){

        if (flatPattern==null||flatPattern.length==0){
            return p_et;
        }

        //temp array for pred
        ArrayList<RealEventType> eventTypes = new ArrayList<>();

        for (String e: flatPattern){
            //remove  (, ) or +
            String etName = e.replace("(","").replace(")","").replace("+","" );
            RealEventType newET =this.nameToEventTypes.containsKey(etName)? this.nameToEventTypes.get(etName):
                    new RealEventType(etName, this.workload.getSchema());

            //set frequency
            newET.setFrequency(getFrequency(newET.getName()));

            EventType pred = eventTypes.isEmpty()?p_et: eventTypes.get(eventTypes.size()-1);

            //maintain the SEQ edge
            newET.addSEQPredecessor(pred, qid);
            eventTypes.add(newET);


            //add new event type to GloriaOptimizer.template
            this.nameToEventTypes.put(etName, newET);
        }
        //return the last event type
        return eventTypes.get(eventTypes.size()-1);

    }



    /**
     * @Assumption only one inner pattern
     * break down the nested pattern into prefix, inner pattern, suffix
     * @param nestedPattern the nested pattern
     * @return prefix, inner pattern, suffix
     */
    public ArrayList<String[]> breakDownNestedPattern(String[] nestedPattern){

        int leftBraceIndex = -1;   // (A, B, (C, D)+)
        int rightBraceIndex = nestedPattern.length;   // (A, B, (C, D)+)

        String[] prefix = {};
        String[] innerPattern = {};
        String[] suffix = {};
        ArrayList<String[]> result = new ArrayList<>();

        //strip "("
        nestedPattern[0] = nestedPattern[0].startsWith("(")?nestedPattern[0].substring(1):nestedPattern[0];
        //strip ")"
        String lastString = nestedPattern[nestedPattern.length-1];
        if (lastString.endsWith(")")){
            lastString = lastString.substring(0, lastString.length()-1);
        }else {
            if (lastString.endsWith(")+")){
                lastString = lastString.substring(0, lastString.length()-2)+"+";

            }
        }

        nestedPattern[nestedPattern.length-1] = lastString;



        //find the biggest inner nested pattern (C, D)+
        for (int i =0; i< nestedPattern.length; i++){
            if (nestedPattern[i].startsWith("(")) leftBraceIndex = i;
            if (leftBraceIndex!=-1) break;
        }


        for (int j = nestedPattern.length-1; j>=0; j --){
            if (nestedPattern[j].endsWith(")") ||nestedPattern[j].endsWith(")+")) rightBraceIndex = j;
            if (rightBraceIndex!= nestedPattern.length) break;
        }


        prefix =  Arrays.copyOfRange(nestedPattern, 0, Math.max(leftBraceIndex, 0));
        innerPattern = Arrays.copyOfRange(nestedPattern, Math.max(leftBraceIndex, 0), Math.min(rightBraceIndex+1, nestedPattern.length));
        suffix = rightBraceIndex<nestedPattern.length-1?Arrays.copyOfRange(nestedPattern,rightBraceIndex+1, nestedPattern.length):
        null;

        result.add(prefix);
        result.add(innerPattern);
        result.add(suffix);

        return result;
    }

    private Integer getFrequency(String eventTypeName){


        return FrequencyGetter.get(eventTypeName, this.workload.getSchema().getDatasetName());

    }
}
