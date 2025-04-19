package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import controller.ControllerSnakeGame;
import model.Game;
import model.InputMap;
import model.SnakeGame;
import strategy.ApproximateQLearning_duel;
import strategy.ApproximateQLearning_solo;
import strategy.DeepQLearningStrategy;
import strategy.Strategy;
import strategy.StrategyAdvanced;
import strategy.StrategyRandom;
import strategy.TabularQLearning_duel;
import strategy.TabularQLearning_solo;
import utils.AgentAction;
import view.PanelSnakeGame;
import view.ViewCommand;
import view.ViewSnakeGame;



public class main_batchMode {

	public static void main(String[] args) {

		double gamma = 0.95;
		double epsilon = 0.2;
		double alpha = 0.1;

		boolean randomFirstApple = true;	
		
		String layoutName = "layouts/alone/small_alone_with_walls.lay";

		InputMap inputMap = null;
		
		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ControllerSnakeGame controllerSnakeGame = new ControllerSnakeGame();
		PanelSnakeGame panelSnakeGame = new PanelSnakeGame(inputMap.getSizeX(), inputMap.getSizeY(), inputMap.get_walls(), inputMap.getStart_snakes(), inputMap.getStart_items());
		
		ViewSnakeGame viewSnakeGame = new ViewSnakeGame(controllerSnakeGame, panelSnakeGame);

		
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		
		 
			
		//// Préciser ici les stratégies pour chaque Snake
		//arrayStrategies[0] = new StrategyAdvanced();
		//arrayStrategies[0] = new TabularQLearning_solo(4, epsilon, gamma, alpha);
		//arrayStrategies[0] = new TabularQLearning_duel(4, epsilon, gamma, alpha);
		arrayStrategies[0] = new ApproximateQLearning_solo(4, epsilon, gamma, alpha);
		//arrayStrategies[0] = new ApproximateQLearning_duel(4, epsilon, gamma, alpha);
		/*arrayStrategies[0] = new DeepQLearningStrategy(
				AgentAction.values().length,
				epsilon,
				0.9995,
				0.1,
				gamma,
				alpha,
				1,
				32,
				5 * 5 * 5
		);*/

		//arrayStrategies[1] = new StrategyRandom();
		//arrayStrategies[1] = new TabularQLearning_duel(4, epsilon, gamma, alpha);
		//arrayStrategies[1] = new TabularQLearning_solo(4, epsilon, gamma, alpha);
		//arrayStrategies[1] = new StrategyAdvanced();

		
	   
			

	    	
		//Nombre de simulations séquentielles lancees pour calculer la recompense moyenne en mode train
		int Ntrain = 100;
		
		
		//Nombre de simulations parallèle lancees pour calculer la recompense moyenne en mode test
		int Ntest = 100;
		
		
		//Nombre max de tours d'une partie de snake
		int maxTurnSnakeGame = 100;
		
		

		for(int cpt = 1; cpt < 10000000; cpt++) {
			
			System.out.println("Generation " + cpt);

			System.out.println("Compute score in test mode");
			launchParallelGames(Ntest, maxTurnSnakeGame, inputMap, arrayStrategies, false, randomFirstApple);
					
			if(cpt%100 == 0) {

				System.out.println("Visualization mode");
				vizualize(maxTurnSnakeGame, inputMap, arrayStrategies, false, randomFirstApple, controllerSnakeGame,  viewSnakeGame);
				System.out.println("End Vizualise");
			}
			
			
			System.out.println("Play and collect examples - train mode");
			launchParallelGames(Ntrain, maxTurnSnakeGame, inputMap, arrayStrategies, true, randomFirstApple);

			
			
			for(int i = 0; i < arrayStrategies.length; i++) {
				arrayStrategies[i].learn();
			}

		}
		

	}
	
	
	
	

	
	public static void launchParallelGames(int nbGames, int maxTurnSnakeGame, InputMap inputMap, Strategy[] arrayStrats, boolean modeTrain, boolean randomFirstApple) {
		

		double[] scoreStrats = new double[arrayStrats.length];
		
		ArrayList<SnakeGame> snakeGames = new ArrayList<SnakeGame>();
		
		System.out.println("Build games " + modeTrain);

		for(int i = 0; i < nbGames; i++ ) {

			for(int j =0; j < arrayStrats.length; j++) {
				arrayStrats[j].setModeTrain(modeTrain);
			}
			
			
			SnakeGame snakeGame = new SnakeGame(maxTurnSnakeGame, inputMap, randomFirstApple);
			snakeGame.setStrategies(arrayStrats);
			snakeGame.init();
			
			snakeGame.setTime(0);
			
			
			snakeGames.add(snakeGame);
				

		}
		
		System.out.println("Start games " + modeTrain);
		
		for(int i = 0; i < nbGames; i++ ) {
		
			//System.out.println("launch game " + i);
			snakeGames.get(i).launch();
			
		}
		

		for(int i = 0; i < nbGames; i++ ) {
				
			try {
				((Game) snakeGames.get(i)).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for(int j =0; j < arrayStrats.length; j++) {
				
				scoreStrats[j] += snakeGames.get(i).getTabTotalScoreSnakes()[j];
				
			}
			
		}


		
		System.out.println("Print scores " + modeTrain);
		
		for(int j =0; j < arrayStrats.length; j++) {
			
			if(modeTrain) {
				System.out.println("Train - agent " + j + " - strategy " + arrayStrats[j] + " average global score : " + scoreStrats[j]/nbGames);
			} else {
				System.out.println("Test - agent " + j + " - strategy " + arrayStrats[j] + " average global score : " + scoreStrats[j]/nbGames);
			}
			
		}
		

		
	}


	
	
	
	private static void vizualize(int maxTurnSnakeGame, InputMap inputMap, Strategy[] arrayStrats, boolean modeTrain, boolean randomFirstApple,
			ControllerSnakeGame controllerSnakeGame, ViewSnakeGame viewSnakeGame) {
		
		
	
		
		System.out.println("Visualize new game");
		
		SnakeGame snakeGame = new SnakeGame(maxTurnSnakeGame, inputMap, randomFirstApple);
		
		for(int j =0; j < arrayStrats.length; j++) {
			
			arrayStrats[j].setModeTrain(modeTrain);

		}
		
		snakeGame.setStrategies(arrayStrats);
		snakeGame.setTime(200);
		
		snakeGame.init();
		

		viewSnakeGame.addSnakeGame(snakeGame);

		
		
		snakeGame.run();
		System.out.println("Time " + snakeGame.getTime());

		System.out.println("=======================");
		

//		try {
//			snakeGame.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
	
	
}
