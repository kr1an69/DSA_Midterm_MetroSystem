package commercial;

public enum TicketStatus {
	NEW, 
	PAID, //đã mua, chưa vô cổng soát vé
	IN_TRIP, //đã vô cổng soát vé, đang bên trong ga hoặc trên tàu
	USED,  //đa ra cổng soát vé sau khi đi xong - hoàn thành
	EXPIRED, 
	CANCELED
}
