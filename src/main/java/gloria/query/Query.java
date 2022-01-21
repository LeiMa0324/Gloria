package gloria.query;

import gloria.query.aggregator.Aggregator;
import gloria.query.pattern.Pattern;
import gloria.query.predicate.Predicate;
import gloria.query.window.Window;
import gloria.gloriaOptimizer.Workload;
import lombok.Data;

import java.util.ArrayList;

/**
 * class of queries
 */
@Data
public class Query {
    private Integer id;
    protected Pattern pattern;
    protected ArrayList<Predicate> predicates;
    protected Aggregator aggregator;
    protected GroupBy groupBy;
    protected Window window;
    private Workload workload;

    public Query(Pattern pattern){
        this.pattern = pattern;
        this.predicates = new ArrayList<>();
    }


    public Query(Pattern pattern,
                 ArrayList<Predicate> predicates,
                 Aggregator aggregator,
                 GroupBy groupBy,
                 Window window){
        this.pattern = pattern;
        this.predicates = predicates;
        this.aggregator = aggregator;
        this.groupBy = groupBy;
        this.window = window;
    }


    @Override
    public String toString() {

        StringBuilder pattern = new StringBuilder(this.pattern.toString());


        StringBuilder conditions = new StringBuilder("");
        for (Predicate p: this.predicates){
            conditions.append(p.toString());
            if (this.predicates.indexOf(p)!=predicates.size()-1){
                conditions.append(" AND");
            }
        }

        return "RETURN "+this.groupBy.getAttributeName() +", "+ this.aggregator.toString()+"\n"+
                "PATTERN "+pattern.toString()+"\n"+
                "WHERE "+conditions.toString()+"\n"+
                "GROUP-BY "+groupBy.getAttributeName()+"\n"+
                window.toString();

    }
}

