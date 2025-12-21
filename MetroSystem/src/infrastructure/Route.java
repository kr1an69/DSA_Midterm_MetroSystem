package infrastructure;

import java.util.ArrayList;
import java.util.LinkedList;

public class Route {
	private String id;
	// LinkedList - mô phỏng các section đều nối tiếp nhau
	private LinkedList<Section> sections = new LinkedList<>();
	// ArrayList - giúp tối đa tốc độ truy xuất, cũng như khi xây dựng hạ tầng
	// đều cố định về mặt số lượng
	private ArrayList<Station> stations = new ArrayList<>();

	public Route(String id) {
		this.id = id;
	}

	public void addSection(Section s) {
		// kiểm tra section đã có trong List sections chưa
		if (sections.contains(s)) {
			System.out.println("LỖI: Đã có đoạn đường (Section) này trong danh sách các đoạn đường (Sections) !");
			return;
		}

		// kiểm tra việc nối đuôi có đúng thứ tự không
		// nếu list không rỗng, phải đảm bảo section mới nối tiếp section cũ
		if (!sections.isEmpty()) {
			Section lastSection = sections.getLast();
			// ga cuối thằng trước phải bằng ga đầu thằng sau
			if (!lastSection.getEndStation().equals(s.getStartStation())) {
				System.out.println("LỖI: đoạn đường (Section) mới không nối tiếp đoạn đường (Section) cũ !");
				return;
			}
		}

		// pass qua 2 thằng check thì add
		sections.add(s);
		if (!stations.contains(s.getStartStation()))
			stations.add(s.getStartStation());
		if (!stations.contains(s.getEndStation()))
			stations.add(s.getEndStation());
	}

	// Tính khoảng cách giữa 2 ga (Logic quét 1 vòng)
	public double getDistance(Station s1, Station s2) {
		if (s1.equals(s2))
			return 0;

		// stations khong co thang nao la s1 hoac s2
		int idx1 = stations.indexOf(s1);
		int idx2 = stations.indexOf(s2);
		if (idx1 == -1 || idx2 == -1)
			return -1;

		int start = Math.min(idx1, idx2);
		int end = Math.max(idx1, idx2);

		double totalDist = 0;
		// duyệt qua các section nằm giữa rồi cộng vô
		for (int i = start; i < end; i++) {
			totalDist += sections.get(i).getDistanceKm();
		}
		return totalDist;
	}

	public double getTotalDistance() {
		double result = 0;
		for (Section s : sections) {
			result += s.getDistanceKm();
		}
		return result;
	}

	// tính giá tiền vé lượt dựa trên cộng dồn giá các section
	public double calTotalPrice(Station s1, Station s2) {
		if (s1.equals(s2))
			return 0;

		int idx1 = stations.indexOf(s1);
		int idx2 = stations.indexOf(s2);

		int start = Math.min(idx1, idx2);
		int end = Math.max(idx1, idx2);

		double totalPrice = 0;
		for (int i = start; i < end; i++) {
			totalPrice += sections.get(i).getPrice();
		}
		return totalPrice;
	}

	public String getId() {
		return id;
	}

	public LinkedList<Section> getSections() {
		return sections;
	}

	public ArrayList<Station> getStations() {
		return stations;
	}

}
