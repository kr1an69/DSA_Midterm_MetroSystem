package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import commercial.*;
import infrastructure.*;
import rollingstock.Carriage;
import rollingstock.Locomotive;
import rollingstock.Train;

public class FileManager {
	// FOLDER lưu DATABASE
	private static final String DATA_FOLDER = "dataTxt";

	// Tickets
	private static final String TICKETS_FILE_PATH = DATA_FOLDER + File.separator + "tickets_db.txt";
	// Orders
	private static final String ORDERS_FILE_PATH = DATA_FOLDER + File.separator + "orders_db.txt";
	// Customers
	private static final String CUSTOMERS_FILE_PATH = DATA_FOLDER + File.separator + "customers_db.txt";
	// Stations
	private static final String STATIONS_FILE_PATH = DATA_FOLDER + File.separator + "stations_db.txt";
	// Trains
	private static final String TRAINS_FILE_PATH = DATA_FOLDER + File.separator + "trains_db.txt";

	// Date formatter
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	// thằng static khởi tạo file/folder đề phòng chưa có tạo
	static {
		try {
			File folder = new File(DATA_FOLDER);
			if (!folder.exists()) {
				folder.mkdir();
				System.out.println("[SYSTEM] Đã tạo Data Folder: " + folder.getAbsolutePath());
			}

			createFileIfNotExists(TICKETS_FILE_PATH);
			createFileIfNotExists(ORDERS_FILE_PATH);
			createFileIfNotExists(CUSTOMERS_FILE_PATH);
			createFileIfNotExists(STATIONS_FILE_PATH);
			createFileIfNotExists(TRAINS_FILE_PATH);

		} catch (IOException e) {
			System.err.println("[ERROR] Khởi tạo file Database thất bại !!!: " + e.getMessage());
		}
	}

