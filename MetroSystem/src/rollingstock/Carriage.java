package rollingstock;

public class Carriage extends RollingStock {
	private int numberOfSeats;

	public Carriage(String id, int maxSeats) {
		super(id);
		this.numberOfSeats = maxSeats;
	}

	public int getNumberOfSeats() {
		return numberOfSeats;
	}
	
}
