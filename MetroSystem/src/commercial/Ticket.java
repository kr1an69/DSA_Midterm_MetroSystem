package commercial;

import java.time.LocalDateTime;

import infrastructure.Station;

public abstract class Ticket {
	protected String ticketId;
	protected TicketType type;
	protected double price;
	protected TicketStatus status;
	protected LocalDateTime issuedDate;

	// constructor nhan vao 2 prop, con luon tao ra ticket status new va lay .now()
	// time
	public Ticket(String id, TicketType type, double price) {
		this.ticketId = id;
		this.type = type;
		this.price = price;
		this.status = TicketStatus.NEW;
		this.issuedDate = LocalDateTime.now();
	}

	public double getPrice() {
		return price;
	}

	public String getTicketId() {
		return ticketId;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public TicketType getType() {
		return type;
	}

	public LocalDateTime getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(LocalDateTime date) {
		this.issuedDate = date;
	}

	public void setStatus(TicketStatus s) {
		this.status = s;
	}

	public void cancelTicket() {
		this.setStatus(TicketStatus.CANCELED);
	}

	public void payTicket() {
		this.setStatus(TicketStatus.PAID);
	}

	public void useTicket() {
		this.setStatus(TicketStatus.USED);
	}

	// ban đầu là isValid nhưng để checkin checkout cho singleTicket nên fix lại
	// logic check-in
	// Mặc định phải là PAID mới được vào
	// thằng method này DailyTicket và MonthlyTicket dùng là đủ không cần isValidExit
	public boolean isValidEntry(Station station) {
		return this.status == TicketStatus.PAID;
	}

	// logic check-out
	// Mặc định phải là IN_TRIP (tức là đã check-in rồi) mới được ra
	public boolean isValidExit(Station station) {
		return this.status == TicketStatus.IN_TRIP;
	}

}
