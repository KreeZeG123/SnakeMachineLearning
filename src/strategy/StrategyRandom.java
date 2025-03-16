package strategy;

import java.util.Random;

import agent.Snake;
import model.SnakeGame;
import utils.AgentAction;

public class StrategyRandom extends Strategy{



	@Override
	public AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {
		
		AgentAction[] listActions = AgentAction.values();
		
		Random rand = new Random();
		
        int randomIndex = rand.nextInt(listActions.length);
        
        
		return listActions[randomIndex];
		
	}

	@Override
	public void update(int idx, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		// TODO Auto-generated method stub
		
	}


}
