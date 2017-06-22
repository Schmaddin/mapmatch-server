package go.chilan.mapmatch;

import java.util.LinkedList;
import java.util.List;

import com.graphhopper.matching.GPXFile;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSMConverter;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;

public class MapMatch {
	private static GraphHopperOSMConverter hopper;
	private static MapMatching mapMatching;

	private static CarFlagEncoder encoder;
	static {
		hopper = new GraphHopperOSMConverter();
		// hopper.set
		// File file = JFileDataStoreChooser.showOpenFile("osm", null);
		hopper.setOSMFile("map-data/cdmx.osm");// file.getAbsolutePath());
		// hopper.setOSMFile("map-data/cadolzburg-original.osm");
		hopper.setGraphHopperLocation("graph/");
		encoder = new CarFlagEncoder();
		hopper.setEncodingManager(new EncodingManager(encoder));
		hopper.getCHFactoryDecorator().setEnabled(false);
		hopper.importOrLoad();
		
		AlgorithmOptions opts = AlgorithmOptions.start().algorithm(Parameters.Algorithms.DIJKSTRA_BI)
				.traversalMode(hopper.getTraversalMode()).weighting(new FastestWeighting(encoder)).
				// Penalizing inner-link U-turns only works with fastest
				// weighting, since
				// shortest weighting does not apply penalties to unfavored
				// virtual edges.
				hints(new HintsMap().put("weighting", "fastest").put("vehicle", encoder.toString())).build();
		 mapMatching = new MapMatching(hopper, opts);
		
		System.out.println("hopper setted");
	}

	synchronized static public List<GPXEntry> matchOSMtoGPX(List<GPXEntry> inputGPXEntries) {

		mapMatching.setMeasurementErrorSigma(50.0);

		MatchResult mr = mapMatching.doWork(inputGPXEntries);

		GPXFile gpx=new GPXFile(mr, null);
		gpx.doExport("export.gpx");

		return gpx.getEntries();
	}
	
	synchronized static public List<List<GPXEntry>> breakMatchOSMtoGPX(List<GPXEntry> inputGPXEntries) {

		List<List<GPXEntry>> input=new LinkedList<>();
		List<List<GPXEntry>> output=new LinkedList<>();
		
		mapMatching.setMeasurementErrorSigma(50.0);

		MatchResult mr = mapMatching.doWork(inputGPXEntries);

		GPXFile gpx=new GPXFile(mr, null);
		gpx.doExport("export.gpx");

		return output;
	}

	public static void init() {
		
	}
}
