package commercial;

import java.time.LocalDate;
import java.util.*;

import infrastructure.Route;
import infrastructure.Station;
import utils.FileManager;

//thằng này quản lý việc mua bán khởi tạo vé và orders -> lưu vào file DB txt ngay sau khi tạo
//báo cáo các doanh thu theo ngày, theo giờ
public class TicketManager {
	// HashMap - lưu vé có key và value là vé, tốc độ truy xuất cao
	private Map<String, Ticket> ticketDB;// = new HashMap<>();
	private Map<String, Customer> customersDB;// = new HashMap<>();
	// ArrayList - lưu là auto lưu cuối -> thứ tự thời gian
	// việc duyệt cũng nhanh -> tăng tốc độ tính tổng vé và show info trong ngày
	// và tránh tốn bộ nhớ lưu pointer node như LinkedList
	private List<Order> ordersDB;// = new ArrayList<>();
	private Route route;

	public TicketManager(Route r) {
		this.route = r;
		// load db theo thứ tự tránh sai sót
		// cus trước
		this.customersDB = FileManager.loadCustomers();

		// tickets
		this.ticketDB = FileManager.loadTickets(route, customersDB);

		// orders
		this.ordersDB = FileManager.loadOrders(ticketDB);
	}

	// checkin khi lên tàu
	public void checkInTicket(String ticketId) {
		ticketDB.get(ticketId).useTicket();
		;
	}

	public void cancelTicket(String ticketID) {
		ticketDB.get(ticketID).cancelTicket();
	}

	// METHODS tạo vé để bán
	// vé lượt
	public Ticket createTicket(TicketType type, Station start, Station end) {
		if (type != TicketType.SINGLE)
			return null;
		String id = "SGL-" + System.nanoTime();
		double price = route.calTotalPrice(start, end);
		return new SingleTicket(id, price, start, end);
	}

	// vé ngày/tháng
	public Ticket createTicket(TicketType type, String customerId) {
		String id = (type == TicketType.DAILY ? "DAY-" : "MTH-") + System.nanoTime();
		if (type == TicketType.DAILY) {
			return new DailyTicket(id, 40000); // giá fix 40k
		} else {
			// Tìm Object Customer từ id nhập vào
			Customer c = customersDB.get(customerId);
			if (c == null) {
				// Nếu chưa có thì tạo mới - để unknow và new guest đơn giản hóa (khách mới)
				c = new Customer(customerId, "Unknown", "New Guest");
				customersDB.put(customerId, c);

				// save id khách mới vô file DB
				FileManager.saveCustomer(c);
				System.out.println("[SYSTEM] Đã tạo và lưu khách hàng mới: " + customerId);
			}
			return new MonthlyTicket(id, 200000, c); // giá fix 200k
		}
	}

	// METHODS lưu và load data tickets, orders từ file DB txt
	public void saveOrder(Order order) {
		// vẫn add vô để báo cáo nhanh, này là lưu trong code tức là RAM
		this.ordersDB.add(order);

		// Lưu vé của Order vào Databse tickets và đổi trạng thái
		for (Ticket t : order.getTickets()) {
			t.payTicket(); // PAID
			ticketDB.put(t.getTicketId(), t); // lưu vào MAP trong lúc chạy code test (RAM)

			// auto save tickets vào file DB
			FileManager.saveTicket(t);
		}
		System.out.println("[SYSTEM] Đã lưu Order " + order.getOrderId() + " vào hệ thống.");

		// autosave order vào file DB
		FileManager.saveOrder(order);
	}

	// checkin
	public boolean processCheckIn(String ticketId, Station currentStation) {
		Ticket t = ticketDB.get(ticketId);
		if (t == null) {
			System.out.println("LỖI: Không tìm thấy vé!");
			return false;
		}

		if (t.isValidEntry(currentStation)) {
			t.setStatus(TicketStatus.IN_TRIP); // đổi status
			System.out.println("CHECK-IN THÀNH CÔNG: Mời vào ga " + currentStation.getName());
			return true;
		} else {
			System.out.println("CHECK-IN THẤT BẠI: Vé không hợp lệ tại ga này hoặc chưa thanh toán!");
			return false;
		}
	}

	// checkout
	public boolean processCheckOut(String ticketId, Station currentStation) {
		Ticket t = ticketDB.get(ticketId);
		if (t == null) {
			System.out.println("LỖI: Không tìm thấy vé!");
			return false;
		}

		if (t.isValidExit(currentStation)) {
			t.setStatus(TicketStatus.USED); // đổi status vé đã sử dụng
			System.out.println("CHECK-OUT THÀNH CÔNG: Cảm ơn quý khách. Hẹn gặp lại !");
			return true;
		} else {
			// Logic phạt tiền có thể thêm ở đây
			System.out.println("CHECK-OUT THẤT BẠI: Bạn đi sai ga hoặc chưa check-in đầu vào!");
			return false;
		}
	}

