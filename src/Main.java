import Generators.SatOrbitProbagation;
import Graph.Graph;
import Utilty.QuantumBitTransmitanceCalculator;
import Utilty.SimValues;


public class Main {

    public static void main(String[] args){
        SatOrbitProbagation.loadStuff();

        if (SimValues.IsSim) {
            Graph g = new Graph();
            g.GenerateGraph(SatOrbitProbagation.Generate());
            SimValues.calc.set(new QuantumBitTransmitanceCalculator());
            g.calculateAllTransmittance();
            g.SaveToFile();
        }
    }
}
