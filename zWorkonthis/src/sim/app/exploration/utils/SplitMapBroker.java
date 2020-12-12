package sim.app.exploration.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import ec.util.MersenneTwisterFast;
import sim.app.exploration.agents.ExplorerAgent;
import sim.util.Int2D;

public class SplitMapBroker {
	
	private int w;
	private int h;
	public int numberOfSplits;
	
	private int nbAgents;
	
	private Hashtable<Integer, List<ExplorerAgent>> assigned= new Hashtable<Integer, List<ExplorerAgent>>();
	/** dictionnay assigned:
	 * key : zone of the map(int)
	 * value : list int : contains the agents IDS for each zone that they are assigned, 
	 */
	
	
	
	public Hashtable<Integer, Tuple<Int2D,Int2D>> zonesForAgents_dict = new Hashtable<Integer, Tuple<Int2D,Int2D>>();
	//Dictionnay:
	//key-> zone of the map number format: +number
	//value-> Tuple composed of 2 INT2D , first if the start of the zone , 2nd the end of the zone(indexes)  rectangular format
	
	public SplitMapBroker ( int w, int h , int numberOfSplits,int nbAgents) {
		this.w=w;
		this.h=h;
		this.numberOfSplits = numberOfSplits;
		this.nbAgents= nbAgents;
		
		for (int i = 0 ;i<numberOfSplits;i++) {
			//List<Integer> tmpL = new ArrayList<Integer>();
			assigned.put(i, new ArrayList<ExplorerAgent>());
		}
		
		
	}

	public void gen_ZoneBounds() {
		
		List<Tuple<Integer,Integer>> coords = new ArrayList<Tuple<Integer,Integer>>();
		for (int i =1 ; i<numberOfSplits+1;i++) {
			for (int j = 1; j<numberOfSplits+1;j++) {
				if (i*j==numberOfSplits) {
					if( ( w/i==(int)(w/i)) && ((h/j)==(int)(h/j))) {
						coords.add(new Tuple<Integer,Integer>(w/i ,h/j));
					}
				}
			}
		}
		
		for (int i =0;i<coords.size();i++) {
			System.out.println(coords.get(i).x+" zz :"+ coords.get(i).y);
		}
		
		Random rand = new Random(); 
        Tuple<Integer,Integer> tup = coords.get(rand.nextInt(coords.size())); 
        
        System.out.println("Tuple x:"+tup.x + " & Y:"+tup.y);
        
        int zoneNumber = 0;
        for (int i=1;i<w+1;i++) {
        	for (int j=1;j<h+1;j++) {
        		if(i%tup.x==0 && j%tup.y==0) {
        			zonesForAgents_dict.put(zoneNumber, new Tuple<Int2D,Int2D>( new Int2D((i-tup.x) , (j-tup.y)) , new Int2D(i,j) ));
        			zoneNumber++;
        		}
        	}
        }
        
        System.out.println("Dict:"+zonesForAgents_dict.size());
        for (Map.Entry<Integer,Tuple<Int2D,Int2D> > entry : zonesForAgents_dict.entrySet()) {
            Integer key = entry.getKey();
            Tuple<Int2D,Int2D> value = entry.getValue();
            
            System.out.println ("Key: " + key + "  ||  Value X: " + value.x + ";" + " & Y: "+value.y);
        }
        
	}
	
	
	public Int2D pickRandomSpotOnZone(int zone) {
		Int2D res= new Int2D();
		Random rand = new Random();
		int max_x = zonesForAgents_dict.get(zone).y.x;
		int min_x = zonesForAgents_dict.get(zone).x.x;
		res.x = rand.nextInt((max_x - min_x)+1)+min_x;
		
		int max_y = zonesForAgents_dict.get(zone).y.y;
		int min_y = zonesForAgents_dict.get(zone).x.y;
		
		res.y = rand.nextInt((max_y - min_y)+1)+min_y;
		return res;
		
		
	}
	
