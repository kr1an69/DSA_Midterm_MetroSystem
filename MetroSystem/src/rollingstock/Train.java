package rollingstock;

import java.util.*;

public class Train {
	private String id;
	private List<Locomotive> locomotives = new ArrayList<>(); //cố định là 2 Loco
	private List<Carriage> carriages = new LinkedList<>(); // LinkedList thêm xóa dễ

	public Train(String id, Locomotive firstLoco) {
		this.id = id;
		this.locomotives.add(firstLoco);
	}

	//them loco cuoi cung vo
	public void addLocomotive(Locomotive extraLoco) {
		locomotives.add(extraLoco);
	}

	public void addCarriage(Carriage car) {
		carriages.add(car);
	}

	public int getTotalOfSeats() {
		int total = 0;
		for (Carriage c : carriages)
			total += c.getNumberOfSeats();
		return total;
	}

	//lấy tốc độ trung bình của đầu máy (locomotive)
	public double getAverageSpeed() {
		if (locomotives.isEmpty())
			throw new Error("This train doesn't have any Loco");
		return locomotives.get(0).getSpeed();
	}

	@Override
	public String toString() {
		return "Train " + id + " [Locos: " + locomotives.size() + " | Carriages: " + carriages.size() + "]";
	}

	public String getId() {
		return id;
	}

	public List<Locomotive> getLocomotives() {
		return locomotives;
	}

	public List<Carriage> getCarriages() {
		return carriages;
	}
	
	
}
