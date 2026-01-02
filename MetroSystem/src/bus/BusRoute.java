package bus;

import infrastructure.Station;
import java.util.*;

public class BusRoute {
	private String routeName;
	private Station connectedStation; // Các stations Metro kết nối với tuyến bus
	private List<String> busStops; // Danh sách điểm dừng bus

	public BusRoute(String name, Station metroStation, List<String> stops) {
		this.routeName = name;
		this.connectedStation = metroStation;
		this.busStops = (stops != null) ? stops : new ArrayList<>();
	}

	public BusRoute(String name, Station metroStation) {
		this(name, metroStation, new ArrayList<>());
	}

	public String getRouteName() {
		return routeName;
	}

	public Station getConnectedStation() {
		return connectedStation;
	}

	public List<String> getBusStops() {
		return busStops;
	}

	public void addBusStop(String stopName) {
		if (!busStops.contains(stopName)) {
			busStops.add(stopName);
		}
	}

	@Override
	public String toString() {
		return String.format("Bus [%s] -> Metro [%s] | Stops: %s", routeName, connectedStation.getName(), busStops);
	}
}