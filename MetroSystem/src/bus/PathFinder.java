package bus;
import java.util.*;

import infrastructure.Station;
public class PathFinder {
	public List<BusRoute> busNetwork = new ArrayList<>();

	public void addBusRoute(BusRoute b) {
		busNetwork.add(b);
	}

	public void findPath(String currentLocation, Station destStation) {
		System.out.println("\nĐang tìm đường từ: " + currentLocation + " đến Metro " + destStation.getName());
		// Logic giả lập đơn giản
		for (BusRoute b : busNetwork) {
			// Giả sử tên tuyến bus chứa địa điểm
			if (currentLocation.contains("Thủ Đức") && b.routeName.contains("Thủ Đức")) {
				System.out.println("GỢI Ý: Bắt xe buýt [" + b.routeName + "]");
				System.out.println("   -> Xuống tại Ga Metro: " + b.connectedStation.getName());
				System.out.println("   -> Đi tàu Metro tới: " + destStation.getName());
				return;
			}
		}
		System.out.println("Không tìm thấy tuyến xe buýt phù hợp.");
	}
}
