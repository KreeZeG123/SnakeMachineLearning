package model;
import java.io.Serializable;
import java.util.Observable;


public abstract class Game extends Observable implements Runnable, Serializable{

	
	int turn;
	int maxTurn;
	boolean isRunning;
	
	transient Thread thread;
	
	long time = 500;

	public Game(int maxTurn) {
		
		this.maxTurn = maxTurn;
		
	}
	
	public void init() {
		this.turn = 0;
		isRunning = true;
		
		initializeGame();
		
		setChanged();
		notifyObservers();
		
	}
	




	public void step() {
		

		if(this.gameContinue() & turn < maxTurn) {
			turn ++;
			takeTurn();
			
		} else {
			isRunning = false;
			
		
			gameOver();
		}

		
		setChanged();
		notifyObservers();
	}
	
	
	public void run() {
		
		while(isRunning == true) {
			
			step();
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		
		
	}
	
	public void pause() {
		
		isRunning = false;
	}
	
	public void join() throws InterruptedException {
		this.thread.join();
	}
	
	
	public void launch() {
		

		isRunning = true;
		this.thread = new Thread(this);
		this.thread.start();
		
	}	
		
		
	public abstract void initializeGame();
	
	public abstract void takeTurn();
	
	public abstract boolean gameContinue();
	
	public abstract void gameOver();
	
	
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getTurn() {
		return turn;
	}
	
}
