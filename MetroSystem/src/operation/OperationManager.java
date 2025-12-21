package operation;

import java.time.*;
import java.util.*;
import operation.Direction;
import infrastructure.Route;
import rollingstock.Train;
import utils.FileManager;

public class OperationManager {
	private List<Train> fleet = new ArrayList<>();
	// ArrayList: Giữ thứ tự thời gian
	private List<Trip> dailySchedule = new ArrayList<>();
	private Route route;

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

	public void generateScheduleForTest() {
		// test data, gene ra trips
		LocalTime time = LocalTime.of(6, 0);
		for (int i = 0; i < 10; i++) {
			Train t = fleet.get(i % fleet.size());
			Direction d = (i % 2 == 0) ? Direction.TO_SUOITIEN : Direction.TO_BENTHANH;
			dailySchedule.add(new Trip("TRIP-" + (i + 1), t, route, d, time));
			time = time.plusMinutes(30);
		}
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