	private static void createFileIfNotExists(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
			System.out.println("[SYSTEM] Đã tạo mới file Database: " + file.getName());
		}
	}

	// CUSTOMERS - customers_db.txt
	// Format: ID,PHONE,NAME

	public static void saveCustomer(Customer c) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(CUSTOMERS_FILE_PATH, true)))) {
			// ID,PHONE,NAME
			String line = String.format("%s,%s,%s", c.getId(), c.getPhoneNumber(), c.getName());
			writer.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Customer> loadCustomers() {
		Map<String, Customer> customersDB = new HashMap<>();
		File file = new File(CUSTOMERS_FILE_PATH);
		if (!file.exists())
			return customersDB;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				if (parts.length < 3)
					continue;

				String id = parts[0];
				String phone = parts[1];
				String name = parts[2];

				Customer c = new Customer(id, phone, name);
				customersDB.put(id, c);
			}
			System.out.println("[FILE] Đã Load " + customersDB.size() + " customers.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load customers thất bại: " + e.getMessage());
		}
		return customersDB;
	}

	// STATIONS - stations_db.txt
	// Format: ID,NAME

	public static void saveStation(Station s) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(STATIONS_FILE_PATH, true)))) {
			String line = String.format("%s,%s", s.getId(), s.getName());
			writer.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Station> loadStations() {
		Map<String, Station> stationsDB = new HashMap<>();
		File file = new File(STATIONS_FILE_PATH);
		if (!file.exists())
			return stationsDB;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				if (parts.length < 2)
					continue;

				String id = parts[0];
				String name = parts[1];

				Station s = new Station(id, name);
				stationsDB.put(id, s);
			}
			System.out.println("[FILE] Đã load " + stationsDB.size() + " stations.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load stations thất bại: " + e.getMessage());
		}
		return stationsDB;
	}

	// TICKETS - tickets_db.txt

	// Save 1 vé
	public static void saveTicket(Ticket t) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(TICKETS_FILE_PATH, true)))) {
			StringBuilder line = new StringBuilder();
			// Base info: ID,TYPE,PRICE,STATUS,DATE
			line.append(t.getTicketId()).append(",");
			line.append(t.getType()).append(",");
			line.append(t.getPrice()).append(",");
			line.append(t.getStatus()).append(",");
			line.append(t.getIssuedDate().format(DATE_FMT));

			// Extra info based on Type
			if (t.getType() == TicketType.SINGLE) {
				SingleTicket st = (SingleTicket) t;
				// Format: ,START_ID,END_ID
				line.append(",").append(st.getStartStation().getId());
				line.append(",").append(st.getEndStation().getId());
			} else if (t.getType() == TicketType.MONTHLY) {
				MonthlyTicket mt = (MonthlyTicket) t;
				// Format: ,CUST_ID
				// Nếu Customer null thì ghi UNKNOWN
				String custId = (mt.getCustomer() != null) ? mt.getCustomer().getId() : "UNKNOWN";
				line.append(",").append(custId);
			}
			// DAILY ko cần thêm gì

			writer.println(line.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Load Ticket, cần Route (để map Station) và CustomersDB (để map Customer)
	public static Map<String, Ticket> loadTickets(Route route, Map<String, Customer> customersDB) {
		Map<String, Ticket> ticketsDB = new HashMap<>();
		File file = new File(TICKETS_FILE_PATH);
		if (!file.exists())
			return ticketsDB;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				if (parts.length < 5)
					continue;

				String id = parts[0];
				TicketType type = TicketType.valueOf(parts[1]);
				double price = Double.parseDouble(parts[2]);
				TicketStatus status = TicketStatus.valueOf(parts[3]);
				LocalDateTime date = LocalDateTime.parse(parts[4], DATE_FMT);

				Ticket t = null;

				switch (type) {
				case SINGLE:
					Station s1 = null, s2 = null;
					if (parts.length >= 7) {
						s1 = findStationById(route, parts[5]);
						s2 = findStationById(route, parts[6]);
					}
					// Fallback nếu ko tìm thấy trạm
					if (s1 == null)
						s1 = route.getStations().get(0);
					if (s2 == null)
						s2 = route.getStations().get(1);

					t = new SingleTicket(id, price, s1, s2);
					break;

				case MONTHLY:
					Customer owner = null;
					if (parts.length >= 6) {
						String custId = parts[5];
						if (customersDB != null) {
							owner = customersDB.get(custId);
						}
					}
					// Fallback owner
					if (owner == null)
						owner = new Customer("UNKNOWN", "N/A", "Unknown Guest");

					t = new MonthlyTicket(id, price, owner);
					break;

				case DAILY:
					t = new DailyTicket(id, price);
					break;
				}

				if (t != null) {
					t.setStatus(status);
					t.setIssuedDate(date);
					ticketsDB.put(id, t);
				}
			}
			System.out.println("[FILE] Đã load " + ticketsDB.size() + " tickets.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load tickets thất bại: " + e.getMessage());
		}
		return ticketsDB;
	}

	// tìm station
	private static Station findStationById(Route route, String stationId) {
		for (Station s : route.getStations()) {
			if (s.getId().equals(stationId))
				return s;
		}
		return null;
	}

	// ORDERS - orders_db.txt
	// Format: ID,DATE,TOTAL_PRICE,TICKET_ID_1|TICKET_ID_2|...

	public static void saveOrder(Order o) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(ORDERS_FILE_PATH, true)))) {
			StringBuilder line = new StringBuilder();
			line.append(o.getOrderId()).append(",");
			line.append(o.getOrderDate().format(DATE_FMT)).append(",");
			line.append(o.getTotalPrice()).append(",");

			// Nối các ticketID lại bằng dấu gạch đứng |
			// Stream API cho gọn, hoặc dùng for loop
			String ticketIds = o.getTickets().stream().map(Ticket::getTicketId).collect(Collectors.joining("|"));

			line.append(ticketIds);
			writer.println(line.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Load Orders: Cần ticketsDB để map ngược ID -> Object Ticket
	public static List<Order> loadOrders(Map<String, Ticket> ticketsDB) {
		List<Order> orders = new ArrayList<>();
		File file = new File(ORDERS_FILE_PATH);
		if (!file.exists())
			return orders;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				// Cần ít nhất ID, DATE, PRICE. Cột ticketIds có thể rỗng nếu order rỗng (hiếm)
				if (parts.length < 3)
					continue;

				String orderId = parts[0];
				LocalDateTime date = LocalDateTime.parse(parts[1], DATE_FMT);
				// double totalPrice = Double.parseDouble(parts[2]); // Có thể tính lại từ list
				// vé

				Order order = new Order(orderId);
				order.setOrderDate(date); // Nhớ thêm setter này vào Order

				// Parse list ticket IDs
				if (parts.length >= 4) {
					String[] tIds = parts[3].split("\\|"); // Escape pipe char
					for (String tId : tIds) {
						if (ticketsDB.containsKey(tId)) {
							order.addTicket(ticketsDB.get(tId));
						} else {
							// System.out.println("Warning: Ticket ID " + tId + " not found in DB.");
						}
					}
				}
				orders.add(order);
			}
			System.out.println("[FILE] Đã load " + orders.size() + " orders.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load orders thất bại: " + e.getMessage());
		}
		return orders;
	}

	// TRAINS - trains_db.txt
	// Format: TrainID,Speed,Power,SeatsPerCar,NumCars
	// này chỉ có load từ file DB chứ không lưu vì không tạo trong code 
	// có thể vô file và tự viết theo Format nếu muốn có thêm trains
	public static List<Train> loadTrains() {
		List<Train> fleet = new ArrayList<>();
		File file = new File(TRAINS_FILE_PATH);
		if (!file.exists())
			return fleet;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				if (parts.length < 5)
					continue;

				String id = parts[0];
				double speed = Double.parseDouble(parts[1]);
				double power = Double.parseDouble(parts[2]);
				int seats = Integer.parseInt(parts[3]);
				int numCars = Integer.parseInt(parts[4]);

				// build train - loco - carriage từ thông số
				// constructor loco đầu tiên - bắt buộc
				Train t = new Train(id, new Locomotive("H1-" + id, speed, power));
				// thêm loco thứ 2
				t.addLocomotive(new Locomotive("H2-" + id, speed, power));

				// thêm toa tàu
				for (int i = 1; i <= numCars; i++) {
					t.addCarriage(new Carriage("C" + i + "-" + id, seats));
				}

				fleet.add(t);
			}
			System.out.println("[FILE] Đã load " + fleet.size() + " trains from DB.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load trains thất bại: " + e.getMessage());
		}
		return fleet;
	}
}