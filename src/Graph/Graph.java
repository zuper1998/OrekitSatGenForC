package Graph;

import Utilty.SatFlightData;
import Utilty.SimValues;
import accessories.DefaultValues;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Graph {

    public Map<String, Node> nodes = new HashMap<>();


    public void GenerateGraph(Map<String, ArrayList<SatFlightData>> timelineMap) {

        for (Map.Entry<String, ArrayList<SatFlightData>> tlm : timelineMap.entrySet()) {
            nodes.put(tlm.getKey(), new Node(tlm.getKey()));
        }
        // add edges
        for (Map.Entry<String, ArrayList<SatFlightData>> tlm : timelineMap.entrySet()) {
            for (SatFlightData sfd : tlm.getValue()) {
                for (int i = 0; i < sfd.Interval.size(); i++) {
                    SatFlightData.SatFlightDataRetunVal satDat = sfd.getDataAt(i);
                    nodes.get(tlm.getKey()).addEdge(nodes.get(sfd.Dest), satDat.getTimeInterval().start, satDat.getTimeInterval().end, satDat.getIntervalData());
                }
            }
        }


        //Save data to dat.ser
        FileOutputStream fos;
        try {
            fos = new FileOutputStream("src/Data/dat.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(nodes);
            oos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
        Using QSCS2.0
     */
    public void calculateAllTransmittance() {

        nodes.forEach((a, b) -> b.edges.forEach(Edge::genTransmittance));
    }

    /*
        The format is the following:
        SAT-XXX|num of lines
        transmittance values|DestSat-XXX
        .
        .
        .
        SAT-XXX|num of lines
     */
    public void SaveToFile(File satData) {
        String filename = satData.getName();
        FileWriter file = null;
        try {
            if(SimValues.cityData != "src/Data/cities.txt"){
                filename = "Test_"+filename;
            }
            file = new FileWriter( String.format("%s.satNetwork",filename ) );
            System.out.printf("Saving to file: %s.satNetwork",filename);
            BufferedWriter writer = new BufferedWriter(file);
            nodes.forEach((a,b)-> {
                //System.out.println("Beep");
                try {
                    b.SaveToFile(writer);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
