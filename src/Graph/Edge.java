package Graph;


import Utilty.IntervalData;

import Utilty.SimValues;
import org.hipparchus.util.FastMath;
import org.orekit.time.AbsoluteDate;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

import static Utilty.SimValues.stepT;

public class Edge implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Node start;
    public Node end;
    EdgeData data;

    public Node getEndNode() {
        return end;
    }

    public Node getStartNode() {
        return start;
    }

    /**
     * @param s   Start node
     * @param e   End Node
     * @param ds  Start date
     * @param de  End Date
     * @param dat Data for the edge timeline
     */
    public Edge(Node s, Node e, AbsoluteDate ds, AbsoluteDate de, IntervalData dat) {
        start = s;
        end = e;
        data = new EdgeData(ds, de, dat);
    }

    /**
     * @param e The Edge that should be copied
     */
    public Edge(Edge e) {
        start = e.start;
        end = e.end;
        data = new EdgeData(e.data);

    }



    public void genTransmittance() {
        getOrbitData().Transmittance = new ArrayList<>();
        if (start.isCity() || end.isCity()) { //its a cit
            for (int i = 0; i < getOrbitData().Angle.size(); i++) {
                double a = getOrbitData().Angle.get(i);
                double d = getOrbitData().Distance.get(i);
                int dir = 0;
                if (end.isCity())
                    dir = 2;
                double tr = SimValues.calc.get().calculateTransmitanceCity(a, d * FastMath.sin(FastMath.toRadians(a)), dir) * stepT;
                getOrbitData().Transmittance.add(tr);
            }
        } else {
            for (Double d : getOrbitData().Distance) {
                double tr = SimValues.calc.get().calculateTransmitanceSat(d) * stepT;
                getOrbitData().Transmittance.add(tr);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge outer = ((Edge) o);
            return outer.start.equals(start) && outer.end.equals(end) && outer.data.equals(data);
        }
        return false;
    }



    public IntervalData getOrbitData() {
        return data.orbitData;
    }

    public void SaveToFile(BufferedWriter writer) throws IOException {

        double start = data.start.durationFrom(SimValues.initialDate);
        double end = data.end.durationFrom(SimValues.initialDate);

        StringBuilder transmittance_values = new StringBuilder();
        int i = 0;
        for(double t : getOrbitData().Transmittance){
            transmittance_values.append(t);
            //Dont put  ',' after last character
            if(++i<getOrbitData().Transmittance.size())
                transmittance_values.append(",");
        }
        writer.write( String.format("start: %f | end: %f | NODE_FROM: %s | NODE_TO: %s | DATA: %s\n",
                start,end, getStartNode().name,getEndNode().name, transmittance_values));

    }
    public void SaveToFileNewFormat(BufferedWriter writer) throws IOException {
        StringBuilder transmittance_values = new StringBuilder();
        double sum_data = 0;
        double start = data.start.durationFrom(SimValues.initialDate);
        double end = data.end.durationFrom(SimValues.initialDate);
        for(double t : getOrbitData().Transmittance) {
            sum_data+=t;
        }


        writer.write( String.format("start: %f | end: %f | NODE_FROM: %s | NODE_TO: %s | DATA: %f\n",
                start,end, getStartNode().name,getEndNode().name, sum_data ));

    }

    private static class EdgeData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        public AbsoluteDate start;
        public AbsoluteDate end;
        public double duration;
        public IntervalData orbitData;

        public EdgeData(EdgeData ed) {
            start = ed.start;
            end = ed.end;
            duration = ed.duration;
            orbitData = new IntervalData(ed.orbitData);
        }

        /**
         * @param s   Start date
         * @param e   End date
         * @param dat Data for the timeline
         */
        public EdgeData(AbsoluteDate s, AbsoluteDate e, IntervalData dat) {
            start = s;
            end = e;
            duration = e.durationFrom(s);
            orbitData = dat;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof EdgeData) {
                EdgeData outer = ((EdgeData) o);
                return outer.start.equals(start) && outer.end.equals(end);
            }
            return false;
        }


    }
}
