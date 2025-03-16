package controller;

import model.Game;
import view.ViewCommand;

public abstract class AbstractController {

	Game game;
	

	public void restart() {
		this.game.pause();
		this.game.init();
	}


	public void step() {
		
		this.game.step();
		
	}


	public void play() {
		
		this.game.launch();
		
	}


	public void pause() {
		this.game.pause();
		
	}


	
	public void setSpeed(double speed) {
		
		long time = (long) (1000/speed);
		this.game.setTime(time);
		
	}
	
}
