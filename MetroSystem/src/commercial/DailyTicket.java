package commercial;

import java.time.*;

import infrastructure.Station;

public class DailyTicket extends Ticket {
	private LocalDateTime expiryDate;
	private Customer customer;

	public DailyTicket(String id, double price, Customer customer) {
		super(id, TicketType.DAILY, price);
		this.expiryDate = this.issuedDate.plusHours(24);
		this.customer = customer;
	}
	
	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	@Override
	public boolean isValidEntry(Station currentStation) {
		return status == TicketStatus.PAID && LocalDateTime.now().isBefore(expiryDate);
	}

	@Override
	public String toString() {
		return "Vé Ngày [" + ticketId + "] Hết hạn: " + expiryDate.toLocalDate() + " KH: " + customer;
	}
	//expiryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}
