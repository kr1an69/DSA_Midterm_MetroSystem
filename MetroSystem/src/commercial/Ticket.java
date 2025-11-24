package commercial;

import java.time.LocalDateTime;

public abstract class Ticket {
	protected String ticketId;
	protected double price;
	protected TicketStatus ticketStatus;
	protected LocalDateTime issuedDate;
	
	public void payTicket() {
		this.ticketStatus = TicketStatus.PAID;
	}
	
	public abstract boolean isValid();
	
}
