package stream;

import base.DatasetSchema;
import base.Event;
import gloriaOptimizer.Workload;
import workload.WorkloadAnalyzer;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.IllegalFormatCodePointException;
import java.util.Scanner;

/**
 * load the stream file into a arraylist of events
 */
public class streamLoader {
    protected String streamFile;
    protected DatasetSchema schema;
    protected WorkloadAnalyzer analyzer;

    protected int epw;

    public streamLoader(String streamFile, DatasetSchema schema, WorkloadAnalyzer analyzer, int epw) {
        this.streamFile = streamFile;
        this.schema = schema;
        this.analyzer = analyzer;
        this.epw = epw;

    }

    public void load() {

        try {
            Scanner scanner = new Scanner(new File(streamFile));
            scanner.nextLine();

            int eventNum =0;

            while (scanner.hasNext()) {
                if (eventNum > this.epw){
                    break;
                }
                eventNum ++;
                String line = scanner.nextLine();
                String[] data = line.split(",");

                dispatchEventToMiniWorkloads(data);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();

        }

    }


    /**
     * dispatch the events to different workload
     * @param data
     * @throws ParseException
     */
    private void dispatchEventToMiniWorkloads(String[] data) throws ParseException {
        //seq workload
        int eventTypeIndex = 0;

        if (this.schema.getDatasetName().equals("bus")){
            eventTypeIndex = 5;
        }
        if (this.schema.getDatasetName().equals("taxi")){
            eventTypeIndex = 7;
        }


        for (Workload seqW: analyzer.getSEQWorkloads().values()){

            if (seqW.getTemplate().getNameToEventTypes().containsKey(data[eventTypeIndex])){
                Event event = new Event(seqW.getTemplate().getNameToEventTypes().get(data[eventTypeIndex]), data);
                seqW.getPredicateManager().setValidQueriesForEvent(event);
                if (!event.getValidQueries().isEmpty()) {
                    event.setEventId(seqW.getSubstream().size());
                    seqW.getSubstream().add(event);

                }
            }
        }

        //single cycle workload
        for (Workload singleW: analyzer.getSingleWorkloads().values()){
            if (singleW.getTemplate().getNameToEventTypes().containsKey(data[eventTypeIndex])){
                Event event = new Event(singleW.getTemplate().getNameToEventTypes().get(data[eventTypeIndex]), data);
                singleW.getPredicateManager().setValidQueriesForEvent(event);
                if (!event.getValidQueries().isEmpty()) {
                    event.setEventId(singleW.getSubstream().size());

                    singleW.getSubstream().add(event);
                }
            }
        }

        //nested cycle workload
        for (Workload nestedW: analyzer.getNestedWorkloads().values()){
            if (nestedW.getTemplate().getNameToEventTypes().containsKey(data[eventTypeIndex])){
                Event event = new Event(nestedW.getTemplate().getNameToEventTypes().get(data[eventTypeIndex]), data);
                nestedW.getPredicateManager().setValidQueriesForEvent(event);
                if (!event.getValidQueries().isEmpty()) {
                    event.setEventId(nestedW.getSubstream().size());

                    nestedW.getSubstream().add(event);
                }
            }
        }
    }
}
