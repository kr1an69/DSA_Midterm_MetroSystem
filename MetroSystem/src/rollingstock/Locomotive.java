package rollingstock;

public class Locomotive extends RollingStock {
	private double speed; // km/h
	private double power;

	public Locomotive(String id, double speed, double power) {
		super(id); 
		this.speed = speed;
		this.power = power;
	}

	public double getSpeed() {
		return speed;
	}
}
