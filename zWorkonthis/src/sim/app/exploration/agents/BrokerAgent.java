package sim.app.exploration.agents;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ec.util.MersenneTwisterFast;
import sim.app.exploration.core.Simulator;
import sim.app.exploration.utils.SplitMapBroker;
import sim.app.exploration.utils.SplitMapBroker.Tuple;
import sim.util.Int2D;

public class BrokerAgent {
	
	private ArrayList<PointOfInterest> pointsOfInterest;
	private ArrayList<PointOfInterest> removedPoIs;
	
	private final int modeOfSwitch=1;
	
	//---New attributes:
	private boolean firstRequest=true;
	private SplitMapBroker smp ;
	
	public Hashtable<Integer, List<Int2D>> ptsFrontier_byZone= new Hashtable<Integer, List<Int2D>>(); // pseudo frontier..
	private Hashtable<Integer, List<PointOfInterest>> ptsOfInterest_byZone= new Hashtable<Integer, List<PointOfInterest>>();
	//----
	
	public BrokerAgent(SplitMapBroker s) {
		this.pointsOfInterest = new ArrayList<PointOfInterest>();
		this.removedPoIs = new ArrayList<PointOfInterest>();
		
		//--New stuff:
		this.smp = s;
		
		for (int i=0;i<smp.numberOfSplits;i++) {
			ptsOfInterest_byZone.put(i , new ArrayList<PointOfInterest>());
			ptsFrontier_byZone.put(i,  new ArrayList<Int2D>());
		}
		
		
		//-----
	}
	public Int2D requestTarget(Int2D agentPos,ExplorerAgent agent) {

		Int2D target = null;
		PointOfInterest target_PoI = null;
		MersenneTwisterFast mtf = new MersenneTwisterFast(); //RANDOM TOOL FROM MASON (more effective it seems...)
		int zone = smp.getZoneOfTheAgent(agent);
		
		
		System.out.println("Agent num:"+agent.getID()+" of the zone:"+zone);
		// If we have no points of interest, return a random point
		//System.out.println("Agent id:"+agentID+"of zone:"+zone+" requests. Pts of interest: "+ptsOfInterest_byZone.get(zone).size());
		
		if (firstRequest) {
			target = smp.pickRandomSpotOnZone(zone);
		}
		
		
		if (ptsOfInterest_byZone.get(zone).size()==0) { // IF NO POINTS OF INTEREST WE EXPlORE THE FRONTIER CELLS OR EVENTUALLY WE SWITCH ZONE IF FRONTIER OF THE ZONE IS EMPTY 
			if(!ptsFrontier_byZone.get(zone).isEmpty()) {
				System.out.println(">0 [BROKER]Frontier size:"+ptsFrontier_byZone.get(zone).size());
				//target = ptsFrontier_byZone.get(zone).get(mtf.nextInt(ptsFrontier_byZone.get(zone).size())); // RANDOM FRONTIER APPROACH
				target = getNearestFrontierPoint(zone,agentPos); // NEAREST FRONTIER APPROACH
				System.out.println("1[BROKER]>>>>>GOING HERE POINT FROM FRONTIER:"+target+" IN THE ZONE:"+zone);
				return target;
			}
			else{
				int new_zone=smp.switch_zone_agent(zone,agent,modeOfSwitch);
				target = smp.pickRandomSpotOnZone(new_zone);
				System.out.println("2 [BROKER]>>>>>GOING FOR RANDOM POINT:"+target+" IN THE ZONE:"+new_zone);
				return target;//smp.pickRandomSpotOnZone(new_zone);
			}
		}
		
		
		// Else, find the best point of Interest
		else {
			System.out.println("3 [BROKER]GOING FOR POINT OF INTEREST");
			double bestScore = Double.NEGATIVE_INFINITY;
			double score;
			
			for (PointOfInterest PoI : ptsOfInterest_byZone.get(zone)) {
				score = PoI.interestMeasure - ( (agentPos.distance(PoI.loc) * 100) / Simulator.limitRadius);
				
				//System.out.println("[Broker] Score for " + PoI + ": " + score);
				
				if (score > bestScore) {
					bestScore = score;
					target = PoI.loc;
					target_PoI = PoI;
				}
			}
			
			// If the target is too far, send a random target
			if (bestScore < 0) {
				if (ptsOfInterest_byZone.get(zone).size()==0) {
					if(!ptsFrontier_byZone.get(zone).isEmpty()) {
						//target = ptsFrontier_byZone.get(zone).get(mtf.nextInt(ptsFrontier_byZone.get(zone).size())); // RANDOM APPROACH 
						target = getNearestFrontierPoint(zone,agentPos); // NEAREST FRONTIER APPROACH
						System.out.println("4[BROKER]>>FROM PT OF INTEREST -> GOING HERE POINT FROM FRONTIER:"+target);
						return target;
					}
					else{
						//System.out.println("5 [BROKER]>>FROM PT OF INTEREST -> GOING FOR RANDOM POINT GG FF:"+target);
						return smp.pickRandomSpotOnZone(zone);
					}
				}
			}
			
			// Remove the target from the list of Points of Interest and add it to the removed list (this should be done when you arrive at the point if you're constantly calculating new targets)
			if (target_PoI != null) {
				
				ptsOfInterest_byZone.get(zone).remove(target_PoI);
				removedPoIs.add(target_PoI);
			}
			
			//System.out.println("[Broker] Best score: " + bestScore);
			//System.out.println("[Broker] Target: " + target);
		}
		
		return target;
	}
	static public double Heuristic(Int2D a,Int2D b)
    {
		//return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return Math.sqrt(Math.pow(a.x - b.x,2)+ Math.pow(a.y - b.y,2));
    }
	public Int2D getNearestFrontierPoint(int zone, Int2D agentPos) {
		double minDist = Heuristic(agentPos,ptsFrontier_byZone.get(zone).get(0));
		Int2D resultLocation = ptsFrontier_byZone.get(zone).get(0);
		
		for(int i = 1 ; i<ptsFrontier_byZone.get(zone).size();i++) {
			double tmpHeuristic = Heuristic(agentPos,ptsFrontier_byZone.get(zone).get(i));
			if (tmpHeuristic < minDist) {
				minDist=tmpHeuristic;
				resultLocation = ptsFrontier_byZone.get(zone).get(i);
				System.out.println("Cost:"+minDist+" point:"+resultLocation);
			}
			
		}
		return resultLocation;
	}
	
