package go.chilan.mapmatch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.graphhopper.chilango.data.MapMatchMessage;
import com.graphhopper.chilango.data.gps.GPSPoint;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.GPXEntry;

public class Server {
	public Server(int port) {

		do {
			try (ServerSocket serverSocket = new ServerSocket(port); Socket socket = serverSocket.accept()) {
	//		try (ServerSocket serverSocket = (SSLServerSocket) SSLServerSocketFactory
	//                 .getDefault().createServerSocket(port); Socket socket = serverSocket.accept();) {
				
				System.out.println("Server accepted Client");
				ObjectInputStream ois;
				ObjectOutputStream ous;

				MapMatchMessage message = null;

				do {
					ois = new ObjectInputStream(socket.getInputStream());
					ous = new ObjectOutputStream(socket.getOutputStream());
					// data
					message = (MapMatchMessage) ois.readObject();
					System.out.println("message received");

					if (message.getMessageType() == 2) {
						ois.close();
						socket.close();

					} else if (message.getMessageType() == 1) {

						MapMatchMessage returnMessage = processMessage(message);
						
						ous.writeObject(returnMessage);
						ous.flush();
					}

				} while (message.getMessageType() == 1);

			} catch (IOException | ClassCastException e) {
				e.printStackTrace();

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (true);
	}

	private MapMatchMessage processMessage(MapMatchMessage message) throws IOException {
		System.out.println("process data: point number: " + message.getGpsList().size());

		DistanceCalcEarth calc = new DistanceCalcEarth();

		List<GPXEntry> entries = new LinkedList<>();

		long time = 0L;
		long timeLast = 0L;
		double totalDistance = 0.0;
		double mediumSpeed = 0.0;
		GPSPoint oldPoint = null;

		Map<Long,GPSPoint> gpsList=new TreeMap<>(message.getGpsList());
		for (Entry<Long, GPSPoint> point : gpsList.entrySet()) {
			if (oldPoint == null) {
				time = point.getKey();
			}

			if (oldPoint != null) {
				double dist = calc.calcDist(oldPoint.lat, oldPoint.lon, point.getValue().lat,
						point.getValue().lon);
				totalDistance += dist;
			}

			entries.add(new GPXEntry(point.getValue().lat, point.getValue().lon, point.getKey()));

			timeLast = point.getKey();
			
			oldPoint=point.getValue();

		}

		mediumSpeed = totalDistance / (timeLast - time);
		System.out.print(totalDistance + " km  -  speed:" + mediumSpeed+" total time: "+(timeLast - time));
		mediumSpeed *= 0.9;
		System.out.println(" reduced speed: " + mediumSpeed);

		Map<Long, GPSPoint> matched = new TreeMap<>();

		List<GPXEntry> result = new LinkedList<>();
		
		try{
				result=MapMatch.matchOSMtoGPX(entries);
		}catch(Exception ignore)
		{
			
		}

		totalDistance = 0.0;
		oldPoint = null;
		System.out.println(" new point size: " + result.size()+" start Time: " + time);
		
		long timeL=time;
		GPXEntry oldGPXEntry=null;
		for (GPXEntry entry : result) {
			if (oldGPXEntry != null) {
				double dist = calc.calcDist(oldGPXEntry.getLat(), oldGPXEntry.getLon(), entry.getLat(),
						entry.getLon());
				timeL += dist / mediumSpeed;
				totalDistance += dist;

			}
			
			oldGPXEntry=entry;

			matched.put(timeL, new GPSPoint(entry.getLat(), entry.getLon(), entry.ele, 0.0f));
		}
		System.out.println(" final time: " + timeL+" - "+time+" = "+(timeL-time) + "  distance: " + totalDistance);

		System.out.println("new point number: " + matched.size());

		MapMatchMessage returnMessage = new MapMatchMessage(matched);

		System.out.println("message returned: " + matched.size());
		
		return returnMessage;
	}

}

