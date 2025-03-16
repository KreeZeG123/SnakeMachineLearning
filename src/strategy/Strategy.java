package strategy;

import agent.Snake;
import model.SnakeGame;

import utils.AgentAction;


public abstract class Strategy {

	private boolean modeTrain;
	
	protected int nbActions;
	protected double epsilon;
	protected double base_epsilon;
	protected double gamma;
	protected double alpha;

	public Strategy() {
	}
	
	public Strategy(int nbActions, double epsilon, double gamma, double alpha) {
		
		this.nbActions = nbActions;
		this.epsilon = epsilon;
		this.base_epsilon = epsilon;
		this.gamma = gamma;
		this.alpha = alpha;
	}
	
	public abstract AgentAction chooseAction(int idxSnake, SnakeGame snakeGame);

	
	public abstract void update(int idx, SnakeGame state,  AgentAction action, SnakeGame nextState, int reward, boolean isFinalState);
	
	
	public boolean isModeTrain() {
		return modeTrain;
	}


	public void learn() {
		
	}

	
	public void setModeTrain(boolean modeTrain) {
		
		this.modeTrain = modeTrain;
		
		if(this.modeTrain) {
			this.epsilon = this.base_epsilon;
		} else {
			this.epsilon = 0;
			
		}
	}


	

	
}
