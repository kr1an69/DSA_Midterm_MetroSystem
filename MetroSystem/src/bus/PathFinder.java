package bus;

import java.util.*;
import infrastructure.*;

public class PathFinder {
	public List<BusRoute> busNetwork = new ArrayList<>();
	private Route metroRoute; // Thêm để check ga Metro

	
	public PathFinder(Route route) {
		this.metroRoute = route;
	}

	public void addBusRoute(BusRoute b) {
		busNetwork.add(b);
	}
	
	//ban đầu e có dự định làm chức năng này là quá trình có thể nhập
	//vị trí đang đứng và vị trí muốn đi đến, sau đó thử làm bằng thuật toán dijkstra, 
	//tạo inner class gồm node và edge và sau đấy lấy data sections và 
	//các busroutes build graph dựa vào 2 trọng số là giá tiền và khoảng cách
	//nhưng có 1 vấn đề là Hồ Chí Minh quá nhiều route, sau đó mỗi route lại dừng ở các trạm
	//và các trạm metro quá nhiều, thành ra toàn bộ code quá phức tạp và e cảm giác
	//gần như là không thể làm hoặc do e chưa đủ trình để làm nên e đã
	//chuyển qua làm mô phỏng bằng các thoạt toán search đơn giản hơn và user đơn giản nhập
	//vị trí muốn đến và show ra các bước đến 
	
	public void findSimpleRoute(String destinationInput) {
		if (destinationInput == null || destinationInput.trim().isEmpty()) {
			System.out.println("⚠️ Vui lòng nhập địa điểm hợp lệ ⚠️");
			return;
		}

		String searchKey = destinationInput.toLowerCase().trim();

		System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		System.out.println("TÌM ĐƯỜNG ĐẾN: \"" + destinationInput + "\"");
		System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		// 1. check xem input có phải là ga Metro không
		Station metroMatch = checkIfMetroStation(searchKey);
		if (metroMatch != null) {
			System.out.println("✅ ĐÃ TÌM THẤY!");
			System.out.println("📍 Điểm đến: " + metroMatch.getName());
			System.out.println("🚇 Đây là ga Metro - đi Metro trực tiếp!\n");
			return;
		}

		// 2. check trong Bus Routes 
		List<RouteMatch> matches = findBusMatches(searchKey);

		if (matches.isEmpty()) {
			System.out.println("⚠️ Không tìm thấy tuyến nào đến địa điểm này.");
			// đoạn này gợi ý cách nhập
			System.out.println("💡 Gợi ý: Thử 'Chợ Thủ Đức', 'ĐHBK', 'KCN Sóng Thần'...\n");
			return;
		}

		// 3. show kết quả
		System.out.println("✅ TÌM THẤY " + matches.size() + " TUYẾN:\n");

		for (int i = 0; i < matches.size(); i++) {
			RouteMatch match = matches.get(i);
			System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			System.out.println("LỰA CHỌN " + (i + 1) + ":");
			System.out.println("   🚇 Bước 1: Đi Metro đến ga " + match.metroStation);
			System.out.println("   🚌 Bước 2: Đón xe Bus [" + match.busRoute + "]");
			System.out.println("   📍 Bước 3: Xuống tại " + match.matchedStops.get(0));

			// Nếu có nhiều điểm dừng trùng, hiển thị thêm
			// cụ thể hơn thì logic này xử lý việc user nhập địa điểm 1 cách chung chung
			// sau đó nó tìm các điểm dừng match với các key mà user nhập
			// và gợi ý thêm để đảm bảo bao quát và đầy đủ
			if (match.matchedStops.size() > 1) {
				System.out.println("      (Bus này cũng đi qua: "
						+ String.join(", ", match.matchedStops.subList(1, match.matchedStops.size())) + ")");
			}
		}

		System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
	}

	// method giúp kiểm tra xem input có phải là ga Metro không
	private Station checkIfMetroStation(String input) {
		if (metroRoute == null)
			return null;

		for (Station s : metroRoute.getStations()) {
			String stationName = s.getName().toLowerCase();
			if (stationName.contains(input) || input.contains(stationName)) {
				return s;
			}
		}
		return null;
	}

	// method tìm tất cả Bus matches (nhóm theo tuyến)
	private List<RouteMatch> findBusMatches(String searchKey) {
		List<RouteMatch> results = new ArrayList<>();

		for (BusRoute bus : busNetwork) {
			List<String> matchedStops = new ArrayList<>();

			// Tìm tất cả điểm dừng match trong tuyến này
			for (String stop : bus.getBusStops()) {
				if (stop.toLowerCase().contains(searchKey)) {
					matchedStops.add(stop);
				}
			}

			// Nếu có match, thêm vào kết quả (1 tuyến = 1 kết quả)
			if (!matchedStops.isEmpty()) {
				results.add(new RouteMatch(bus.getConnectedStation().getName(), bus.getRouteName(), matchedStops));
			}
		}

		return results;
	}

	// Inner class để lưu thông tin match
	private class RouteMatch {
		String metroStation;
		String busRoute;
		List<String> matchedStops;

		RouteMatch(String metro, String bus, List<String> stops) {
			this.metroStation = metro;
			this.busRoute = bus;
			this.matchedStops = stops;
		}
	}
}