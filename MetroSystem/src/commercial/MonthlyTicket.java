package commercial;

import java.time.*;
import infrastructure.Station;

public class MonthlyTicket extends Ticket {
	private LocalDateTime expiryDate;
	private Customer customer;

	public MonthlyTicket(String id, double price, Customer customer) {
		super(id, TicketType.MONTHLY, price);
		this.expiryDate = LocalDateTime.now().plusMonths(1);
		this.customer = customer;
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
		return "Vé Tháng [" + ticketId + "] Hết hạn: " + expiryDate.toLocalDate() + " KH: " + customer;
	}
}
