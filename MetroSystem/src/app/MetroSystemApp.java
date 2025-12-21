package app;

import java.time.*;
import java.util.*;

import bus.*;
import commercial.*;
import infrastructure.*;
import operation.*;

public class MetroSystemApp {
	static TicketManager ticketManager;
	static OperationManager opManager;
	static PathFinder pathFinder;
	static Route routeL1;
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		initSystem();

		while (true) {
			System.out.println("\n========================================");
			System.out.println("    HỆ THỐNG QUẢN LÝ METRO HCM (L1)    ");
			System.out.println("========================================");
			System.out.println("1. [Operation] Xem lịch chạy & Đội tàu");
			System.out.println("2. [Operation] Theo dõi vị trí tàu");
			System.out.println("3. [Commercial] Mua vé (Tạo Order có thể mua nhiều vé)");
			System.out.println("4. [Commercial] Soát vé (Check-in / Check-out)");
			System.out.println("5. [Report] Báo cáo doanh thu (TreeMap)");
			System.out.println("6. [Advanced] Tìm đường Bus + Metro");
			System.out.println("0. Thoát");
			System.out.print(">> Chọn chức năng: ");

			int choice = scanner.nextInt();
			scanner.nextLine(); // clear buffer

			switch (choice) {
			case 1:
				opManager.showFleetStatus();
				opManager.showSchedule();
				break;
			case 2:
				System.out.print("Nhập ID chuyến (VD: TRIP-1): ");
				String tId = scanner.nextLine().trim().toUpperCase();
				System.out.print("Giả lập giờ hiện tại (VD: 7 , 7:01): ");
				String timeStr = scanner.nextLine();
				LocalTime formattedTime = parseTimeInput(timeStr);

				if (formattedTime != null)
					opManager.showTrainLocation(tId, formattedTime);
				break;
			case 3:
				handleShoppingProcess();
				break;
			case 4:
				handleGateControl(); // Tách hàm riêng cho gọn
				break;
			case 5:
				System.out.println("Chọn kiểu xem báo cáo:");
				System.out.println("1. Tăng dần (Ngày cũ -> Mới / Sáng -> Tối)");
				System.out.println("2. Giảm dần (Ngày mới -> Cũ / Tối -> Sáng)");
				int sortChoice = scanner.nextInt();
				boolean isAsc = (sortChoice == 1);

				// call method
				ticketManager.showDailyReport(isAsc);
				// In thêm chi tiết giờ của hôm nay
				ticketManager.showHourlyReport(LocalDate.now(), isAsc);
				break;
			case 6:
				System.out.println("Bạn đang ở đâu? (VD: Chợ Thủ Đức): ");
				String loc = scanner.nextLine();
				pathFinder.findPath(loc, routeL1.getStations().get(0)); // Giả sử muốn về Bến Thành
				break;
			case 0:
				System.out.println("Tạm biệt !");
				System.exit(0);
			default:
				System.out.println("Sai lệnh !");
			}
		}
	}

	// method xử lý soát vé
	static void handleGateControl() {
		System.out.println("\n--- CỔNG SOÁT VÉ ---");
		System.out.println("1. CHECK-IN (Vào ga)");
		System.out.println("2. CHECK-OUT (Ra ga)");
		System.out.print("Chọn: ");
		int c = scanner.nextInt();
		scanner.nextLine();

		System.out.print("Nhập mã vé: ");
		String ticketID = scanner.nextLine().trim();
		showStations();
		System.out.print("Chọn ga hiện tại (Index 0 - 13): ");
		int stIdx = scanner.nextInt();
		// xử lý nhập quá index
		if (stIdx < 0 || stIdx >= routeL1.getStations().size()) {
			System.out.println("Sai index ga!");
			return;
		}
		// lưu curr station
		Station currentSt = routeL1.getStations().get(stIdx);

		// gọi 2 options
		if (c == 1) {
			ticketManager.processCheckIn(ticketID, currentSt);
		} else if (c == 2) {
			ticketManager.processCheckOut(ticketID, currentSt);
		}
	}

	// method chuẩn hóa input giờ (7 -> 07:00)
	public static LocalTime parseTimeInput(String input) {
		try {
			input = input.trim();
			if (!input.contains(":")) { // tức là input "7" -> "07:00"
				int h = Integer.parseInt(input);
				return LocalTime.of(h, 0);
			}
			// còn nhập "7:30" -> LocalTime tự parse được mặc định là h:m
			// nhưng xử lý luôn
			String[] p = input.split(":");
			int h = Integer.parseInt(p[0]);
			int m = Integer.parseInt(p[1]);
			return LocalTime.of(h, m);
		} catch (Exception e) {
			System.out.println("Lỗi định dạng giờ ! Mặc định dùng giờ hiện tại");
			return LocalTime.now();
		}
	}

	// xử lý quá trình mua vé
	public static void handleShoppingProcess() {
		Order myOrder = new Order("ORD-" + System.currentTimeMillis());
		// flag check còn trong đoạn mua hay không
		boolean shopping = true;

		while (shopping) {
			System.out.println("\n--- GIỎ HÀNG: " + myOrder.getTicketCount() + " vé ---");
			System.out.println("1. Thêm vé lượt (Single)");
			System.out.println("2. Thêm vé ngày (Daily)");
			System.out.println("3. Thêm vé tháng (Monthly)");
			System.out.println("4. Thanh toán & In hóa đơn");
			System.out.print("Chọn: ");
			int c = scanner.nextInt();

			Ticket t = null;
			if (c == 1) {
				showStations();
				System.out.print("Ga đi (0-13): ");
				int s1 = scanner.nextInt();
				System.out.print("Ga đến (0-13): ");
				int s2 = scanner.nextInt();
				if (s1 >= 0 && s2 < routeL1.getStations().size())
					t = ticketManager.createTicket(TicketType.SINGLE, routeL1.getStations().get(s1),
							routeL1.getStations().get(s2));
			} else if (c == 2) {
				t = ticketManager.createTicket(TicketType.DAILY, null);
			} else if (c == 3) {
				System.out.print("Nhập ID khách hàng: ");
				String uid = scanner.next();
				t = ticketManager.createTicket(TicketType.MONTHLY, uid);
			} else if (c == 4) {
				shopping = false;
			}

			if (t != null) {
				myOrder.addTicket(t);
				System.out.println("-> Đã thêm vé: " + t.getPrice() + " VND");
			}
		}

		if (myOrder.getTicketCount() > 0) {
			ticketManager.saveOrder(myOrder); // luư order vào DB txt
			System.out.println("HÓA ĐƠN CHI TIẾT:\n" + myOrder);
		}
	}
	
	// khởi tạo hệ thống
	static void initSystem() {
		// setup stations
		routeL1 = new Route("L1");
		String[] stationNames = { "Bến Thành", "Nhà Hát TP", "Ba Son", "Văn Thánh", "Tân Cảng", "Thảo Điền", "An Phú",
				"Rạch Chiếc", "Phước Long", "Bình Thái", "Thủ Đức", "Khu Công Nghệ Cao", "ĐHQG TP.HCM", "Suối Tiên" };
		
		//setup sections
		Station prev = null;
		for (int i = 0; i < stationNames.length; i++) {
			Station curr = new Station("S" + i, stationNames[i]);
			if (prev != null) {
				// tạo đoạn đường nối 2 ga
				// giá và khoảng cách cho random
				routeL1.addSection(new Section(prev, curr, 1.5 + (i * 0.2), 5000 + (i * 1000)));
			}
			prev = curr;
		}

		// setup Operation - trains load lên từ file khi dùng constructor
		opManager = new OperationManager(routeL1);
		opManager.generateScheduleForTest(); // tự tạo lịch trình

		// setup TicketManager
		ticketManager = new TicketManager(routeL1);

		// setup Bus
		pathFinder = new PathFinder();
		// Cập nhật dùng getter getStations() cho thống nhất
		pathFinder.addBusRoute(new BusRoute("Bus 08: Chợ Thủ Đức - ĐHQG", routeL1.getStations().get(10))); // Nối ga Thủ
																											// Đức
		pathFinder.addBusRoute(new BusRoute("Bus 56: Chợ Lớn - ĐH SPKT", routeL1.getStations().get(11)));
	}
	
	// method đơn giản là show các stations để chọn
	public static void showStations() {
		String str1 = String.format("0. %-20s | 1. %-20s | 2. %-20s", "Bến Thành", "Nhà Hát TP", "Ba Son");
		String str2 = String.format("3. %-20s | 4. %-20s | 5. %-20s", "Văn Thánh", "Tân Cảng", "Thảo Điền");
		String str3 = String.format("6. %-20s | 7. %-20s | 8. %-20s", "An Phú", "Rạch Chiếc", "Phước Long");
		String str4 = String.format("9. %-20s | 10. %-20s| 11. %-20s", "Bình Thái", "Thủ Đức", "Khu Công Nghệ Cao");
		String str5 = String.format("12. %-20s| 13. %-20s|", "ĐHQG TP.HCM", "Suối Tiên");
		System.out.println(str1);
		System.out.println(str2);
		System.out.println(str3);
		System.out.println(str4);
		System.out.println(str5);

	}
}