	public void addFrontierPoint(Int2D p){
		
		for (Entry<Integer, Tuple<Int2D, Int2D>> entry :smp.zonesForAgents_dict.entrySet()) {
            Integer key = entry.getKey();
            Tuple<Int2D, Int2D> value = entry.getValue();
            //Tup x->(left) y->(right) 
            if(value.x.x <= p.x && p.x <value.y.x && value.x.y<=p.y && p.y<value.y.y) {
            	
            	if(!ptsFrontier_byZone.get(key).contains(p)) {
            		//System.out.println("Adding PoI to zone:"+key+" LIST:"+ptsOfInterest_byZone.get(key).size()+" the point" +p);
            		ptsFrontier_byZone.get(key).add(p);
            	}
            }
            //System.out.println("Size list:"+ptsOfInterest_byZone.get(key).size());
            
        }
	}
	public void removeFrontierPoint(Int2D loc ) {
		for (Entry<Integer, List<Int2D>> entry : ptsFrontier_byZone.entrySet()) {
            Integer key = entry.getKey();
            List<Int2D> value = entry.getValue();
            //Tup x->(left) y->(right) 
            if (value.contains(loc)) {
            	value.remove(loc);
            }
            
        }
	}
	public void addPointOfInterest(Int2D point, double interestMeasure) {
		PointOfInterest p = new PointOfInterest(point, interestMeasure);

		for (Entry<Integer, Tuple<Int2D, Int2D>> entry :smp.zonesForAgents_dict.entrySet()) {
            Integer key = entry.getKey();
            Tuple<Int2D, Int2D> value = entry.getValue();
            //Tup x->(left) y->(right) 
            

            if(value.x.x <= p.loc.x && p.loc.x <value.y.x && value.x.y<=p.loc.y && p.loc.y<value.y.y) {
           
            	//System.out.println("Adding PoI to zone:"+key+" LIST:"+ptsOfInterest_byZone.get(key));
            	if(!ptsOfInterest_byZone.get(key).contains(p))
            		ptsOfInterest_byZone.get(key).add(p);
            }
            
            
        }
		
	}
	
