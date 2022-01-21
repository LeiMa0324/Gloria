package gloria.gloriaOptimizer.costModel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class ExistingCostExpr {
    private Double aDouble =0.0;
    private ArrayList<Boolean> variables;
    private ArrayList<Integer> coeffs;
    public boolean Infinity = false;

    public ExistingCostExpr(){
        this.variables = new ArrayList<>();
        this.coeffs = new ArrayList<>();
    }

    public ExistingCostExpr(Double aDouble, ArrayList<Boolean> variables, ArrayList<Integer> coeffs){
        this.aDouble = aDouble;
        this.variables = variables;
        this.coeffs = coeffs;
    }


    public Double evaluate(ArrayList<Double> variableValues){
        Double res = 0.0;
        for (int i =0; i<variables.size();i++){

            res += variableValues.get(i) * coeffs.get(i);

        }

        return res+aDouble;
    }


    public Double evaluateWithZeros(){

        return aDouble;
    }

    public ExistingCostExpr sum(Double weight){
        ArrayList<Boolean> newVariables = new ArrayList<>();
        ArrayList<Integer> newCoeffs = new ArrayList<>();
        newVariables = new ArrayList<>(this.variables);
        newCoeffs = new ArrayList<>(this.coeffs);

        Double newADouble = this.aDouble+weight;
        return new ExistingCostExpr(newADouble, newVariables, newCoeffs);

    }

    public ExistingCostExpr sum(ExistingCostExpr expr){
        ArrayList<Boolean> newVariables = new ArrayList<>();
        ArrayList<Integer> newCoeffs = new ArrayList<>();

        if (this.variables.size()==0){
            newVariables = new ArrayList<>(expr.variables);
            newCoeffs = new ArrayList<>(expr.coeffs);
        }

        if (expr.variables.size()==0){
            newVariables = new ArrayList<>(this.variables);
            newCoeffs = new ArrayList<>(this.coeffs);

        }

        if (expr.variables.size()==this.variables.size()) {

            for (int i = 0; i < Math.max(variables.size(), expr.variables.size()); i++) {
                newVariables.add(variables.get(i) || expr.getVariables().get(i));
                newCoeffs.add(this.coeffs.get(i) + expr.getCoeffs().get(i));
            }
        }

        Double newADouble = this.aDouble+expr.aDouble;
        return new ExistingCostExpr(newADouble, newVariables, newCoeffs);
    }

    public boolean isGreater(ExistingCostExpr expr){
        if (this.isInfinity()) return true;
        if (expr.isInfinity()) return false;
        return doubleGreater(expr)&&exprGreater(expr);
    }

    public boolean doubleGreater(ExistingCostExpr expr){
        return this.aDouble>expr.aDouble;
    }

    public boolean exprGreater(ExistingCostExpr expr){
        if (this.variables.size()==0){return false;}

        if (expr.variables.size() != 0){
            for (int i =0; i< Math.max(variables.size(), expr.variables.size()); i++){
                if (coeffs.get(i)<expr.coeffs.get(i)){
                    return false;
                }
            }
        }

        return true;
    }



}
