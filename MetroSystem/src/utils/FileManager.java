package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import bus.BusRoute;
import commercial.*;
import infrastructure.*;
import rollingstock.Carriage;
import rollingstock.Locomotive;
import rollingstock.Train;

public class FileManager {
	// FOLDER lưu DATABASE
	private static final String DATA_FOLDER = "dataTxt";

	private static final String STATIONS_FILE_PATH = DATA_FOLDER + File.separator + "stations_db.txt";
	private static final String SECTIONS_FILE_PATH = DATA_FOLDER + File.separator + "sections_db.txt";

	// Tickets
	private static final String TICKETS_FILE_PATH = DATA_FOLDER + File.separator + "tickets_db.txt";
	// Orders
	private static final String ORDERS_FILE_PATH = DATA_FOLDER + File.separator + "orders_db.txt";
	// Customers
	private static final String CUSTOMERS_FILE_PATH = DATA_FOLDER + File.separator + "customers_db.txt";
	
	// Trains
	private static final String TRAINS_FILE_PATH = DATA_FOLDER + File.separator + "trains_db.txt";
	// Bus Routes
	private static final String BUS_ROUTES_FILE_PATH = DATA_FOLDER + File.separator + "bus_routes_db.txt";

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
//			createFileIfNotExists(STATIONS_FILE_PATH);
			createFileIfNotExists(TRAINS_FILE_PATH);
			createFileIfNotExists(BUS_ROUTES_FILE_PATH);
			createFileIfNotExists(STATIONS_FILE_PATH);
			createFileIfNotExists(SECTIONS_FILE_PATH);

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
				String phoneNumber = parts[1];
				String name = parts[2];

