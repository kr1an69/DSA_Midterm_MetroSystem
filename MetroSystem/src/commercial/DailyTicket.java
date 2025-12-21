package commercial;

import java.time.*;

import infrastructure.Station;

public class DailyTicket extends Ticket {
	private LocalDateTime expiryDate;

	public DailyTicket(String id, double price) {
		super(id, price, TicketType.DAILY);
		this.expiryDate = this.issuedDate.plusHours(24);
		//ve ngay het han trong ngay chu khong phai dang 24h
	}
	
	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	@Override
	public boolean isValidEntry(Station currentStation) {
		return status == TicketStatus.PAID && LocalDateTime.now().isBefore(expiryDate);
	}

	@Override
	public String toString() {
		return "Vé Ngày [" + ticketId + "] Hết hạn: " + expiryDate.toLocalDate();
	}
	//expiryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}
