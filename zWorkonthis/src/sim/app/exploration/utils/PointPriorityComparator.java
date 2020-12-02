package sim.app.exploration.utils;

import java.util.Comparator;

import sim.util.Int2D;

public class PointPriorityComparator implements Comparator<PointPriority>{


	@Override
	public int compare(PointPriority o1, PointPriority o2) {
		
		
		
		if (o1.getPriority() > o2.getPriority()) {
			return 1;
			
		}
		else if (o1.getPriority() < o2.getPriority()){
			return -1;
		}
		return 0;
	}

}
