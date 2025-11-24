package infrastructure;

import java.util.ArrayList;
import java.util.LinkedList;

public class Route {
	private String id;
	private LinkedList<Section> sections = new LinkedList<>();
	private ArrayList<Station> stations = new ArrayList<>();
	
	public Route(String id) {
		this.id = id;
	}
	
	public void addSection(Section s) {
		this.sections.add(s);
		if(!stations.contains(s.startStation)) stations.add(s.startStation);
		if(!stations.contains(s.endStation)) stations.add(s.endStation);
		
	}
}
