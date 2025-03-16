package strategy;

import agent.Snake;
import model.SnakeGame;
import utils.AgentAction;

public class StrategyDown extends Strategy{




	public AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {
		
		return AgentAction.MOVE_DOWN;
		
	}

	@Override
	public void update(int idx, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		// TODO Auto-generated method stub
		
	}


	
}
