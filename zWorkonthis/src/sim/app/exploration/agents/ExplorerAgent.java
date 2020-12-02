package sim.app.exploration.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import sim.app.exploration.env.SimEnvironment;
import sim.app.exploration.objects.Prototype;
import sim.app.exploration.objects.SimObject;
import sim.app.exploration.utils.PointPriority;
import sim.app.exploration.utils.Utils;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;

public class ExplorerAgent implements sim.portrayal.Oriented2D {

	private static final long serialVersionUID = 1L;
	private float INTEREST_THRESHOLD = 60;
	private final double STEP = Math.sqrt(2);
	private final int viewRange = 4;
	
	private int identifyClock;

	private Int2D loc;
	private Int2D target;
	private double orientation;

	public SimEnvironment env;
	public BrokerAgent broker;
	public MapperAgent mapper;
	//new attributes: -----------------------------
	public PathfinderAgent pathfinder;
	//Hashtable<PointPriority,PointPriority> pathToNextTarget =  new Hashtable<PointPriority,PointPriority>();
	List<Int2D> pathToNextTarget = new ArrayList<Int2D>();
	public QlearnAgent qlearn;
	//-------------------
	
	private Vector<Prototype> knownObjects;

	private boolean GLOBAL_KNOWLEDGE = true;
	private int IDENTIFY_TIME = 15;

	public ExplorerAgent(Int2D loc) {
		this.loc = loc;
		this.orientation = 0;
		this.target = null;
		this.knownObjects = new Vector<Prototype>();
		this.identifyClock = 0;
	}

	public void step(SimState state) {

		// The explorer sees the neighboring objects and sends them to the
		// mapper
		if (identifyClock == 0) {
			Bag visible = env.getVisibleObejcts(loc.x, loc.y, viewRange);

			// -------------------------------------------------------------
			for (int i = 1; i < visible.size(); i++) {
				SimObject obj = (SimObject) visible.get(i);

				if (!mapper.isIdentified(obj.loc)) {
					Hashtable<Class, Double> probs = getProbabilityDist(obj);

					float interest = getObjectInterest(probs);
					//System.out.println("OBJECT AT: (" + obj.loc.x + ","
					//		+ obj.loc.y + "). INTEREST: " + interest);

					// If not interesting enough, classify it to the highest prob
					if (interest < INTEREST_THRESHOLD) {
						Class highest = Utils.getHighestProb(probs);

						mapper.identify(obj, highest);
						Class real = env.identifyObject(obj.loc).getClass();
						//if (highest != real)
						//	System.err.println(real.getSimpleName());
						
						broker.removePointOfInterest(obj.loc);

					} else {
						mapper.addObject(obj);
						broker.addPointOfInterest(obj.loc, interest);

					}
				}

			}
			// --------------------------------------------------------------

			// Check to see if the explorer has reached its target
			if (target != null) {
				if (loc.distance(target) == 0) {
					target = null;

					SimObject obj = env.identifyObject(loc);

					if (obj != null) {
						broker.removePointOfInterest(obj.loc);
						mapper.identify(obj, obj.getClass());
						addPrototype(obj, obj.getClass());

						identifyClock = IDENTIFY_TIME;
					}
				}
			}

			// If the explorer has no target, he has to request a new one from
			// the broker
			if (target == null) {
				target = broker.requestTarget(loc);
				//System.out.println("NEW TARGET: X: " + target.x + " Y: "
				//		+ target.y);
			}
			
			//if we have a target, we ask the pathfinder to give us the best path
			/**
			 * COMPUTATION OF A* ALGORITHM BY PATHFINDER
			 */
			//PointPriority pploc = new PointPriority (loc,pathfinder.getCostOfCell(loc));
			
			//PointPriority pptarget = new PointPriority(target, pathfinder.getCostOfCell(target));
			
			//System.out.println("Loc x : "+loc.x + " and y :"+loc.y);
			//System.out.println("target x : "+target.x + " and y :"+target.y);
			//System.out.println("");
			//pathToNextTarget= pathfinder.computePath_Astar(loc, target);
			
			//System.out.println("SIZE PATHFINDER:"+pathToNextTarget.size());
			
			//qlearn.printWorld();
			// Agent movement
			
			/*
			Int2D newMove=null;
			if (!pathToNextTarget.isEmpty()) {
				newMove= pathToNextTarget.remove(0);
				System.out.println("WE PERFORM THIS MOVE: "+newMove.x+";"+newMove.y);
			}
			
			else {
				if(target !=null) {
					
					
					pathToNextTarget= pathfinder.computePath_Astar(loc, target);
					newMove=pathToNextTarget.remove(0);
					System.out.println("WE PERFORM THIS MOVE: "+newMove.x+";"+newMove.y);
				}
			}
			
			
			this.loc= newMove;
			newMove=null;
			
			*/
			// Agent movement
			Double2D step = new Double2D(target.x - loc.x, target.y - loc.y);
			step.limit(STEP);

			loc.x += Math.round(step.x);
			loc.y += Math.round(step.y);

			env.updateLocation(this, loc);
			mapper.updateLocation(this, loc);

			orientation = Math.atan2(Math.round(step.y), Math.round(step.x));
		}
		
		if (identifyClock > 0)
			identifyClock--;
	}

	
	private int getObjectInterest(Hashtable<Class, Double> probs) {
		double unknownInterest = 0;
		double entropyInterest;
		Vector<Double> prob = new Vector<Double>();

		for (Class c : probs.keySet()) {
			if (c == SimObject.class)
				unknownInterest = Utils.interestFunction(probs.get(c));

			prob.add(probs.get(c));
		}

		entropyInterest = Utils.entropy(prob);

		//System.out.println("ENTROPY: " + entropyInterest + " | UNKNOWN: "
		//		+ unknownInterest);

		double interest = (entropyInterest > unknownInterest ? entropyInterest : unknownInterest) * 100;

		return (int) Math.round(interest);
	}

