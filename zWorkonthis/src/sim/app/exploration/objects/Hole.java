package sim.app.exploration.objects;

import java.awt.Color;

import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Int2D;

public class Hole extends SimObject{

	final static double size =1.0;
	
	public static final int RED_DELTA = 0;
	public static final int GREEN_DELTA = 0;
	public static final int BLUE_DELTA = 250;
	public static final double SIZE_DELTA = 1;
	
	public Hole(){
		super();
	}
	
	public Hole(int x, int y){
		super(new Int2D(x,y), Color.BLUE, size);
		this.introduceRandomness(RED_DELTA, GREEN_DELTA, BLUE_DELTA, SIZE_DELTA);
	}
	
	public Hole(Int2D loc, Color color, double size){
		super(loc,color,size);
	}
	
	
	public static Portrayal getPortrayal(){
		return new RectanglePortrayal2D(Color.BLUE, size);
	}
	
}
