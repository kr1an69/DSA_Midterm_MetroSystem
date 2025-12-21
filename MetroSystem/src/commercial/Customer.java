package commercial;

public class Customer {
	private String id;
	private String phoneNumber;
	private String name;
	public Customer() {}
	public Customer(String id, String phoneNumber, String name) {
		super();
		this.id = id;
		this.phoneNumber = phoneNumber;
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public String getName() {
		return name;
	}
	
}
