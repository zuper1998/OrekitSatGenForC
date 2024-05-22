package Generators;

import Utilty.*;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.*;

import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static Utilty.SimValues.*;

public class SatOrbitProbagation {
    // configure Orekit


    public static void loadStuff() {
        final File home = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                    orekitData.getAbsolutePath());
            System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                    "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                    home.getAbsolutePath());
            System.exit(1);
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
        initialDate = new AbsoluteDate(2021, 1, 1, 23, 30, 00.000, TimeScalesFactory.getUTC())
                .shiftedBy(0);
    }

    public static Map<String, ArrayList<SatFlightData>> Generate(File satData) {

        //  Initial state definition : date, orbit

        final double mu = 3.986004415e+14; // gravitation coefficient
        final Frame inertialFrame = FramesFactory.getEME2000(); // inertial frame for orbit definition
        ArrayList<Satellite_Sajat> sats = Satellite_Sajat.SatLoader(
                satData.getAbsolutePath());

        Map<String, Propagator> orbits = new HashMap<>();
        for (Satellite_Sajat s1 : sats) {
            final Orbit initialOrbitE = new KeplerianOrbit(s1.a, s1.e, s1.i, s1.omega, s1.raan, s1.lM, PositionAngleType.MEAN,
                    inertialFrame, initialDate, mu);

            final Orbit initialOrbit = new EquinoctialOrbit(initialOrbitE);

            // Propagator : consider a simple Keplerian motion (could be more elaborate)
            orbits.put(s1.Name, new KeplerianPropagator(initialOrbit));
        }
        ArrayList<City> cities = SimValues.cities;

        final Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);


        Map<String, ArrayList<SatFlightData>> timelines = new HashMap<>();
        HashMap<String, AbsoluteDate> timelineHelperMap = new HashMap<>(); // absolut date is also a bool --> false if null
        HashMap<String, ArrayList<Double>> dataMap = new HashMap<>();
        HashMap<String, ArrayList<Double>> dataAngleMap = new HashMap<>();
        //prepare timelines
        for (Map.Entry<String, Propagator> p : orbits.entrySet()) {
            timelines.put(p.getKey(), new ArrayList<>());
        }
        for (City c : cities) {
            timelines.put(c.getName(), new ArrayList<>());
        }


        ArrayList<TopocentricFrame> cityFrames = new ArrayList<>();


        final BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                earthFrame);

        for (City c : cities) {
            final GeodeticPoint station1 = new GeodeticPoint(c.latitude, c.longitude, c.altitude);
            final TopocentricFrame sta1Frame = new TopocentricFrame(earth, station1, c.getName());
            cityFrames.add(sta1Frame);


        }

        final AbsoluteDate finalDate = initialDate.shiftedBy(SimValues.duration);


        for (AbsoluteDate extrapDate = initialDate;
             extrapDate.compareTo(finalDate) <= 0;
             extrapDate = extrapDate.shiftedBy(stepT)) {
            Map<String, SpacecraftState> curState = new HashMap<>();
            for (Map.Entry<String, Propagator> p : orbits.entrySet()) {

                curState.put(p.getKey(), p.getValue().propagate(extrapDate)); // Use HashMap to store the cur state

            }

            //check visibility for cities also backwards
            for (TopocentricFrame c : cityFrames) {
                // 99% the basis of the vector system is earth itself so it wont realy move xd


                for (Map.Entry<String, SpacecraftState> Sat : curState.entrySet()) {
                    SpacecraftState ss = Sat.getValue();
                    //https://www.orekit.org/mailing-list-archives/orekit-users/msg00625.html same as this,check the implementation of getElevation
                    double degree = FastMath.toDegrees(c.getElevation(ss.getPVCoordinates().getPosition(), ss.getFrame(), ss.getDate()));
                    String name = String.format("%s->%s", c.getName(), Sat.getKey());
                    String name_backwards = String.format("%s->%s", Sat.getKey(), c.getName());

                    if (degree > minAngle) {
                        double distance = c.getRange(ss.getPVCoordinates().getPosition(), ss.getFrame(), ss.getDate());

                        // ->
                        timelineHelperMap.putIfAbsent(name, extrapDate);
                        dataMap.putIfAbsent(name, new ArrayList<>());
                        dataAngleMap.putIfAbsent(name, new ArrayList<>());
                        dataMap.get(name).add(distance);
                        dataAngleMap.get(name).add(degree);
                        // <-
                        timelineHelperMap.putIfAbsent(name_backwards, extrapDate);
                        dataMap.putIfAbsent(name_backwards, new ArrayList<>());
                        dataAngleMap.putIfAbsent(name_backwards, new ArrayList<>());
                        dataMap.get(name_backwards).add(distance);
                        dataAngleMap.get(name_backwards).add(degree);


                    } else if (timelineHelperMap.get(name) != null) { // end: first time iteration when there is no visibility
                        AbsoluteDate start = timelineHelperMap.get(name);
                        // ->
                        //add SatFlight Data
                        if (!timelines.get(c.getName()).contains(new SatFlightData(Sat.getKey()))) {
                            timelines.get(c.getName()).add(new SatFlightData(Sat.getKey()));
                        }
                        int index = timelines.get(c.getName()).indexOf(new SatFlightData(Sat.getKey()));
                        timelines.get(c.getName()).get(index).addIntervalWithData(new TimeInterval(start, extrapDate), new IntervalData(dataMap.get(name), dataAngleMap.get(name)));

                        // <-
                        //add SatFlight Data
                        if (!timelines.get(Sat.getKey()).contains(new SatFlightData(c.getName()))) {
                            timelines.get(Sat.getKey()).add(new SatFlightData(c.getName()));
                        }
                        int index_back = timelines.get(Sat.getKey()).indexOf(new SatFlightData(c.getName()));
                        timelines.get(Sat.getKey()).get(index_back).addIntervalWithData(new TimeInterval(start, extrapDate), new IntervalData(dataMap.get(name_backwards), dataAngleMap.get(name_backwards)));


                        // clean helpers
                        dataMap.put(name, null);
                        dataAngleMap.put(name, null);
                        timelineHelperMap.put(name, null);
                        dataMap.put(name_backwards, null);
                        dataAngleMap.put(name_backwards, null);
                        timelineHelperMap.put(name_backwards, null);


                    }
                }


            }


            // check for visibility inter satellites
            for (Map.Entry<String, SpacecraftState> spaceState_outer : curState.entrySet()) {
                for (Map.Entry<String, SpacecraftState> spaceState_inner : curState.entrySet()) {
                    if (spaceState_inner != spaceState_outer) {
                        String name = String.format("%s->%s", spaceState_outer.getKey(), spaceState_inner.getKey());

                        if (Utility.SatVisible(spaceState_outer.getValue(), spaceState_inner.getValue())) {
                            timelineHelperMap.putIfAbsent(name, extrapDate);
                            dataMap.putIfAbsent(name, new ArrayList<>());
                            Vector3D posOuter = spaceState_outer.getValue().getPVCoordinates().getPosition();
                            Vector3D posInner = spaceState_inner.getValue().getPVCoordinates().getPosition();
                            double distance = posInner.distance(posOuter);
                            dataMap.get(name).add(distance);
                        } else if (timelineHelperMap.get(name) != null) { // end: first time iteration when there is no visibility
                            AbsoluteDate start = timelineHelperMap.get(name);

                            if (!timelines.get(spaceState_outer.getKey()).contains(new SatFlightData(spaceState_inner.getKey()))) {
                                timelines.get(spaceState_outer.getKey()).add(new SatFlightData(spaceState_inner.getKey()));
                            }
                            int index = timelines.get(spaceState_outer.getKey()).indexOf(new SatFlightData(spaceState_inner.getKey()));
                            timelines.get(spaceState_outer.getKey()).get(index).addIntervalWithData(new TimeInterval(start, extrapDate), new IntervalData(dataMap.get(name)));
                            // clean helpers
                            dataMap.put(name, null);
                            timelineHelperMap.put(name, null);
                        }
                    }
                }
            }
        }
        return timelines;
    }

}
