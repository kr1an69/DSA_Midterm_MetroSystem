package operation;

import java.time.*;
import java.util.*;
import operation.Direction;
import infrastructure.Route;
import rollingstock.Train;
import utils.FileManager;

public class OperationManager {
	private List<Train> fleet = new ArrayList<>();
	private List<Trip> dailySchedule = new ArrayList<>();
	private Route route;

	// setup thời gian cho việc sinh lịch trình tự động
	private static final int DWELL_TIME_MINUTES = 5; // time dừng ở mỗi ga
	private static final int TURN_AROUND_TIME = 15;
	// time dừng cuối trip và nạp nhiên liệu hay làm gì đó và chạy ngược lại

	public OperationManager(Route r) {
		this.route = r;
		// load fleet từ file DB
		this.fleet = FileManager.loadTrains();
	}

	public List<Train> getFleet() {
		return fleet;
	}

	public List<Trip> getDailySchedule() {
		return dailySchedule;
	}

	public Route getRoute() {
		return route;
	}

	public void addTrain(Train t) {
		fleet.add(t);
	}

	//
	public void generateScheduleForTest() {
		// 1. Tính thời gian chạy 1 lượt cho từng tàu
		// giả sử lấy tốc độ trung bình của tàu để ước lượng
		double avgSpeed = fleet.get(0).getAverageSpeed();
		double totalDist = route.getTotalDistance();

		// Số lượng ga trung gian phải dừng - trừ thằng ga đầu với cuối
		int intermediateStops = route.getStations().size() - 2;

		// TGian chạy 1 lượt = (totalDist/avgSpeed) + (số ga * tgian dững giữa mỗi trạm)
		long totalTripTime = (long) ((totalDist / avgSpeed) * 60) + (intermediateStops * DWELL_TIME_MINUTES);

		// tgian tàu sẵn sàng chuyến tiếp theo tính từ lúc khởi hành
		long nextAvailableAfter = totalTripTime + TURN_AROUND_TIME;

//		System.out.println("[SYSTEM] Quãng đường: " + String.format("%.1f", totalDist) + "km");
//		System.out
//				.println("[SYSTEM] Thời gian hành trình dự kiến: " + totalTripTime + " phút (gồm dừng ở các trạm)");
//		System.out.println("[SYSTEM] Thời gian quay vòng tàu - sẵn sàng đi tiếp: " + nextAvailableAfter + " phút");

		//
		// 2. tạo trạng thái cho đội tàu
		// Map lưu thời điểm tàu sẽ rảnh (6:00 bắt đầu)
		Map<Train, LocalTime> trainAvailability = new HashMap<>();
		// Map lưu vị trí hiện tại của tàu
		// (True: chạy từ Bến Thành/Start, False: chạy từ Suối Tiên/End)
		Map<Train, Boolean> trainAtStartStation = new HashMap<>();

		LocalTime startTime = LocalTime.of(6, 0); // Giờ bắt đầu chạy

		// đây là logic phân bố tàu: một nửa ở đầu, một nửa ở cuối để chạy song song
		for (int i = 0; i < fleet.size(); i++) {
			Train t = fleet.get(i);
			trainAvailability.put(t, startTime); // all tàu sẵn sàng chạy lúc 6

			// logic cho việc chia đôi: chẵn đi từ BThanh, lẻ đi từ STiên
			boolean isAtStart = (i % 2 == 0);
			trainAtStartStation.put(t, isAtStart);

			System.out.println(" -> Tàu " + t.getId() + " đang chờ ở " + (isAtStart ? "Bến Thành" : "Suối Tiên"));
		}

		// 3. loop cho việc xếp lịch (6:00 -> 22:00)
		LocalTime currTime = startTime;
		LocalTime endTime = LocalTime.of(22, 0);

		while (!currTime.isAfter(endTime)) {
			// TÌM TÀU Ở BẾN THÀNH ĐỂ CHẠY VỀ SUỐI TIÊN
			Train t1 = findAvailableTrain(trainAvailability, trainAtStartStation, currTime, true);
			if (t1 != null) {
				Trip trip = new Trip("TRIP-" + t1.getId() + "-" + currTime.toString().replace(":", ""), t1, route,
						Direction.TO_SUOITIEN, currTime);
				trip.setDwellTime(DWELL_TIME_MINUTES); // Set thời gian dừng để tính vị trí
				dailySchedule.add(trip);

				// Cập nhật trạng thái tàu
				trainAvailability.put(t1, currTime.plusMinutes(nextAvailableAfter));
				trainAtStartStation.put(t1, false); // chạy đến suối tiên
			}

			// TÌM TÀU Ở SUỐI TIÊN ĐỂ CHẠY VỀ BẾN THÀNH
			Train t2 = findAvailableTrain(trainAvailability, trainAtStartStation, currTime, false);
			if (t2 != null) {
				Trip trip = new Trip("TRIP-" + t2.getId() + "-" + currTime.toString().replace(":", ""), t2, route,
						Direction.TO_BENTHANH, currTime);
				trip.setDwellTime(DWELL_TIME_MINUTES);
				dailySchedule.add(trip);

				// Cập nhật trạng thái tàu
				trainAvailability.put(t2, currTime.plusMinutes(nextAvailableAfter));
				trainAtStartStation.put(t2, true); // chạy đến BThanh
			}

			// thêm 30 phút cho lượt tàu kế khởi hành (tức là sau khi đợt 1 xong)
			currTime = currTime.plusMinutes(30);
		}

		// Sắp xếp lại lịch trình theo giờ để in ra cho đẹp
		//java8
		dailySchedule.sort(Comparator.comparing(Trip::getStartTime));
		System.out.println("[SYSTEM] Đã khởi tạo lịch trình: " + dailySchedule.size() + " chuyến (" + fleet.size() + " tàu hoạt động).");

	}

	// helper method - tìm tàu rảnh ở vị trí cụ thể (isAtStart) và có thời gian rảnh <= now
	private Train findAvailableTrain(Map<Train, LocalTime> availMap, Map<Train, Boolean> locMap, LocalTime now,
			boolean needAtStart) {
		for (Train t : fleet) {
			boolean isAtLocation = locMap.get(t) == needAtStart;
			boolean isTimeReady = !availMap.get(t).isAfter(now); 
			// thời gian rảnh để sẵn sàng chuyến mới <= Hiện tại

			if (isAtLocation && isTimeReady) {
				return t;
			}
		}
		return null; // k tàu nào rảnh
	}

	public void showFleetStatus() {
		System.out.println("\n=== ĐỘI TÀU HIỆN TẠI ===");
		for (Train t : fleet)
			System.out.println(t);
	}

	public void showSchedule() {
		System.out.println("\n=== LỊCH CHẠY HÔM NAY ===");
		for (Trip t : dailySchedule)
			System.out.println(t);
	}

	public void showTrainLocation(String tripId, LocalTime time) {
		for (Trip t : dailySchedule) {
			if (t.getTripId().equals(tripId)) {
				System.out.println("Vị trí tàu " + tripId + ": " + t.getCurrentLocation(time));
				return;
			}
		}
		System.out.println("Không tìm thấy chuyến tàu!");
	}
}