	public void assignZones(Vector<ExplorerAgent> vectorExpAgts) {
		MersenneTwisterFast mtf = new MersenneTwisterFast();
		for(int i = 0;i<vectorExpAgts.capacity();i++) {
			int tmpRandom = mtf.nextInt(numberOfSplits);
			//System.out.println(tmpRandom);
			//System.out.println(assigned.get(tmpRandom));
			assigned.get(tmpRandom).add(vectorExpAgts.get(i));
		}
		
		/*
		for(int i = 0;i<nbAgents;i++) {
			int tmpRandom = mtf.nextInt(numberOfSplits);
			System.out.println(tmpRandom);
			System.out.println(assigned.get(tmpRandom));
			assigned.get(tmpRandom).add(i);
		}
		*/
	}
	
	public int getZoneOfTheAgent(ExplorerAgent agent) {

		for (Map.Entry<Integer,List<ExplorerAgent> > entry : assigned.entrySet()) {
            Integer key = entry.getKey();
            List<ExplorerAgent> value = entry.getValue();
            
            for (int i =0;i<value.size();i++) {
            	if(value.get(i)==agent) {
            		return (int)key;
            	}
            }
        }
		return 0;
	}

	public int switch_zone_agent(int previousZoneNumber , ExplorerAgent agent, int modeOfTheSwitch) {
		//0 FOR RANDOM ;
		//1 FOR BASED ON THE ZONE WITH THE LOWEST NUMBER OF AGENTS ; 
		//2 FOR DISTANCE BASED SWITCH
		
		if (modeOfTheSwitch==0) { // we assign a random zone different from the previous one
			System.out.println("Switching randomly...");
			int new_zoneForAgt;
			MersenneTwisterFast mtf = new MersenneTwisterFast();
			do {
				new_zoneForAgt = mtf.nextInt(numberOfSplits);
			}while(new_zoneForAgt==previousZoneNumber);
			assigned.get(previousZoneNumber).remove(agent);
			System.out.println("ASSIGNED SIZE:"+assigned.size());
			assigned.get(new_zoneForAgt).add(agent);
			return new_zoneForAgt;
			
		}
		
		else if (modeOfTheSwitch==1){ // we assign a zone based on the 
			System.out.println("Switching based on the lowest frequency zone");
			int min_agts = assigned.get(0).size();
			int new_zoneNumber=0;
			for (Map.Entry<Integer,List<ExplorerAgent> > entry :assigned.entrySet()) {
	            Integer key = entry.getKey();
	            List<ExplorerAgent> value = entry.getValue();
	            if(value.size()<min_agts) {
	            	min_agts = value.size();
	            	new_zoneNumber = key;
	        		
	            }
	            
			}
			assigned.get(previousZoneNumber).remove(agent);
			assigned.get(new_zoneNumber).add(agent);
			return new_zoneNumber;
		}
		else {
			return previousZoneNumber;
		}
		
	}
	
	public void debugPrints(int zoneNumber) {
		//System.out.println("Random location gen:"+pickRandomSpotOnZone(zoneNumber));
		
		for (Map.Entry<Integer,List<ExplorerAgent> > entry :assigned.entrySet()) {
            Integer key = entry.getKey();
            List<ExplorerAgent> value = entry.getValue();
            
            for (int i =0;i<value.size();i++) {
            	System.out.println("Agent number "+value.get(i).getID()+" is in the zone "+key);
            }
        }
		
		
		
		
	}
	
	/*
	public static void main(String[]args) {
		
		SplitMapBroker smp = new SplitMapBroker(400,300,2,4);
		smp.gen_ZoneBounds();
		int zoneNumber= 1;
		
		//String namezone= "Zone_"+zoneNumber;
		
		System.out.println("Random location gen:"+smp.pickRandomSpotOnZone(zoneNumber));
		
		smp.assignZones();
		System.out.println("");
		
		for (Map.Entry<Integer,List<Integer> > entry : smp.assigned.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            
            for (int i =0;i<value.size();i++) {
            	System.out.println("Agent number "+value.get(i)+" is in the zone "+key);
            }
        }
		
		
		
		
	}
	*/
	
	
	//Tuple pair class
	public class Tuple<X, Y> { 
		  public final X x; 
		  public final Y y; 
		  public Tuple(X x, Y y) { 
		    this.x = x; 
		    this.y = y; 
		  } 
		} 
	
}
