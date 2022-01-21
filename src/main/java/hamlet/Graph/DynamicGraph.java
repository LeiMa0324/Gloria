package hamlet.Graph;

import hamlet.Graph.tools.GraphletManager.GraphletManager_DynamicHamlet;
import hamlet.Graph.tools.Utils;
import hamlet.base.Event;
import hamlet.base.Template;
import hamlet.optimizer.DynamicOptimizer;
import hamlet.query.aggregator.Value;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;

@Data
public class DynamicGraph extends Graph{

    private final DynamicOptimizer optimizer;
    private GraphletManager_DynamicHamlet graphletManagerDynamicHamlet;
    int mergeNum;
    int splitNum;
    private long optimizerTime = 0;
    private long executionTime = 0;


    public DynamicGraph(Template template, ArrayList<Event> events, ArrayList<ArrayList<Event>> panes){
        super(template, events, panes);
        optimizer = new DynamicOptimizer();
        graphletManagerDynamicHamlet = new GraphletManager_DynamicHamlet();
//        printWorkload();
        this.utils.setGraphletManager(graphletManagerDynamicHamlet);
        this.utils.setGraphType(Utils.GraphType.DYNAMIC);


    }

    public void run() {

        this.windowManager.initAllWindows(this.events.get(0).getTimeStamp());

        long start = System.currentTimeMillis();


        for (int i = 0; i< this.panes.size(); i++){
            ArrayList<Event> burst = panes.get(i);
//            System.out.printf("Pane number: "+i+"\n");


            boolean isKleeneBurst = burst.get(0).getType().isKleene()||burst.get(0).getType().getName().equals(this.kleeneET);


            HashMap<Integer, Value> burstValues = new HashMap<>();

            if (isKleeneBurst){
                long optimizerStart = System.currentTimeMillis();
                HashMap<String, Integer> params = this.graphletManagerDynamicHamlet.getParams(burst);
                boolean shareDecision = this.optimizer.isToShare(params);
//                System.out.printf("share?" + shareDecision);

                long optimizerEnd = System.currentTimeMillis();

                optimizerTime += (optimizerEnd - optimizerStart);

                if (shareDecision){
                    long shareStart = System.currentTimeMillis();
                    burstValues = this.graphletManagerDynamicHamlet.share(burst);
                    this.memory += burst.size()*12;
                    this.mergeNum +=1;
                    long shareEnd= System.currentTimeMillis();
//                    System.out.println("shared Kleene graphlet took: "+(shareEnd-shareStart)+", burst size:"+burst.size());
                }
                if (!shareDecision){
                    long nonShareStart = System.currentTimeMillis();
                    burstValues = this.graphletManagerDynamicHamlet.split(burst);
                    this.memory += burst.size()*this.utils.getQueryIds().size()*12;
                    this.splitNum += 1;
                    long nonShareEnd = System.currentTimeMillis();

//                    System.out.println("non-Shared Kleene graphlet took: "+(nonShareEnd-nonShareStart));
                }

            }else {
                long nonShareStart = System.currentTimeMillis();
                burstValues = this.graphletManagerDynamicHamlet.newNoneSharedGraphlet(burst);
                this.memory += burst.size()*12;
                long nonShareEnd = System.currentTimeMillis();

//                System.out.println("non-Shared non-Kleene graphlet took: "+(nonShareEnd-nonShareStart));

            }

            updateFinalValues(burstValues, burst);
//            windowProcess(burst);
        }

        long end = System.currentTimeMillis();

        this.executionTime = end-start;

        this.memory += this.utils.getSnapshotManager().getSnapshots().size()*this.utils.getQueryIds().size()*12;

    }

    public void updateFinalValues(HashMap<Integer, Value> burstValues, ArrayList<Event> burst ){

        ArrayList<Integer> endQueries = Utils.getInstance().getTemplate().getQueriesEndWithEventType(burst.get(0).getType());
        if (endQueries!=null){
            ArrayList<Integer> validEndQueries = (ArrayList<Integer>) endQueries.clone();
            validEndQueries.retainAll(burst.get(0).getValidQueries());

            Value newFinalValue;

            for (int qid: validEndQueries){
                newFinalValue = this.finalValues.get(qid).add(burstValues.get(qid));
                this.finalValues.put(qid, newFinalValue);
            }
//        printFinalCount();
        }

    }

    public enum ActiveFlag{
        SPLITS,
        MERGED,
        NONSHARED
    }

}

