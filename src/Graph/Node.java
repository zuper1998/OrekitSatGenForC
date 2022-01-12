package Graph;

import Utilty.City;
import Utilty.IntervalData;
import Utilty.SimValues;
import org.orekit.time.AbsoluteDate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    ArrayList<Edge> edges = new ArrayList<>();
    public String name;
    public Node(String n){
        name = n;
    }
    public void addEdge (Node e, AbsoluteDate ds, AbsoluteDate de, IntervalData dat) {
        Edge toAdd = new Edge(this,e,ds,de,dat);
        if(!edges.contains(toAdd))
        edges.add(toAdd);
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            return ((Node)o).name.equals(name);
        }
        return false;
    }
    public boolean stringEquals(Node n){
        return name.equals(n.name);
    }

    public void SaveToFile(BufferedWriter writer) throws IOException {
        writer.write( String.format("%s|%d%n",name,edges.size()));
        edges.forEach(e -> {
            try {
                e.SaveToFile(writer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public boolean isCity() {
        for(City c : SimValues.cities){
            if(c.getName().equals(name)){
                return true;
            }
        }
        return false;
    }
}