	private void addPrototype(SimObject obj, Class class1) {
		// TODO Auto-generated method stub

		// Using the global team knowledge
		if (GLOBAL_KNOWLEDGE) {

			mapper.addPrototype(obj, class1);

			// Using only the agent's knowledge
		} else {
			for (Prototype p : this.knownObjects) {
				if (class1 == p.thisClass) {
					p.addOccurrence(obj.size, obj.color);
					return;
				}
			}

			this.knownObjects.add(new Prototype(class1, obj.size, obj.color));
		}

	}

	private Hashtable<Class, Double> getProbabilityDist(SimObject obj) {

		Hashtable<Class, Double> probs = new Hashtable<Class, Double>();

		// TODO: Implement global knowledge

		Vector<Prototype> prototypes;
		if (GLOBAL_KNOWLEDGE) {
			prototypes = mapper.knownObjects;
		} else {
			prototypes = this.knownObjects;
		}
		int nClasses = prototypes.size();
		double unknownCorr = 0;
		double corrSum = 0;

		for (Prototype prot : prototypes) {
			// TODO: Stuff here
			double corr;
			double colorDist = Utils.colorDistance(obj.color, prot.color);
			double sizeDist = Math.abs(obj.size - prot.size) / Utils.MAX_SIZE;

			// Correlation
			corr = 1 - (0.5 * colorDist + 0.5 * sizeDist);
			// Saturation
			corr = Utils.saturate(corr, prot.nOccurrs);

			probs.put(prot.thisClass, corr*corr*corr);
			corrSum += corr*corr*corr;

			if(corr > unknownCorr){
				unknownCorr = corr;
			}
		}

		if (nClasses == 0)
			unknownCorr = 1.0;
		else
			unknownCorr = 1- unknownCorr;
		
		probs.put(SimObject.class, unknownCorr*unknownCorr*unknownCorr);
		corrSum += unknownCorr*unknownCorr*unknownCorr;

		for (Class c : probs.keySet()) {
			
			probs.put(c, probs.get(c) / corrSum);
			//System.out.println(c.getSimpleName() + " : " + probs.get(c));
		}

		return probs;
	}

	@Override
	public double orientation2D() {
		return orientation;
	}

	public Int2D getLoc() {
		return loc;
	}

	public double getOrientation() {
		return orientation;
	}

}