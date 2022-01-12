package Utilty;

import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SatTimeline {
    public String name;
    public Map<SatTimeline, ArrayList<AbsoluteDate>> timelineList = new HashMap<>();

    public SatTimeline(String n) {
        name = n;
    }

    void AddElement(AbsoluteDate date, SatTimeline target) {
        if (!timelineList.containsKey(target)) {
            timelineList.put(target, new ArrayList<AbsoluteDate>());
        }
        timelineList.get(target).add(date);

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SatTimeline) {
            return ((SatTimeline) o).name.equals(name);
        }
        return false;
    }




}
