import Generators.SatOrbitProbagation;
import Graph.Graph;
import Utilty.QuantumBitTransmitanceCalculator;
import Utilty.SimValues;

import java.io.File;


public class Main {

    public static void main(String[] args) {
        SatOrbitProbagation.loadStuff();
        File dir = new File(("src/Data/InSTSM"));
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {

                    System.out.println(child.getName());
                    if (SimValues.IsSim) {
                        Graph g = new Graph();
                        g.GenerateGraph(SatOrbitProbagation.Generate(child));
                        SimValues.calc.set(new QuantumBitTransmitanceCalculator());
                        g.calculateAllTransmittance();
                        g.SaveToFileMultiDataFormat(child);
                        g.SaveToFileSingleDataFormat(child);

                }
            }
        }

    }
}
