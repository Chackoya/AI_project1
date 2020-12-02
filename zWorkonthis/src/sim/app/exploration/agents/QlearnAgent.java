package sim.app.exploration.agents;

import java.util.Random;

public class QlearnAgent {
	
	MapperAgent mapper;
	
	private int total_episodes = 25000;        //Total number of training episodes
	private int total_test_episodes = 100;     //Total number of test episodes
	private int max_steps = 200;               // Max steps per episode
	private double learning_rate = 0.01;          // Learning rate
	private double gamma = 0.99;                  // Discounting rate

	//Exploration parameters
	private double epsilon = 1.0 ;                // Exploration rate
	private double max_epsilon = 1.0;             // Exploration probability at start
	private double min_epsilon = 0.001;           // Minimum exploration probability 
	private double decay_rate = 0.01 ;            // Exponential decay rate for exploration prob
	
	
	int nbActions = 8;
	
	int w;
	int h;
	double [][]qTable;
	
	public QlearnAgent(MapperAgent mapper) {
		this.mapper = mapper;
		
		this.w= mapper.knownWorld.getWidth();
		this.h= mapper.knownWorld.getHeight();
		
		qTable = new double[w*h][nbActions];
		
		
		/*
		for (int i = 0;i<(w*h);i++) {
			for (int j =0;j<nbActions;j++) {
				qTable[i][j] = 0;
				System.out.print(qTable[i][j]+"  ");
			}
			System.out.println(""); 
		}
		
		
		System.out.println("SIZE:"+qTable.length);
		*/
	}
	
	void calculateQ() {
		
		
	        
	}
	
	
	void printWorld() {
		System.out.println(mapper.knownWorld.getHeight());
		System.out.println(mapper.knownWorld.getWidth());
	}
	
}

