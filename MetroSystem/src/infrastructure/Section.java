package infrastructure;

public class Section {
	public Station startStation;
	public Station endStation;
	public int distance;
	public double priceForSellingTicket;
	
	public Section(Station startStation, Station endStation, int distance, double priceForSellingTicket) {
		this.startStation = startStation;
		this.endStation = endStation;
		this.distance = distance;
		this.priceForSellingTicket = priceForSellingTicket;
	}
	
	
}
