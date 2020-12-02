package sim.app.exploration.agents;

import sim.app.exploration.objects.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import jdk.internal.module.ModuleLoaderMap.Mapper;
import sim.app.exploration.objects.SimObject;
import sim.app.exploration.utils.PointPriority;
import sim.app.exploration.utils.PointPriorityComparator;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;

public class PathfinderAgent {
	
	private MapperAgent mapper;
	public Network graph;

	
	private static final double COST_PATH_TREE=2;
	private static final double COST_PATH_WATER=5;
	private static final double COST_PATH_HOUSE=2;
	private static final double COST_PATH_WALL=100;
	private static final double COST_PATH_UNKNOWN=1;
	
	//public SparseGrid2D exploredLocations;
	//PriorityQueue<Int2D> frontier = new PriorityQueue<Int2D>();
	
	public PathfinderAgent (MapperAgent mapper) {
		this.graph= new Network(false); // false for undirected graph
		
		//exploredLocations=new SparseGrid2D(width, heigth);
		
		this.mapper = mapper;
		//exploredLocations= mapper.knownWorld;
		
	}
	
	static public double Heuristic(PointPriority  a, PointPriority b)
    {
		return Math.abs(a.getInt2D_PointPrio().x - b.getInt2D_PointPrio().x) + Math.abs(a.getInt2D_PointPrio().y - b.getInt2D_PointPrio().y);
    }
	

	
	
	
	
	
	/**
	 * Consider priority like cost of path.
	 * @param start_loc
	 * @param target_loc
	 * @return
	 */
	public List<Int2D>  computePath_Astar(Int2D start_loc, Int2D target_loc){
		//ashtable<PointPriority,PointPriority>
		
		
		double costStart = getCostOfCell(start_loc);
		double costTarget= getCostOfCell(start_loc);
		//System.out.println(costStart);
		PointPriority start_l = new PointPriority(start_loc, costStart);
		PointPriority target_l = new PointPriority(target_loc, costTarget);
		
		PriorityQueue<PointPriority> frontier =  new PriorityQueue<PointPriority>(new PointPriorityComparator());
		
		frontier.add(start_l);
		List<Int2D>cameFrom= new ArrayList<Int2D>();
		//Hashtable<PointPriority,PointPriority> cameFrom= new Hashtable<PointPriority,PointPriority>();
		// -------
		//Hashtable<PointPriority,Double> costSoFar = new Hashtable<PointPriority,Double>();
		Hashtable<Int2D,Double> costSoFar = new Hashtable<Int2D,Double>();
		costSoFar.put(start_l.getInt2D_PointPrio(), 0.0);
		//cameFrom.put(start_l, start_l);

		//cameFrom.add(start_l.getInt2D_PointPrio());
		while (frontier.size()>0) {
			PointPriority current = frontier.poll();
			//System.out.println("The prio is:"+current.getPriority() );
			Int2D currentInt2D = current.getInt2D_PointPrio();
			//System.out.println("Show X:"+currentInt2D.x + " SHOW Y:"+currentInt2D.y);
			
			if(currentInt2D.equals(target_l.getInt2D_PointPrio())) {
				//cameFrom.put(target_l, target_l);
				break;
			}
			IntBag xbag = new IntBag();
			IntBag ybag = new IntBag();
			
			mapper.knownWorld.getNeighborsMaxDistance(currentInt2D.x,currentInt2D.y, 1, false,xbag,ybag);
			removeOrigin(currentInt2D.x, currentInt2D.y,xbag,ybag);
			
			
			Bag tmpNeighbors = convertNeighborsToPointPrio(xbag,ybag);
			
			for(int i =0; i<tmpNeighbors.numObjs;i++) {

				PointPriority next = (PointPriority)tmpNeighbors.get(i);
				
				if (next.getPriority()==COST_PATH_WALL) {
					continue;
				}
				
				double newCost = costSoFar.get(current.getInt2D_PointPrio()) + next.getPriority();
				
				if (!costSoFar.containsKey(next.getInt2D_PointPrio()) || newCost < costSoFar.get(next.getInt2D_PointPrio())) {
					
					//costSoFar.put(tmpPoint, newCost);
					double prio = newCost+ Heuristic(next, target_l );
					
					//put in the frontier priorityQueue the next point with new cost updated
					PointPriority toAddFrontier = new PointPriority(next.getInt2D_PointPrio(),prio);
					frontier.add(toAddFrontier);
					costSoFar.put(toAddFrontier.getInt2D_PointPrio(), newCost);
					//cameFrom.put(current,toAddFrontier);
					if (!cameFrom.contains(current.getInt2D_PointPrio())) 
					cameFrom.add(current.getInt2D_PointPrio());
					
				}
				
			}
			
			/*
			for (int a=0;a<frontier.size();a++) {
				PointPriority currenttmp = frontier.remove();
				System.out.println(">>>>>>> + +"+currenttmp.toString());
				System.out.println("For index a:"+a+" la prio est:"+currenttmp.getPriority());
			}
				*/
		}
		//cameFrom.entrySet().forEach( entry -> {
		//    System.out.println( entry.getKey().getInt2D_PointPrio() + " --> " + entry.getValue().getInt2D_PointPrio() );
		//});
		
		cameFrom.add(target_loc);
		
		for (int i = 0;i<cameFrom.size();i++) {
			System.out.print("index "+i+" :"+cameFrom.get(i)+" ..");
		}
		
		System.out.println("");
		cameFrom.remove(0);
		return cameFrom;
		
	}
	
	
	
	
	
	/////////////////////// UTILITIES:
	
	
	public Bag convertNeighborsToPointPrio(IntBag xbag, IntBag ybag) {
		Bag resBag = new Bag();
		for (int i = 0 ;i<xbag.size();i++) {
			Int2D tmp = (new Int2D(xbag.get(i),ybag.get(i)));
			PointPriority pptmp = new PointPriority( tmp , getCostOfCell(tmp));
			resBag.add(pptmp);
			
		}
		
		//System.out.println(resBag.size());
		return resBag;
		
		
	}
	
	
	
	
	
	
	/**
	 * Removes the origin from the neighbor hood . we only get moore neighbors;

	 */
	protected void removeOrigin(int x, int y, IntBag xPos, IntBag yPos)
    {
    int size = xPos.size();
    for(int i = 0; i <size; i++)
        {
        if (xPos.get(i) == x && yPos.get(i) == y)
            {
            xPos.remove(i);
            yPos.remove(i);
            return;
            }
        }
    }
	
	public void printBag(IntBag b) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<b.size();i++) {
			//System.out.println(o.toString());
			sb.append(b.get(i)+" ");
		}
		
		//System.out.println(sb.toString());
	}
	
	
	
	
	
	
	
	public double getCostOfCell(Int2D loc) {
		double cost=0;
		Bag temp = mapper.knownWorld.getObjectsAtLocation(loc.x, loc.y);
		
		if (temp!=null) {
			for (Object o : temp) {
				if (o instanceof Tree)cost = COST_PATH_TREE;
				else if (o instanceof Water) cost = COST_PATH_WATER;
				else if (o instanceof House) cost = COST_PATH_HOUSE;
				else if (o instanceof Wall) cost = COST_PATH_WALL;
				else cost = 1;
			}
			
		}
		else {
			cost = COST_PATH_UNKNOWN;
		}
		
		
		
		return cost;
	}
	
	
	//GETTERS & SETTERS
	
	
	
	
}


