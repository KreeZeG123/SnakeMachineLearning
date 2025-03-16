package controller;

import java.util.ArrayList;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import model.Game;
import model.InputMap;
import utils.AgentAction;
import view.PanelSnakeGame;
import view.ViewSnakeGame;
import view.ViewCommand;



public class ControllerSnakeGame extends AbstractController {


	
	public void setGame(Game game){
		
		this.game = game;
	}

	public void goUp(){
        ((SnakeGame)this.game).setInputMoveHuman1(AgentAction.MOVE_UP);
	}
	
	public void goDown(){
		((SnakeGame)this.game).setInputMoveHuman1(AgentAction.MOVE_DOWN);
	}	
	
	public void goLeft(){
		((SnakeGame)this.game).setInputMoveHuman1(AgentAction.MOVE_LEFT);
	}	
	
	public void goRight(){
		((SnakeGame)this.game).setInputMoveHuman1(AgentAction.MOVE_RIGHT);
	}	



}
