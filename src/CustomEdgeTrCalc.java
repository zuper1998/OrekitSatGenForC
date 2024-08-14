import Generators.SatOrbitProbagation;
import Graph.Graph;
import Utilty.QuantumBitTransmitanceCalculator;
import Utilty.SimValues;
import accessories.DefaultValues;

import java.io.File;

public class CustomEdgeTrCalc {

    public static void main(String[] args) {

        DefaultValues.apertureDiameter = 0.130;//mm
        DefaultValues.mirrorDiameter = 2.0;//mm
        DefaultValues.waveLength = 1060;
        DefaultValues.absorptionAndScatteringPath ="src/Data/asv_1060.csv";


        SatOrbitProbagation.loadStuff();
        File dir = new File(("src/Data/IAC24_Files"));
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
