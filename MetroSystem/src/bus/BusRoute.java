package bus;

import infrastructure.Station;

public class BusRoute {
	public String routeName;
	public Station connectedStation; // Tuyến bus này nối với ga Metro nào
    
    public BusRoute(String name, Station s) {
        this.routeName = name;
        this.connectedStation = s;
    }
}
