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

	public TicketManager(Route route) {
		this.route = route;
		// load db theo thứ tự tránh sai sót
		// cus trước
		this.customersDB = FileManager.loadCustomers();

		// tickets
		this.ticketDB = FileManager.loadTickets(route, customersDB);

		// orders
		this.ordersDB = FileManager.loadOrders(ticketDB);
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
		// Tìm Object Customer từ id nhập vào
		Customer customer = customersDB.get(customerId);
		if (customer == null) {
			// Nếu chưa có thì tạo mới - để unknow và new guest đơn giản hóa (khách mới)
			customer = new Customer(customerId, "Unknown", "New Guest");
			customersDB.put(customerId, customer);
			// save id khách mới vô file DB
			FileManager.saveCustomer(customer);
			System.out.println("[SYSTEM] Đã tạo và lưu khách hàng mới: " + customerId);
		}
		if (type == TicketType.DAILY)
			return new DailyTicket(id, 40000, customer); // giá fix 40k
		else
			return new MonthlyTicket(id, 200000, customer); // giá fix 200k

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
			if (t instanceof SingleTicket) {
				System.out.println("CHECK-IN THẤT BẠI: Vé không hợp lệ tại ga này hoặc chưa thanh toán !");
		    } else {
		    	System.out.println("CHECK-IN THẤT BẠI: Vé đã hết hạn sử dụng !");
		    }
			return false;
		}
	}

	// checkout
	public boolean processCheckOut(String ticketId, Station currentStation) {
		Ticket t = ticketDB.get(ticketId);
		if (t == null) {
			System.out.println("LỖI: Không tìm thấy vé !");
			return false;
		}
		
		if (t.getStatus() != TicketStatus.IN_TRIP) {
	        System.out.println("CHECK-OUT THẤT BẠI: Vé chưa Check-in !");
	        return false;
	    }
		
		if (t instanceof SingleTicket) {
	        // vé lượt - single: tức là vé này dùng 1 lần duy nhất và đổi thành used
			if (t.isValidExit(currentStation)) {
				t.setStatus(TicketStatus.USED); // đổi status vé đã sử dụng
				System.out.println("CHECK-OUT THÀNH CÔNG: Cảm ơn quý khách. Hẹn gặp lại !");
				return true;
			} else {
				System.out.println("CHECK-OUT THẤT BẠI: Bạn đi sai ga !");
				return false;
			}
	    } else {
	    	// đây là check vé daily và monthly, vì 2 thằng này không có trạng thái đã dùng
	    	// mà nó được xác định bằng việc đã thanh toán chưa và còn hạn không, tức là
	        // dạng vé thời gian, ra khỏi ga vẫn dùng tiếp được nếu còn hạn -> Quay về PAID
	        // miễn là check hạn sử dụng (expiryDate) ở lần Check-in tiếp theo
	        t.setStatus(TicketStatus.PAID);
	        System.out.println("CHECK-OUT THÀNH CÔNG: Cảm ơn quý khách. Hẹn gặp lại !");
	        return true;
	    }
	}
	
	// hoàn vé
	public void processRefund(String ticketId) {
		Ticket t = ticketDB.get(ticketId);
		if (t == null) {
			System.out.println("LỖI: Không tìm thấy vé có ID: " + ticketId);
			return;
		}

		// điều kiện hoàn vé - chỉ khi đã thanh toán PAID, những trường hợp còn lại
		// NEW, IN_TRIP, USED, EXPIRED, CANCELED - không cho phép hủy
		if (t.getStatus() != TicketStatus.PAID) {
			System.out.println("TỪ CHỐI: Vé này đã sử dụng, đã hết hạn hoặc đã hủy, không thể hoàn tiền.");
			return;
		}

		// tìm Order chứa vé này (Dùng Java 8 Stream để lọc)
		Optional<Order> targetOrder = ordersDB.stream()
				.filter(o -> o.getTickets().stream().anyMatch(ticket -> ticket.getTicketId().equals(ticketId)))
				.findFirst();

		if (targetOrder.isPresent()) {
			Order order = targetOrder.get();

			// 4. Update trạng thái vé
			t.cancelTicket(); // Chuyển sang CANCELED

			// 5. Trừ tiền Order
			// Lưu ý: Ta không xóa vé khỏi list tickets của Order để giữ lịch sử truy vết
			// Nhưng ta phải trừ tiền doanh thu đi
			double refundAmount = t.getPrice();
			double newTotal = order.getTotalPrice() - refundAmount;
			order.setTotalPrice(Math.max(0, newTotal)); // Tránh âm tiền

			System.out.println("✅ HOÀN VÉ THÀNH CÔNG!");
			System.out.println("   -> Vé " + ticketId + " đã hủy.");
			System.out.println(
					"   -> Đơn hàng " + order.getOrderId() + " giảm " + String.format("%,.0f", refundAmount) + " VND.");
			System.out.println("   -> Tổng tiền mới: " + String.format("%,.0f", order.getTotalPrice()) + " VND.");

			// 6. Cập nhật lại File Database (Ghi đè lại file để update status và price)
			// Lưu ý: Cách này hơi tốn kém I/O nếu file lớn, nhưng đảm bảo nhất quán cho đồ
			// án
			FileManager.updateOrderDatabase(ordersDB); // Cần thêm hàm này bên FileManager
			FileManager.updateTicketDatabase(ticketDB); // Cần thêm hàm này bên FileManager

		} else {
			System.out.println("⚠️ CẢNH BÁO: Tìm thấy vé nhưng không tìm thấy Đơn hàng gốc chứa nó (Lỗi dữ liệu).");
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

	// END - Các methods trả về TreeMap cơ bản - tính toán

	// Các methods show ra màn hình - báo cáo
	// với boolean nếu true trả mặc định là tăng giần - false giảm dần
	// bàn sơ qua thì có khá nhiều cách để show dạng giảm dần
	// 1. descendingMap() -> đơn giản nhanh
	// 2. trả cặp key trong map ra thành list và duyệt ngược bằng for hoặc reverse()
	// 3. dùng comparator khi decalre map

	// Show báo cáo report

	// report theo giờ trong ngày cụ thể
	public void showHourlyReport(LocalDate dateToShow, boolean sortByRevenue, boolean isAscending) {
		TreeMap<Integer, Double> stats = getHourlyRevenueStats(dateToShow);

		if (stats.isEmpty()) {
			System.out.println("\n--- KHÔNG CÓ GIAO DỊCH TRONG NGÀY " + dateToShow + " ---");
			return;
		}

		// Chuyển sang List để sort
		List<Map.Entry<Integer, Double>> list = new ArrayList<>(stats.entrySet());

		if (sortByRevenue) {
			// Sort theo value giá tiền
			list.sort(Map.Entry.comparingByValue());
		} else {
			// Sort theo key giờ
			list.sort(Map.Entry.comparingByKey());
		}

		// Nếu chọn giảm dần thì đảo ngược list
		// Có thể dùng Collections.reverse()
		if (!isAscending) {
			int n = list.size();
			// duyệt thủ công từ đầu đến giữa danh sách, hoán đổi phần tử i với (n-1-i)
			for (int i = 0; i < n / 2; i++) {
				Map.Entry<Integer, Double> temp = list.get(i);
				list.set(i, list.get(n - 1 - i));
				list.set(n - 1 - i, temp);
			}
		}

		System.out.println("\n=== DOANH THU CHI TIẾT NGÀY: " + dateToShow + " ===");
		System.out.println(sortByRevenue ? "(Sắp xếp theo Doanh Thu)" : "(Sắp xếp theo Giờ)");
		System.out.println("------------------------------------------------");

		double totalPrice = 0;
		for (Map.Entry<Integer, Double> entry : list) {
			System.out.printf("   -> Khung giờ %02d:00 : %,12.0f VND\n", entry.getKey(), entry.getValue());
			totalPrice += entry.getValue();
		}
		System.out.println("------------------------------------------------");
		System.out.printf("   TỔNG CỘNG       : %,15.0f VND\n", totalPrice);
	}

	// report theo các ngày gần đây
	public void showDailyReport(boolean sortByRevenue, boolean isAscending) {
		TreeMap<LocalDate, Double> stats = getDailyRevenueStats();

		if (stats.isEmpty()) {
			System.out.println("\n--- CHƯA CÓ DỮ LIỆU GIAO DỊCH ---");
			return;
		}

		// Chuyển sang List để sort
		List<Map.Entry<LocalDate, Double>> list = new ArrayList<>(stats.entrySet());

		if (sortByRevenue) {
			list.sort(Map.Entry.comparingByValue());
		} else {
			// Sort theo key ngày
			list.sort(Map.Entry.comparingByKey());
		}

		// dùng Collections.reverse()
		if (!isAscending) {
			int n = list.size();
			// chơi thủ công bằng Swapping đối xứng two-pointer
			for (int i = 0; i < n / 2; i++) {
				Map.Entry<LocalDate, Double> temp = list.get(i);
				list.set(i, list.get(n - 1 - i));
				list.set(n - 1 - i, temp);
			}
		}

		System.out.println("\n=== BÁO CÁO DOANH THU TỔNG HỢP THEO NGÀY ===");
		System.out.println(sortByRevenue ? "(Sắp xếp theo Doanh Thu)" : "(Sắp xếp theo Thời Gian)");
		System.out.println("------------------------------------------------");

		double totalPrice = 0;
		for (Map.Entry<LocalDate, Double> entry : list) {
			System.out.printf("   -> Ngày %s : %,15.0f VND\n", entry.getKey(), entry.getValue());
			totalPrice += entry.getValue();
		}

		System.out.println("------------------------------------------------");
		System.out.printf("   TỔNG CỘNG       : %,15.0f VND\n", totalPrice);
	}
	// END - Các methods show ra màn hình - báo cáo
}
