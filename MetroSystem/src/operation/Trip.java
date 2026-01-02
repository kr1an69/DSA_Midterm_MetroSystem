package operation;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import infrastructure.*;
import rollingstock.*;

public class Trip {
	private String tripId;
	private Train train;
	private Route route;
	private Direction direction;
	private LocalTime startTime;
	private int dwellTimeMinutes = 0;

	public Trip(String id, Train train, Route route, Direction direction, LocalTime start) {
		this.tripId = id;
		this.train = train;
		this.route = route;
		this.direction = direction;
		this.startTime = start;
	}

	public void setDwellTime(int minutes) {
		this.dwellTimeMinutes = minutes;
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

	// method này trả về vị trí hiện tại của train với tgian input cụ thể để mô
	// phỏng real time
	public String getCurrentLocation(LocalTime currentTime) {
		if (currentTime.isBefore(startTime))
			return "Chưa khởi hành (Dự kiến: " + startTime + ")";

		// đoạn này tính time trôi qua, nếu như khởi hành lúc 6:00 thì nhập 6:00
		// sẽ báo section đầu tiên và 0,0km
		/**
		 * hoursElapsed - thgian đã qua từ tgian khởi hành và tgian mình muốn check
		 * distanceTraveled - quãng đường về mặt lý thuyết mà tàu đã đi được với
		 * hoursElapsed currentSpeed - đơn giản là tốc độ tring bình của tàu
		 */
		double hoursElapsed = Duration.between(startTime, currentTime).toMinutes() / 60.0;
		double currentSpeed = this.train.getAverageSpeed();
		//double distanceTraveled = hoursElapsed * currentSpeed;

		// biến đếm cho việc đếm qduong và tgian
		/**
		 * timeUsedHours - cộng dồn thời gian tiêu tốn để đi qua các đoạn (section)
		 * trước đó
		 */
		double timeUsedHours = 0;

		/*
		 * Duyệt qua section để mô phỏng hành trình tàu chạy vì có 2 chiều, tức là phải
		 * có 1 đoạn reverse list tạo list mới để mô phỏng, không đụng chạm đến
		 * LinkedList sections của class Route và dùng ArrayList bên đây để duyệt và
		 * thực hiện các phép toán nhanh hơn. NGOÀI RA, ở đây có liên quan về vấn đề
		 * DEEP COPY và SHALLOW COPY đầu tiên thì cách khai báo như dưới nó là DEEP COPY
		 * Container và SHALLOW COPY các phần tử bên trong (Section objects)
		 * -----------------------------------------------------------------------------
		 * Tạo mới thằng container để reverse không ảnh hưởng - còn giữ các objects cũ
		 * để đúng về mặt logic chỉ là 1 thực thể duy nhất cũng như không tốn bộ nhớ vô
		 * ích
		 */
		List<Section> path = new ArrayList<>(route.getSections());
		if (direction == Direction.TO_BENTHANH) {
			Collections.reverse(path);
		}

		// LOGIC CHÍNH cho việc tìm xem tàu đang ở Section nào
		for (Section sec : path) {
			// 1. thời gian chạy trên đường ray (không tính lúc dừng)
			// này là cho mỗi section
			/*
			 * travelTime - thời gian chạy qua cái sec đang duyệt ở loop này
			 */
			double travelTime = sec.getDistanceKm() / currentSpeed;

			// 2. thời gian dừng ở ga ĐẾN của section này (trừ ga cuối cùng)
			// tức là giả sử đang đi trên section từ A đến B thì tgian dừng ở ga B
			/*
			 * dwellTime - thời gian dừng ở ga đến và chuyển về hour -> chia cho 60
			 */
			double dwellTime = (double) dwellTimeMinutes / 60.0;

			// FIRST CHECK - Check xem tàu đang ở đâu trong đoạn này
			/*
			 * tức là nếu time trôi qua từ lúc khởi hành với lúc check nó mà NHỎ HƠN
			 * timeUsedHours(như giải thích trên) + travelTime (cũng giải thích ở trên) tức
			 * là ngay cái đoạn sec mà loop đang duyệt tàu ĐANG Ở ĐÂY ĐANG CHẠY TRÊN CÁI SEC
			 * ĐƯỢC DUYỆT
			 */
			if (hoursElapsed <= timeUsedHours + travelTime) {
				// Tàu đang chạy trên đường ray
				double timeInSec = hoursElapsed - timeUsedHours;
				double distInSec = timeInSec * currentSpeed;

				// Xử lý tên ga đúng chiều
				String startName = (direction == Direction.TO_SUOITIEN) ? sec.getStartStation().getName()
						: sec.getEndStation().getName();
				String endName = (direction == Direction.TO_SUOITIEN) ? sec.getEndStation().getName()
						: sec.getStartStation().getName();

				return String.format("Đang chạy: %s -> %s (Km %.1f)", startName, endName, distInSec);
			}

			timeUsedHours += travelTime; // Cộng thời gian chạy tiếp tục cho loop

			// SECOND CHECK - ktra xem tàu có dừng ở ga nào không
			/*
			 * đơn giản là nối tiếp với FIRST CHECK nếu không vào trường hợp đang chạy trên
			 * sec đang được duyệt tức là nó đang dừng để đón khách tại cái sec này
			 */
			if (hoursElapsed <= timeUsedHours + dwellTime) {
				String stationName = (direction == Direction.TO_SUOITIEN) ? sec.getEndStation().getName()
						: sec.getStartStation().getName();
				return "Đang DỪNG đón khách tại ga: " + stationName;
			}

			timeUsedHours += dwellTime; // Cộng thời gian dừng tiếp tục cho loop
		}

		return "Đã về bến cuối";
	}

	@Override
	public String toString() {
		return String.format("[%s] %s | %s | Khởi hành: %s", tripId, train.getId(), direction, startTime);
	}
}
