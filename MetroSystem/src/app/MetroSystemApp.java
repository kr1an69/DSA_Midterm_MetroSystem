package app;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import bus.*;
import commercial.*;
import infrastructure.*;
import operation.*;
import utils.FileManager;

public class MetroSystemApp {
	static TicketManager ticketManager;
	static OperationManager opManager;
	static PathFinder pathFinder;
	static Route routeL1;
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		initSystem();

		while (true) {
			System.out.println("\n═══════════════════════════════════════════════════════════════════");
			System.out.println("           HỆ THỐNG QUẢN LÝ METRO HCM (L1)");
			System.out.println("═══════════════════════════════════════════════════════════════════");
			System.out.println("1. [Operation : Điều Hành] Xem lịch chạy & Đội tàu");
			System.out.println("2. [Operation : Điều Hành] Theo dõi vị trí tàu");
			System.out.println("3. [Commercial: Thương Mại] Mua vé (Tạo Order có thể mua nhiều vé)");
			System.out.println("4. [Commercial: Thương Mại] Soát vé (Check-in / Check-out)");
			System.out.println("5. [Commercial: Thương Mại] Hoàn vé");
			System.out.println("6. [Report    : Báo Cáo] Báo cáo doanh thu (TreeMap)");
			System.out.println("7. [Advanced  : Nâng cao] Tìm đường Bus + Metro");
			System.out.println("0. THOÁT");
			System.out.println("═══════════════════════════════════════════════════════════════════");
			System.out.print(">> Chọn chức năng: ");

			int choice = -1;
			try {
				choice = Integer.parseInt(scanner.nextLine());
			} catch (Exception e) {
			}

			switch (choice) {
			case 1:
				opManager.showFleetStatus();
				opManager.showSchedule();
				break;
			case 2:
				System.out.println("\n--- THEO DÕI VỊ TRÍ TÀU ---");
				System.out.print("Nhập ID chuyến (VD: trip-vn-01-0600 hoặc TRIP-VN-01-0600): ");
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
				handleGateControl();
				break;
			case 5:
				handleRenfund();
				break;
			case 6:
				handleReport();
				break;
			case 7:
				handlePathFinder();
				break;
			case 0:
				System.out.println("👋Tạm biệt và hẹn gặp lại");
				System.exit(0);
			default:
				System.out.println("⚠️ Lệnh không hợp lệ ⚠️");
			}
		}
	}

	// xử lý quá trình mua vé
	public static void handleShoppingProcess() {
		Order myOrder = new Order("ORD-" + System.currentTimeMillis());
		boolean shopping = true;

		while (shopping) {
			System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			System.out.println("\n--- 🛒 GIỎ HÀNG: " + myOrder.getTicketCount() + " vé ---");
			System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			System.out.println("1. Thêm vé lượt (Single)");
			System.out.println("2. Thêm vé ngày (Daily)");
			System.out.println("3. Thêm vé tháng (Monthly)");
			System.out.println("0. Thanh toán & In hóa đơn");
			System.out.print(">> Chọn: ");

			int c = -1;
			try {
				c = Integer.parseInt(scanner.nextLine());
			} catch (Exception e) {
			}

			Ticket t = null;
			if (c == 1) {
				showStations();
				System.out.print("Ga đi (0-13): ");
				int s1 = Integer.parseInt(scanner.nextLine());
				System.out.print("Ga đến (0-13): ");
				int s2 = Integer.parseInt(scanner.nextLine());
				if (s1 >= 0 && s2 < routeL1.getStations().size())
					t = ticketManager.createTicket(TicketType.SINGLE, routeL1.getStations().get(s1),
							routeL1.getStations().get(s2));
			} else if (c == 2) {
				System.out.print("Bạn có muốn nhập ID khách hàng không? (Y/N): ");
				String answer = scanner.nextLine().trim().toUpperCase();

				String dailyCusId = null; // mặc định null

				if (answer.equals("Y")) {
					System.out.print("Nhập ID khách hàng: ");
					dailyCusId = scanner.nextLine().trim();
				}

				// Truyền ID vào - vì vé Daily nên có thể để optional - ID hoặc null
				t = ticketManager.createTicket(TicketType.DAILY, dailyCusId);
			} else if (c == 3) {
				System.out.print("Nhập ID khách hàng: ");
				String uid = scanner.nextLine();
				t = ticketManager.createTicket(TicketType.MONTHLY, uid);
			} else if (c == 0) {
				shopping = false;
			}

			if (t != null) {
				myOrder.addTicket(t);
				System.out.println("✅ Đã thêm vé:");
				System.out.println("   ID: " + t.getTicketId());
				System.out.println("   Giá: " + String.format("%,.0f VND", t.getPrice()));
			}
		}

		if (myOrder.getTicketCount() > 0) {
			ticketManager.saveOrder(myOrder);
			System.out.println("HÓA ĐƠN CHI TIẾT:\n" + myOrder);
		}
	}

	// method xử lý soát vé
	private static void handleGateControl() {
		System.out.println("\n--- CỔNG SOÁT VÉ ---");
		System.out.println("1. CHECK-IN (Vào ga)");
		System.out.println("2. CHECK-OUT (Ra ga)");
		System.out.print(">> Chọn: ");
		int c = scanner.nextInt();
		scanner.nextLine();

		System.out.print("Nhập mã vé: ");
		String ticketID = scanner.nextLine().trim();
		showStations();
		System.out.print("Chọn ga hiện tại (0 - 13): ");
		int stIdx = scanner.nextInt();
		scanner.nextLine();
		// handle việc nhập quá index ga
		if (stIdx < 0 || stIdx >= routeL1.getStations().size()) {
			System.out.println("⚠️ Sai ga ⚠️");
			return;
		}
		// lưu current station
		Station currentSt = routeL1.getStations().get(stIdx);

		// gọi 2 options
		if (c == 1) {
			ticketManager.processCheckIn(ticketID, currentSt);
		} else if (c == 2) {
			ticketManager.processCheckOut(ticketID, currentSt);
		}
	}

	// method hủy vé và tìm orders có vé đó và giảm tiền để báo cáo doanh thu
	private static void handleRenfund() {
		System.out.println("\n--- HOÀN VÉ / HỦY VÉ ---");
		System.out.print("Nhập Mã Vé cần hủy (ID): ");
		String ticketId = scanner.nextLine().trim();

		if (ticketId.isEmpty()) {
			System.out.println("⚠️ Mã vé không được để trống ⚠️");
			return;
		}

		ticketManager.processRefund(ticketId);
	}

	// method report
	private static void handleReport() {
		System.out.println("\n--- BÁO CÁO DOANH THU ---");
		System.out.println("1. Báo cáo tổng hợp (Các ngày gần đây)");
		System.out.println("2. Báo cáo chi tiết (Theo ngày cụ thể)");
		System.out.print("Chọn: ");
		int reportType = Integer.parseInt(scanner.nextLine());

		if (reportType == 1) {
			// report doanh thu theo các ngày gần đây
			System.out.println("--- TÙY CHỌN SẮP XẾP ---");
			System.out.println("1. Sắp xếp theo NGÀY (Cũ/Mới)");
			System.out.println("2. Sắp xếp theo GIÁ TIỀN (Doanh thu)");
			System.out.print("Chọn tiêu chí: ");
			int sortCriteria = Integer.parseInt(scanner.nextLine());

			System.out.println("--- THỨ TỰ SẮP XẾP ---");
			System.out.println("1. Tăng dần");
			System.out.println("2. Giảm dần");
			System.out.print("Chọn thứ tự: ");
			int sortOrder = Integer.parseInt(scanner.nextLine());
			boolean isAsc = (sortOrder == 1);
			boolean sortByRevenue = (sortCriteria == 2);

			ticketManager.showDailyReport(sortByRevenue, isAsc);

		} else if (reportType == 2) {
			// report doanh thu theo ngày cụ thể - nhập ngày
			System.out.print("Nhập ngày cần xem (dd-MM-yyyy): ");
			String dateInput = scanner.nextLine();
			LocalDate dateToCheck;
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				dateToCheck = LocalDate.parse(dateInput, formatter);
			} catch (Exception e) {
				System.out.println("Lỗi định dạng ngày! (Yêu cầu dd-MM-yyyy). Dùng ngày hiện tại.");
				dateToCheck = LocalDate.now();
			}

			System.out.println("--- TÙY CHỌN SẮP XẾP ---");
			System.out.println("1. Sắp xếp theo GIỜ (Sáng/Tối)");
			System.out.println("2. Sắp xếp theo GIÁ TIỀN (Doanh thu)");
			System.out.print("Chọn tiêu chí: ");
			int sortCriteria = Integer.parseInt(scanner.nextLine());

			System.out.println("--- THỨ TỰ HIỂN THỊ ---");
			System.out.println("1. Tăng dần");
			System.out.println("2. Giảm dần");
			System.out.print("Chọn thứ tự: ");
			int sortOrder = Integer.parseInt(scanner.nextLine());
			boolean isAsc = (sortOrder == 1);
			boolean sortByRevenue = (sortCriteria == 2);

			ticketManager.showHourlyReport(dateToCheck, sortByRevenue, isAsc);
		}
	}

	// method xử lý tìm đường
	static void handlePathFinder() {
		System.out.println("\n--- TRA CỨU ĐIỂM ĐẾN & KẾT NỐI METRO ---");
		System.out.println("Hệ thống sẽ gợi ý Ga Metro gần nhất và tuyến Bus cần đi.");
		System.out.println("Ví dụ: Nhập 'KCN Sóng Thần', 'ĐH Quốc Gia', 'Dinh Độc Lập'...");

		System.out.print(">> Bạn muốn đi đến đâu: ");
		String destination = scanner.nextLine().trim();

		pathFinder.findSimpleRoute(destination);
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

	// khởi tạo hệ thống
	static void initSystem() {
		// setup stations
		routeL1 = new Route("L1");
		Map<String, Station> stationMap = FileManager.loadStations();

		// setup sections
		if (stationMap.isEmpty()) {
			System.out.println("LỖI: Không có dữ liệu Stations! Hãy kiểm tra file dataTxt/stations_db.txt");
		} else {
			FileManager.loadSections(routeL1, stationMap);
		}

		// setup Operation - trains load lên từ file khi dùng constructor
		opManager = new OperationManager(routeL1);
		opManager.generateScheduleForTest(); // tự tạo lịch trình

		// setup TicketManager
		ticketManager = new TicketManager(routeL1);

		// setup busRoutes và PathFinder
		pathFinder = new PathFinder(routeL1); // Truyền route vào
		pathFinder.busNetwork = FileManager.loadBusRoutes(routeL1);

	}

	// method đơn giản là show các stations để chọn
	public static void showStations() {
		System.out.println("\n────── DANH SÁCH GA METRO ──────");
		String fmt = "| %-2d. %-25s ";
		System.out.println("----------------------------------------------------------------------------------");
		System.out.printf(fmt + fmt + fmt + "|\n", 0, "Bến Thành", 1, "Nhà Hát TP", 2, "Ba Son");
		System.out.printf(fmt + fmt + fmt + "|\n", 3, "Văn Thánh", 4, "Tân Cảng", 5, "Thảo Điền");
		System.out.printf(fmt + fmt + fmt + "|\n", 6, "An Phú", 7, "Rạch Chiếc", 8, "Phước Long");
		System.out.printf(fmt + fmt + fmt + "|\n", 9, "Bình Thái", 10, "Thủ Đức", 11, "Khu Công Nghệ Cao");
		System.out.printf(fmt + fmt + "|\n", 12, "ĐHQG TP.HCM", 13, "Suối Tiên");
		System.out.println("----------------------------------------------------------------------------------");
//		String str1 = String.format("0. %-20s | 1. %-20s | 2. %-20s", "Bến Thành", "Nhà Hát TP", "Ba Son");
//		String str2 = String.format("3. %-20s | 4. %-20s | 5. %-20s", "Văn Thánh", "Tân Cảng", "Thảo Điền");
//		String str3 = String.format("6. %-20s | 7. %-20s | 8. %-20s", "An Phú", "Rạch Chiếc", "Phước Long");
//		String str4 = String.format("9. %-20s | 10. %-20s| 11. %-20s", "Bình Thái", "Thủ Đức", "Khu Công Nghệ Cao");
//		String str5 = String.format("12. %-20s| 13. %-20s|", "ĐHQG TP.HCM", "Suối Tiên");
//		System.out.println(str1);
//		System.out.println(str2);
//		System.out.println(str3);
//		System.out.println(str4);
//		System.out.println(str5);

	}
}