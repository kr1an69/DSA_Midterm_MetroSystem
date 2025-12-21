package infrastructure;

import java.util.Objects;

public class Section {
	private Station startStation;
	private Station endStation;
    private double distanceKm;
    private double price; // gia tien co ban cho doan section

    public Section(Station start, Station end, double distance, double price) {
        this.startStation = start;
        this.endStation = end;
        this.distanceKm = distance;
        this.price = price;
    }

	public Station getStartStation() {
		return startStation;
	}

	public Station getEndStation() {
		return endStation;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public double getPrice() {
		return price;
	}
    
	//xử lý logic trùng Section bằng cách check station đầu và cuối
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;
	    Section section = (Section) o;
	    return Objects.equals(startStation, section.startStation) &&
	           Objects.equals(endStation, section.endStation);
	}
	@Override
	public int hashCode() {
	    return Objects.hash(startStation, endStation);
	}
}
