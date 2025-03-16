package main;
import controller.ControllerSnakeGame;
import model.InputMap;
import model.SnakeGame;
import strategy.ApproximateQLearning_solo;
import strategy.DeepQLearningStrategy;
import strategy.Strategy;
import strategy.StrategyRandom;
import strategy.TabularQLearning_solo;
import utils.AgentAction;
import view.PanelSnakeGame;
import view.ViewCommand;
import view.ViewSnakeGame;

public class main_debugMode {

	public static void main(String[] args) {
		
		double gamma = 0.95;
		double epsilon = 0.2;
		double alpha = 0.01;

		boolean randomFirstApple = true;
		
		
		String layoutName = "layouts/alone/small_alone_with_walls.lay";
		
		InputMap inputMap = null;
		
		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		
		arrayStrategies[0] = new StrategyRandom();
		

		
		for(int j =0; j < arrayStrategies.length; j++) {
			
			arrayStrategies[j].setModeTrain(true);

		}
		
		
		SnakeGame snakeGame = new SnakeGame(100, inputMap, randomFirstApple);
		snakeGame.setStrategies(arrayStrategies);
		
		snakeGame.init();


		
		ControllerSnakeGame controllerSnakeGame = new ControllerSnakeGame();
		
		controllerSnakeGame.setGame(snakeGame);
		
		
		PanelSnakeGame panelSnakeGame = new PanelSnakeGame(inputMap.getSizeX(), inputMap.getSizeY(), inputMap.get_walls(), inputMap.getStart_snakes(), inputMap.getStart_items());
	
		ViewSnakeGame viewSnakeGame = new ViewSnakeGame(controllerSnakeGame,  panelSnakeGame);
		
		viewSnakeGame.addSnakeGame(snakeGame);
		
		ViewCommand viewCommand = new ViewCommand(controllerSnakeGame, snakeGame);

	}
	
	



}
