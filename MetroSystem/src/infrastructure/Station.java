package infrastructure;

public class Station {
    private String id;
    private String name;

	public Station(String id, String name) {
        this.id = id;
        this.name = name;
    }
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
//    @Override
//    public String toString() { return name; }
//
	
	// xử lý vấn đề trùng Station bằng việc check ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }
}