				Customer c = new Customer(id, phoneNumber, name);
				customersDB.put(id, c);
			}
			System.out.println("[FILE] Đã Load " + customersDB.size() + " customers.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load customers thất bại: " + e.getMessage());
		}
		return customersDB;
	}

	// STATIONS
	public static Map<String, Station> loadStations() {
		Map<String, Station> map = new HashMap<>(); // Dùng Map để truy xuất nhanh theo ID
		File file = new File(STATIONS_FILE_PATH);
		if (!file.exists())
			return map;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] p = line.split(",");
				if (p.length < 2)
					continue;

				String id = p[0].trim();
				String name = p[1].trim();
				map.put(id, new Station(id, name));
			}
			System.out.println("[FILE] Đã load " + map.size() + " stations.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	// SECTIONS
	public static void loadSections(Route route, Map<String, Station> stationMap) {
		File file = new File(SECTIONS_FILE_PATH);
		if (!file.exists())
			return;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] p = line.split(",");
				if (p.length < 4)
					continue;

				String startId = p[0].trim();
				String endId = p[1].trim();
				double dist = Double.parseDouble(p[2]);
				double price = Double.parseDouble(p[3]);

				Station s1 = stationMap.get(startId);
				Station s2 = stationMap.get(endId);

				if (s1 != null && s2 != null) {
					// Tạo Section và add vào Route
					Section section = new Section(s1, s2, dist, price);
					route.addSection(section);
					count++;
				} else {
					System.out.println("[WARN] Không tìm thấy Station ID trong map: " + startId + " hoặc " + endId);
				}
			}
			System.out.println("[FILE] Đã load " + count + " sections vào Route.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TICKETS - tickets_db.txt

	// Save 1 vé
	// Format: ID,TYPE,PRICE,STATUS,ISSUED_DATE
	
	// lưu ticket
	public static void saveTicket(Ticket t) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(TICKETS_FILE_PATH, true)))) {
			writeTicketLine(writer, t);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// method này sẽ update tickets database khi hủy vé
	// file txt không cho phép nhảy vô giữa đoạn nào đó thay đổi -> Cần ghi đè toàn bộ file
	public static void updateTicketDatabase(Map<String, Ticket> ticketDB) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(TICKETS_FILE_PATH)))) {
			for (Ticket t : ticketDB.values()) {
				writeTicketLine(writer, t);
			}
			System.out.println("[FILE] Đã cập nhật database Tickets.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// method hỗ trợ tạo line write line
	private static void writeTicketLine(PrintWriter writer, Ticket t) {
		StringBuilder line = new StringBuilder();
		line.append(t.getTicketId()).append(",");
		line.append(t.getType()).append(",");
		line.append(t.getPrice()).append(",");
		line.append(t.getStatus()).append(",");
		line.append(t.getIssuedDate().format(DATE_FMT));

		// dựa vào tùy type mà có thêm thông tin khác
		if (t.getType() == TicketType.SINGLE) {
			// Format: START_ID,END_ID
			line.append(",").append(((SingleTicket) t).getStartStation().getId());
			line.append(",").append(((SingleTicket) t).getEndStation().getId());
		} else {
			Customer customer = null;
			if (t instanceof MonthlyTicket)
				customer = ((MonthlyTicket) t).getCustomer();
			else if (t instanceof DailyTicket)
				customer = ((DailyTicket) t).getCustomer();

			String cusId = (customer != null) ? customer.getId() : "UNKNOWN";
			line.append(",").append(cusId);
		}

		writer.println(line.toString());
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
					// lỡ không tìm thấy fallback
					if (s1 == null)
						s1 = route.getStations().get(0);
					if (s2 == null)
						s2 = route.getStations().get(1);

					t = new SingleTicket(id, price, s1, s2);
					break;
				// daily với monthly
				default:
					Customer customer = null;
					if (parts.length >= 6 && customersDB != null) {
						customer = customersDB.get(parts[5]);
					}
					if (customer == null)
						customer = new Customer("ID UNKNOWN", "No PhoneNumber", "Unknown Guest");
					//
					if (type == TicketType.DAILY)
						t = new DailyTicket(id, price, customer);
					else
						t = new MonthlyTicket(id, price, customer);

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
		return route.getStations().stream().filter(s -> s.getId().equals(stationId)).findFirst().orElse(null);
	}

	// ORDERS - orders_db.txt
	// Format: ID,DATE,TOTAL_PRICE,TICKET_ID_1|TICKET_ID_2|...

	// method lưu 1 order mới được khởi tạo khi mua vé vào cuối file txt
	public static void saveOrder(Order o) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(ORDERS_FILE_PATH, true)))) {
			writeOrderLine(writer, o);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// method này sẽ update orders database khi có thay đổi giá tiền về việc hủy vé
	// file txt không cho phép nhảy vô giữa đoạn nào đó thay đổi -> Cần ghi đè toàn
	// bộ file
	public static void updateOrderDatabase(List<Order> ordersDB) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(ORDERS_FILE_PATH)))) {
			for (Order o : ordersDB) {
				writeOrderLine(writer, o);
			}
			System.out.println("[FILE] Đã cập nhật database Orders.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// method hỗ trợ viết Order thành từng dòng vô text
	private static void writeOrderLine(PrintWriter writer, Order o) {
		StringBuilder line = new StringBuilder();
		line.append(o.getOrderId()).append(",");
		line.append(o.getOrderDate().format(DATE_FMT)).append(",");
		line.append(o.getTotalPrice()).append(",");

		// nối ticketID lại bằng dấu gạch đứng |
		// đoạn này dùng Stream API cho gọn, hoặc có thể dùng for loop
		String ticketIds = o.getTickets().stream().map(Ticket::getTicketId).collect(Collectors.joining("|"));

		line.append(ticketIds);
		writer.println(line.toString());
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
				if (parts.length < 3)
					continue;

				String orderId = parts[0];
				LocalDateTime date = LocalDateTime.parse(parts[1], DATE_FMT);
				double savedTotalPrice = Double.parseDouble(parts[2]);

				Order order = new Order(orderId);
				order.setOrderDate(date);

				// set về giá 0 để k bị cộng dồn thừa
				order.setTotalPrice(0);

				if (parts.length >= 4) {
					String[] tIds = parts[3].split("\\|");
					for (String tId : tIds) {
						if (ticketsDB.containsKey(tId)) {
							// addTicket có cộng dồn giá sẵn
							order.addTicket(ticketsDB.get(tId));
						}
					}
				}
				// ở đây có thể bỏ setTotalPrice vì khi addTicket vô order thì đã tự
				// cộng dồn price
				order.setTotalPrice(savedTotalPrice);

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
				
				// thằng locomotive và carriage không tạo file txt, vì train lưu các thông
				// số cơ bản là chủ yếu nên chỉ tạo các loco và carri trong RAM
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
	
	//BUSROUTE 
	public static List<BusRoute> loadBusRoutes(Route route) {
		List<BusRoute> busList = new ArrayList<>();
		File file = new File(BUS_ROUTES_FILE_PATH);
		if (!file.exists())
			return busList;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",");
				if (parts.length < 2)
					continue;

				String routeName = parts[0].trim();
				String stationId = parts[1].trim();

				// Tìm Metro Station
				Station connectedStation = null;
				for (Station s : route.getStations()) {
					if (s.getId().equals(stationId)) {
						connectedStation = s;
						break;
					}
				}

				if (connectedStation != null) {
					// Parse danh sách bus stops 
					List<String> busStops = new ArrayList<>();
					if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
						String[] stops = parts[2].split("\\|");
						for (String stop : stops) {
							String trimmed = stop.trim();
							if (!trimmed.isEmpty()) {
								busStops.add(trimmed);
							}
						}
					}

					busList.add(new BusRoute(routeName, connectedStation, busStops));
				}
			}
			System.out.println("[FILE] Đã load " + busList.size() + " bus routes.");
		} catch (Exception e) {
			System.out.println("[ERROR] Load bus routes thất bại: " + e.getMessage());
		}
		return busList;
	}
}