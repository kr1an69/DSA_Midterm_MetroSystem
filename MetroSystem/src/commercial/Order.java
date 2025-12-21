package commercial;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Order {
	private String orderId;
	private LocalDateTime orderDate;
	// ArrayList: Tối ưu duyệt và lưu trữ đơn giản
	private List<Ticket> tickets = new ArrayList<>();
	private double totalPrice = 0;

	public Order(String id) {
		this.orderId = id;
		this.orderDate = LocalDateTime.now();
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void addTicket(Ticket t) {
		tickets.add(t);
		totalPrice += t.getPrice();
	}

	public int getTicketCount() {
		return tickets.size();
	}

	@Override
	public String toString() {
		return String.format("ORD-%s | %s | %d Vé | %.0f VND", orderId,
				orderDate.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")), tickets.size(), totalPrice);
	}
}
