package agent;

import java.io.Serializable;
import java.util.ArrayList;

import model.SnakeGame;
import strategy.Strategy;
import utils.AgentAction;
import utils.ColorSnake;

import utils.Position;

public class Snake implements Serializable{



	ArrayList<Position> positions;

	private AgentAction lastMove;


	private int invincibleTimer;
	private int sickTimer;

	transient Strategy strategy;


	int oldTailX = -1;
	int oldTailY = -1;

	private int id;
	
	boolean isDead;
	


	transient ColorSnake colorSnake;



	public Snake(Position position, AgentAction lastMove,  int id, ColorSnake colorSnake) {

		this.positions = new ArrayList<Position>();
		
		this.positions.add(position);
		
		
		this.setId(id);


		this.setInvincibleTimer(-1);
		this.setSickTimer(-1);
		
		this.isDead = false;
		
		this.colorSnake = colorSnake;
		
		this.setLastMove(lastMove);

	}


	public AgentAction play(SnakeGame game) {

	
		return strategy.chooseAction(this.id, game);

	}

	

	public void move(AgentAction action, SnakeGame game) {

		Position head = this.positions.get(0);

		// Store old tail position
		this.oldTailX = this.positions.get(positions.size() - 1).getX();
		this.oldTailY = this.positions.get(positions.size() - 1).getY();
		
		
		// Move body
		if(this.positions.size() > 1) {
			for(int i = 1; i < this.positions.size(); i++) {	
				
				positions.get(positions.size() - i).setX(positions.get(positions.size() - i - 1).getX());
				positions.get(positions.size() - i).setY(positions.get(positions.size() - i - 1).getY());
	
			}
		}	
		
		
		// Move head
		switch (action) {
		case MOVE_UP:
			int y = positions.get(0).getY();
			
			if(y > 0) {
				head.setY(positions.get(0).getY()-1);
			} else {
				head.setY(game.getSizeY()-1);
			}
			break;
		case MOVE_DOWN:
			head.setY((positions.get(0).getY()+1)%game.getSizeY());
			break;
		case MOVE_RIGHT:
			head.setX((positions.get(0).getX()+1)%game.getSizeX());
			break;		
		case MOVE_LEFT:
			int x = positions.get(0).getX();
	
			if(x > 0) {
				head.setX(positions.get(0).getX()-1);
			} else {
				head.setX(game.getSizeX()-1);
			}

			break;

		default:
			break;
		}

		this.setLastMove(action);
		
	}

	
	
	public void update(SnakeGame state, AgentAction action, SnakeGame nextState, int reward ) {
		
		if(this.strategy.isModeTrain()) {
			this.strategy.update(this.id, state, action, nextState, reward, isDead );
		}
		
	}
	
	

	public void sizeIncrease() {

		this.positions.add(new Position(this.oldTailX, this.oldTailY));

	}

	public int getSize() {
		
		return this.positions.size();
	}

	public ArrayList<Position> getPositions() {
		return positions;
	}




	public void setPositions(ArrayList<Position> positions) {
		this.positions = positions;
	}








	public Strategy getStrategy() {
		return strategy;
	}





	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}


	public int getX() {

		return this.positions.get(0).getX();

	}

	public int getY() {

		return this.positions.get(0).getY();

	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	


	public boolean isDead() {
		return isDead;
	}


	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}




	public ColorSnake getColorSnake() {
		return colorSnake;
	}


	public void setColorSnake(ColorSnake colorSnake) {
		this.colorSnake = colorSnake;
	}


	public int getInvincibleTimer() {
		return invincibleTimer;
	}


	public void setInvincibleTimer(int invincibleTimer) {
		this.invincibleTimer = invincibleTimer;
	}


	public int getSickTimer() {
		return sickTimer;
	}


	public void setSickTimer(int sickTimer) {
		this.sickTimer = sickTimer;
	}


	public AgentAction getLastMove() {
		return lastMove;
	}


	public void setLastMove(AgentAction lastMove) {
		this.lastMove = lastMove;
	}


	
}
