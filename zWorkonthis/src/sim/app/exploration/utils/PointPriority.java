package sim.app.exploration.utils;

import sim.util.Int2D;

public class PointPriority {

	private int x;
	private int y;
	private double priority;//Heuristic cost
	public PointPriority parent;
	
	public int finalCost;//G+H with G(n) the cost of the path from start node to n
	// and H(n) the heuristic that estimates the cost of the cheapest path from n to the goals
	
	
	public PointPriority(Int2D i2d, double priority) {
		this.x = i2d.x;
		this.y = i2d.y;
		
		this.priority= priority;
	}
	
	
	
	
	
	public Int2D getInt2D_PointPrio() {
		return new Int2D(x,y);
	}
	
	
	public double getPriority() {
		return priority;
	}
	
	public void setPriority(double p) {
		priority=p;
	}
}
