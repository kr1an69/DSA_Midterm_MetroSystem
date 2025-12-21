package operation;

import java.time.*;

import infrastructure.*;
import rollingstock.*;

public class Trip {
	private String tripId;
	private Train train;
	private Route route;
	private Direction direction;
	private LocalTime startTime;

    public Trip(String id, Train train, Route route, Direction direction, LocalTime start) {
        this.tripId = id;
        this.train = train;
        this.route = route;
        this.direction = direction;
        this.startTime = start;
    }

    public String getTripId() {
		return tripId;
	}


	public Train getTrain() {
		return train;
	}


	public Route getRoute() {
		return route;
	}


	public Direction getDirection() {
		return direction;
	}


	public LocalTime getStartTime() {
		return startTime;
	}


	// method này trả về vị trí hiện tại của train với tgian input cụ thể để mô phỏng real time
    public String getCurrentLocation(LocalTime currentTime) {
        if (currentTime.isBefore(startTime)) return "Chưa khởi hành";
        
        // đoạn này tính time trôi qua, nếu như khởi hành lúc 6:00 thì nhập 6:00
        // sẽ báo section đầu tiên và 0,0km
        double hoursElapsed = Duration.between(startTime, currentTime).toMinutes() / 60.0;
        double speed = this.train.getAverageSpeed();
        double distanceTraveled = hoursElapsed * speed;

        // Logic tìm xem đang ở Section nào
        for (Section sec : route.getSections()) {
        	// nếu đi hết đoạn này
            if (distanceTraveled > sec.getDistanceKm()) {
                distanceTraveled -= sec.getDistanceKm(); //trừ bớt đi để tính tiếp
            } else {
            	// rớt xuống đây tức là tàu đang di chuyển trong đoạn này
                return String.format("Đang chạy giữa %s và %s (Km %.1f)", 
                       sec.getStartStation().getName(), sec.getEndStation().getName(), distanceTraveled);
            }
        }
        
        return "Đã về bến cuối";
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Khởi hành: %s", tripId, train.getId(), direction, startTime);
    }
}