	public void removePointOfInterest(Int2D loc ) {
		PointOfInterest p = new PointOfInterest(loc, 1);
		for (Entry<Integer, List<PointOfInterest>> entry : ptsOfInterest_byZone.entrySet()) {
            Integer key = entry.getKey();
            List<PointOfInterest> value = entry.getValue();
            //Tup x->(left) y->(right) 
            if (value.contains(p)) {
            	value.remove(p);
            }
            
        }
	}
	
	
	
	
	
	/**####################################################################################################################################################
	 * @OLD 
	 * @METHODS_FROM_ORIGINAL_WORK2010
	 * 
	 */
	public Int2D requestTargetV2(Int2D agentPos,int agentID) {

		Int2D target = null;
		PointOfInterest target_PoI = null;
		
		// If we have no points of interest, return a random point
		System.out.println("Agent id:"+agentID+" requests. Pts of interest: "+pointsOfInterest.size());
		
		
		if (pointsOfInterest.size() == 0) {
			return getLimitedRandomTarget(agentPos);
			//return getRandomTarget();
		}
		// Else, find the best point of Interest
		else {
			
			double bestScore = Double.NEGATIVE_INFINITY;
			double score;
			
			for (PointOfInterest PoI : pointsOfInterest) {
				score = PoI.interestMeasure - ( (agentPos.distance(PoI.loc) * 100) / Simulator.limitRadius);
				
				//System.out.println("[Broker] Score for " + PoI + ": " + score);
				
				if (score > bestScore) {
					bestScore = score;
					target = PoI.loc;
					target_PoI = PoI;
				}
			}
			
			// If the target is too far, send a random target
			if (bestScore < 0)
				return getLimitedRandomTarget(agentPos);
			
			// Remove the target from the list of Points of Interest and add it to the removed list (this should be done when you arrive at the point if you're constantly calculating new targets)
			if (target_PoI != null) {
				pointsOfInterest.remove(target_PoI);
				removedPoIs.add(target_PoI);
			}
			
			//System.out.println("[Broker] Best score: " + bestScore);
			//System.out.println("[Broker] Target: " + target);
		}
		
		return target;
	}
	
	public void addPointOfInterestV2(Int2D point, double interestMeasure) {
		PointOfInterest PoI = new PointOfInterest(point, interestMeasure);
		
		if (!pointsOfInterest.contains(PoI) && !removedPoIs.contains(PoI)) {
			pointsOfInterest.add(PoI);
			//System.out.println("[Broker] PoI added: " + PoI.loc);
		}
	}

	public void removePointOfInterestV2(Int2D loc) {
		PointOfInterest tmp = new PointOfInterest(loc, 1);
		
		if (pointsOfInterest.contains(tmp)) {
			//System.out.println("[Broker] Removing " + loc + " ("+ pointsOfInterest.size() + ")");
			pointsOfInterest.remove(tmp);
			//System.out.println("[Broker] Now with " + pointsOfInterest.size());
			
			removedPoIs.add(tmp);
		}
	}
	
	public Int2D getLimitedRandomTarget(Int2D agentPos) {
		Int2D target = null;
		
		while (true) {
			target = getRandomTarget();
			if (agentPos.distance(target) <= Simulator.limitRadius)
				break;
		}
		
		return target; 
	}
	
	public Int2D getRandomTarget() {
		return new Int2D((int)(Math.random()*Simulator.WIDTH), (int)(Math.random()*Simulator.HEIGHT)); 
	}
	
	
	
	
	
}
///////////////////////////////////////////////////////////////////////////////////////////////

class PointOfInterest {
	public Int2D loc;
	public double interestMeasure;	// I expect this to be in [0, 100]
	
	PointOfInterest(Int2D loc, double interestMeasure) {
		this.loc = loc;
		this.interestMeasure = interestMeasure;
	}
	
	@Override
	public boolean equals(Object o_PoI) {
		PointOfInterest PoI = (PointOfInterest) o_PoI;
		return this.loc.equals(PoI.loc);
	}
	
	public String toString() {
		return "[" + loc.x + ", " + loc.y + " - " + interestMeasure + "]";
	}
}