	// REPORTING (TreeMap)
	// Các methods trả về TreeMap cơ bản - tính toán
	// thống kê doanh thu trong ngày
	public TreeMap<Integer, Double> getHourlyRevenueStats(LocalDate dateToCheck) {
		TreeMap<Integer, Double> stats = new TreeMap<Integer, Double>();
		for (Order order : ordersDB) {
			if (order.getOrderDate().toLocalDate().equals(dateToCheck)) {
				int hour = order.getOrderDate().getHour();
				// put vào TreeMap
				// stats.getOrDefault ở đây là tìm cùng key hour check xem
				// ở key hour đó đã có value chưa, chưa thì để mặc định là 0.0
				// nếu rồi thì lấy cái value (giá) cũ và cộng thêm giá order hiện tại
				// không nhét thẳng chỗ 0.0 getTotalPrice nếu không sẽ dính ghi đè (overwrite)
				// hệ cộng dồn - acccumulation
				stats.put(hour, stats.getOrDefault(hour, 0.0) + order.getTotalPrice());
			}
		}
		return stats;
	}

	// thống kê doanh thu theo ngày (Sắp xếp ngày tăng dần)
	public TreeMap<LocalDate, Double> getDailyRevenueStats() {
		TreeMap<LocalDate, Double> stats = new TreeMap<>();
		for (Order ord : ordersDB) {
			LocalDate date = ord.getOrderDate().toLocalDate();
			// logic tương tự ở method hourly
			stats.put(date, stats.getOrDefault(date, 0.0) + ord.getTotalPrice());
		}
		return stats;
	}

	// Lấy các đơn hàng giá trị cao (Sắp xếp theo giá tiền tăng dần)
	public TreeMap<Double, Order> getHighValueOrders() {
		TreeMap<Double, Order> stats = new TreeMap<>();
		for (Order ord : ordersDB) {
			stats.put(ord.getTotalPrice(), ord);
		}
		return stats;
	}
	// END - Các methods trả về TreeMap cơ bản - tính toán

	// Các methods show ra màn hình - báo cáo
	// với boolean nếu true trả mặc định là tăng giần - false giảm dần
	// bàn sơ qua thì có khá nhiều cách để show dạng giảm dần
	// 1. descendingMap() -> đơn giản nhanh
	// 2. trả cặp key trong map ra thành list và duyệt ngược bằng for hoặc reverse()
	// 3. dùng comparator khi decalre map
	public void showHourlyReport(LocalDate dateToShow, boolean isAscending) {
		TreeMap<Integer, Double> stats = getHourlyRevenueStats(dateToShow);
		List<Integer> sortedKeys = new ArrayList<>(stats.keySet());
		// cách gọn
		/*
		 * Map<Integer, Double> viewMap = isAscending ? stats : stats.descendingMap();
		 * for (Map.Entry<Integer, Double> entry : viewMap.entrySet()) {
		 * System.out.printf("   -> Khung giờ %02d:00 : %,12.0f VND\n", entry.getKey(),
		 * entry.getValue()); }
		 */
		if (!isAscending) {
			int sizeSortedKeys = sortedKeys.size();
			for (int i = 0; i < sizeSortedKeys / 2; i++) {
				Integer tmpHour = sortedKeys.get(i);
				sortedKeys.set(i, sortedKeys.get(sizeSortedKeys - 1 - i));
				sortedKeys.set(sizeSortedKeys - 1 - i, tmpHour);
			}
		}
		System.out.println("\n=== DOANH THU CHI TIẾT NGÀY: " + dateToShow + " ===");
		if (stats.isEmpty()) {
			System.out.println("Không có giao dịch nào trong ngày này");
			return;
		}

		double totalPrice = 0;
		for (Integer hour : sortedKeys) {
			System.out.printf("   -> Khung giờ %02d:00 : %,12.0f VND\n", hour, stats.get(hour));
		}
		System.out.println("------------------------------------------------");
		System.out.printf("   TỔNG CỘNG      : %,15.0f VND\n", totalPrice);

	}

	// với boolean nếu true trả mặc định là tăng giần - false giảm dần
	public void showDailyReport(boolean isAscending) {
		// Gọi hàm tính toán để lấy TreeMap
		TreeMap<LocalDate, Double> stats = getDailyRevenueStats();
		List<LocalDate> sortedKeys = new ArrayList<LocalDate>(stats.keySet());
		// cách gọn
		/*
		 * Map<LocalDate, Double> viewMap = isAscending ? stats : stats.descendingMap();
		 * for (Map.Entry<LocalDate, Double> entry : viewMap.entrySet()) {
		 * System.out.printf("   -> Ngày %s : %,15.0f VND\n", entry.getKey(),
		 * entry.getValue()); }
		 */
		if (!isAscending) {
			int sizeSortedKeys = sortedKeys.size();
			for (int i = 0; i < sizeSortedKeys / 2; i++) {
				LocalDate tmpDate = sortedKeys.get(i);
				sortedKeys.set(i, sortedKeys.get(sizeSortedKeys - 1 - i));
				sortedKeys.set(sizeSortedKeys - 1 - i, tmpDate);
			}
		}

		System.out.println("\n=== BÁO CÁO DOANH THU TỔNG HỢP THEO NGÀY ===");
		if (stats.isEmpty()) {
			System.out.println("Chưa có dữ liệu giao dịch.");
			return;
		}

		double totalPrice = 0;
		for (LocalDate date : sortedKeys) {
			Double money = stats.get(date);
			System.out.printf("   -> Ngày %s : %,15.0f VND\n", date, money);
			totalPrice += money;
		}

		System.out.println("------------------------------------------------");
		System.out.printf("   TỔNG CỘNG      : %,15.0f VND\n", totalPrice);
	}
	// END - Các methods show ra màn hình - báo cáo
}
