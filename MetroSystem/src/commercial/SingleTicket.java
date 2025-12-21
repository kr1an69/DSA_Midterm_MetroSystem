package commercial;

import java.time.LocalDateTime;

import infrastructure.Station;

public class SingleTicket extends Ticket {
	private Station startStation;
	private Station endStation;

	public SingleTicket(String id, double price, Station start, Station end) {
		super(id, TicketType.SINGLE, price);
		this.startStation = start;
		this.endStation = end;
	}
	
	public Station getStartStation() {
		return startStation;
	}

	public Station getEndStation() {
		return endStation;
	}

	@Override
	public boolean isValidEntry(Station currentStation) {
		if (this.status != TicketStatus.PAID) return false;
		return currentStation.equals(startStation);
	}
	
	@Override
	public boolean isValidExit(Station currentStation) {
		if (this.status != TicketStatus.IN_TRIP) return false;
		return currentStation.equals(endStation);
	}
	
	@Override
	public String toString() {
		return "Vé Lượt [" + ticketId + "] Từ: " + startStation.getName() + " -> " + endStation.getName()	;
	}
}
