package gloria.query.pattern;

import lombok.Data;

@Data
public class Pattern {

    private String patternString;  //SEQ(H, SEQ(A, B, D)+, E)


    /**
     * read in a patternString and transform it into a pattern
     * @param patternString a pattern string
     */
    public Pattern(String patternString){
        this.patternString = patternString;
    }


}
