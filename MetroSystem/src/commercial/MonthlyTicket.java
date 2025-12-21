package commercial;

import java.time.*;
import infrastructure.Station;

public class MonthlyTicket extends Ticket {
	private Customer customer;
	private LocalDateTime expiryDate;

	public MonthlyTicket(String id, double price, Customer customer) {
		super(id, price, TicketType.MONTHLY);
		this.customer = customer;
		this.expiryDate = LocalDateTime.now().plusMonths(1);
	}

	public Customer getCustomer() {
		return customer;
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
		return "Vé Tháng [" + ticketId + "] KH: " + customer;
	}
}
