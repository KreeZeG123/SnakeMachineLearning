package model;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.SerializationUtils;

import agent.Snake;
import factory.SnakeFactory;

import item.Item;
import strategy.Strategy;
import utils.AgentAction;
import utils.FeaturesItem;
import utils.FeaturesSnake;
import utils.ItemType;
import utils.Position;



public class SnakeGame extends Game implements Serializable{



	public static final int REWARD_APPLE = 1;
	public static final int REWARD_ITEM = 1;
	public static final int REWARD_DEAD = -10;
	public static final int REWARD_KILL = 10;
	
	
	
	public static int timeInvincible = 20;
	public static int timeSick = 20;

	double probSpecialItem = 0;


	private transient ArrayList<FeaturesSnake> start_snakes ;
	private transient ArrayList<FeaturesItem> start_items ;


	private ArrayList<Snake> snakes;
	private ArrayList<Item> items;


	transient InputMap inputMap;


	private AgentAction inputMoveHuman1;


	private int sizeX;
	private int sizeY;

	
	private int[] tabCurrentRewardSnakes;
	
	private int[] tabTotalScoreSnakes;
	
	private transient Strategy[] strats;
	
	boolean randomFirstApple;

	private Boolean aucunMur = null;

	public SnakeGame(int maxTurn, InputMap inputMap, boolean randomFirstApple) {

		super(maxTurn);

		this.inputMap = inputMap;
		this.inputMoveHuman1 = AgentAction.MOVE_DOWN;

		this.randomFirstApple = randomFirstApple;

	}

	@Override
	public void initializeGame() {

		
		this.walls = inputMap.get_walls().clone();

		String levelAISnake = "Advanced";
		SnakeFactory snakeFactory = new SnakeFactory();

		start_snakes = inputMap.getStart_snakes();
		start_items = inputMap.getStart_items();

		this.sizeX = inputMap.getSizeX();
		this.sizeY = inputMap.getSizeY();
		
		
		snakes = new ArrayList<Snake>();
		items = new ArrayList<Item>();

		int id = 0;
		
		
		for(FeaturesSnake featuresSnake : start_snakes) {
			snakes.add(snakeFactory.createSnake(featuresSnake, levelAISnake, id));
			snakes.get(id).setStrategy(strats[id]);	
			id++;
		}
		

		if(this.randomFirstApple || start_items.size() == 0 ) {
			
			addRandomApple();
		} else {
			for(FeaturesItem featuresItem : start_items) {	
				items.add(new Item(featuresItem.getX(),featuresItem.getY(), featuresItem.getItemType()));
			}
		}
		
		tabTotalScoreSnakes = new int[snakes.size()];
		
		tabCurrentRewardSnakes = new int[snakes.size()];
		
	}
	
	

	public void setStrategies(Strategy[] strats) {
		
		this.strats = strats;
		
	
	}

	@Override
	public void takeTurn() {


		SnakeGame state = SerializationUtils.clone( this);
		
		for(int i =0; i < tabCurrentRewardSnakes.length; i++) {	
			tabCurrentRewardSnakes[i] = 0;
		}
		
		
		ArrayList<AgentAction> actions = new ArrayList<AgentAction>(); 
		
		for(int i = 0; i < snakes.size(); i++) {
			
			if(snakes.get(i).isDead()) {
				actions.add(null);
			} else {
				actions.add(snakes.get(i).play(this));
			}
			
			
		}

		
		for(int i = 0; i < actions.size(); i++) {

			if(actions.get(i) != null) {
				if(isLegalMove(snakes.get(i), actions.get(i))) {
					snakes.get(i).move(actions.get(i), this);
				} else {
					snakes.get(i).move(snakes.get(i).getLastMove(), this);
				}
			}
			
		}
		
		checkSnakeEaten();

		checkWalls();


		boolean isAppleEaten = checkItemFound();


		if(isAppleEaten) {

			addRandomApple();

			Random rand = new Random();

			double r = rand.nextDouble();

			if(r < probSpecialItem) {
				
				addRandomItem();
			}

		}


		for(int i = 0; i < actions.size(); i++) {
			
			if(actions.get(i) != null) {
				
				snakes.get(i).update(state, actions.get(i), this, tabCurrentRewardSnakes[i]);
				
			}
			
		}


		updateSnakeTimers();
		
		//removeSnake();

			
	}

	public boolean isLegalMove(Snake snake, AgentAction action) {
		
		if(snake.getSize() > 1) {
			if(snake.getLastMove() == AgentAction.MOVE_DOWN && action == AgentAction.MOVE_UP) {
				
				return false;
				
			} else if(snake.getLastMove() == AgentAction.MOVE_UP && action == AgentAction.MOVE_DOWN) {
				
				return false;
				
			} else if(snake.getLastMove() == AgentAction.MOVE_LEFT && action == AgentAction.MOVE_RIGHT) {
				
				return false;
				
			} else if(snake.getLastMove() == AgentAction.MOVE_RIGHT && action == AgentAction.MOVE_LEFT) {
				
				return false;
				
			}
		}
		return true;
		
	}

	@Override
	public boolean gameContinue() {

		boolean gameContinue = false;
				
		for(Snake snake : snakes) {
			
			if(!snake.isDead()) {
				
				gameContinue = true;
			}
		}
		
		return gameContinue;
	}

	@Override
	public void gameOver() {
		//System.out.println("Game over");
	}




	public void addRandomApple() {

		Random rand = new Random();

		ArrayList<Position> listFreePositions = new ArrayList<Position>();

		for(int x = 0; x < this.inputMap.getSizeX(); x++) {
			for(int y = 0; y < this.inputMap.getSizeY(); y++) {

				if(!this.walls[x][y] & !isSnake(x, y)) {
					listFreePositions.add(new Position(x, y));
				}
			}

		}

		if(listFreePositions.size() > 0){

			Position pos = listFreePositions.get(rand.nextInt(listFreePositions.size()));
			
			this.items.add(new Item(pos.getX(),pos.getY(),ItemType.APPLE));
		}




	}



	public void addRandomItem() {

		Random rand = new Random();

		int r = rand.nextInt(3);

		ItemType itemType = null;

		if(r == 0) {		
			itemType = ItemType.BOX;
		} else if(r == 1) {
			itemType = ItemType.SICK_BALL;
		} else if(r == 2) {
			itemType = ItemType.INVINCIBILITY_BALL;
		}


		boolean notPlaced = true;

		while(notPlaced) {

			int x = rand.nextInt(this.inputMap.getSizeX());
			int y = rand.nextInt(this.inputMap.getSizeY());

			if(!this.walls[x][y] & !isSnake(x,y) & !isItem(x,y)) {

				this.items.add(new Item(x,y,itemType));
				notPlaced = false;  	
			}
		}


	}


	public boolean isSnake(int x, int y) {

		for(Snake snake : snakes) {

			for(Position pos : snake.getPositions()) {

				if(pos.getX() == x & pos.getY() == y) {
					return true;
				}
			}
		}

		return false;
	}


	
	
	public boolean isItem(int x, int y) {

		for(Item item : items) {


			if(item.getX() == x & item.getY() == y) {
				return true;
			}

		}

		return false;
	}







	public boolean checkItemFound() {

		ListIterator<Item> iterItem = items.listIterator();

		boolean isAppleEaten = false;

		while(iterItem.hasNext()){

			Item item = iterItem.next();

			for(Snake snake : snakes) {


				if(snake.getSickTimer() < 1 && snake.isDead() == false) {

					int x = snake.getPositions().get(0).getX();
					int y = snake.getPositions().get(0).getY();	

					if(item.getX() == x &  item.getY() == y ) {

						iterItem.remove();

						if(item.getItemType() == ItemType.APPLE) {
							snake.sizeIncrease();
							isAppleEaten = true;
							
							tabCurrentRewardSnakes[snake.getId()] += this.REWARD_APPLE;
							tabTotalScoreSnakes[snake.getId()] += this.REWARD_APPLE;
							
							
						}

						if(item.getItemType() == ItemType.BOX) {
							Random rand = new Random();
							double r = rand.nextDouble();
							if(r < 0.5) {
								snake.setInvincibleTimer(this.timeInvincible);

							} else {
								snake.setSickTimer(this.timeSick);
							}
							
							tabCurrentRewardSnakes[snake.getId()] += this.REWARD_ITEM;
							tabTotalScoreSnakes[snake.getId()] += this.REWARD_ITEM;
							
							
						}

						if(item.getItemType() == ItemType.SICK_BALL) {

							snake.setSickTimer(this.timeSick);
							
							tabCurrentRewardSnakes[snake.getId()] += this.REWARD_ITEM;
							tabTotalScoreSnakes[snake.getId()] += this.REWARD_ITEM;
							
							
						}

						if(item.getItemType() == ItemType.INVINCIBILITY_BALL) {

							snake.setInvincibleTimer(this.timeInvincible);
							
							tabCurrentRewardSnakes[snake.getId()] += this.REWARD_ITEM;
							tabTotalScoreSnakes[snake.getId()] += this.REWARD_ITEM;

						}

					}

				}

			}


		}


		return isAppleEaten;
	}



	public void checkSnakeEaten() {


		boolean[] tabEaten = new boolean[snakes.size()];
		
				
		for(int s = 0; s < snakes.size(); s++) {

			Snake snake1 = snakes.get(s);
			
			tabEaten[s] = false;
			
			if(snake1.getInvincibleTimer() < 1 && snake1.isDead() == false) {


				for(Snake snake2 : snakes) {

					int x2 = snake2.getPositions().get(0).getX();
					int y2 = snake2.getPositions().get(0).getY();	
					
					// Kill by an other snake
					if(snake1.getId() != snake2.getId() && snake2.isDead() == false && snake1.getSize() <= snake2.getSize()) {


						for(int i = 0; i < snake1.getPositions().size(); i++) {

							if( x2 == snake1.getPositions().get(i).getX() &&  y2 == snake1.getPositions().get(i).getY() ) {
								
								tabEaten[s] = true;
								
								tabCurrentRewardSnakes[snake1.getId()] += this.REWARD_DEAD;
								//tabTotalScoreSnakes[snake1.getId()] += this.REWARD_DEAD;
								
								tabCurrentRewardSnakes[snake2.getId()] += this.REWARD_KILL;
								tabTotalScoreSnakes[snake2.getId()] += this.REWARD_KILL;
								
							}
							
						}
					}
					
					
					// Kill by himself
					
					if(snake2.isDead() == false && snake1.getId() == snake2.getId()) {
							
							
						for(int i = 1; i < snake1.getPositions().size(); i++) {

							if( x2 == snake1.getPositions().get(i).getX() &&  y2 == snake1.getPositions().get(i).getY() ) {

								tabEaten[s] = true;
								tabCurrentRewardSnakes[snake1.getId()] += this.REWARD_DEAD;	
								//tabTotalScoreSnakes[snake1.getId()] += this.REWARD_DEAD;

								
							}
						}
						

					} 



				}

			}

		}
		
		
		for(int s = 0; s < snakes.size(); s++) {
			
			if(tabEaten[s]) {
				
				snakes.get(s).setDead(true);
			}
			
		}

	}

	public void checkWalls() {

		for(Snake snake1 : snakes) {

			if(snake1.getInvincibleTimer() < 1) {
				
				int x = snake1.getPositions().get(0).getX()%this.sizeX;
				int y = snake1.getPositions().get(0).getY()%this.sizeY;
	
				if(walls[x][y]) {
	
					tabCurrentRewardSnakes[snake1.getId()] += this.REWARD_DEAD;
					
					
					snake1.setDead(true);
	
				}
			}
		}


	}

	public boolean isWall(int x, int y) {
		return walls[x][y];
	}

	public boolean isOccupiedBySnake(int x, int y, int snakeID) {
		Position pos = new Position(x, y);
		for(Snake snake : snakes) {
			if ( snake.getId() == snakeID ) {
				if ( snake.getPositions().subList(1, snake.getPositions().size()).contains( pos ) ) return true;
			}
			else {
				if ( snake.getPositions().contains(pos) ) return true;

			}
		}
		return false;
	}


	public void removeSnake() {

		ListIterator<Snake> iterSnake = snakes.listIterator();

		while(iterSnake.hasNext()){

			Snake snake = iterSnake.next();

			if(snake.isDead()) {
				
				
				iterSnake.remove();

			}

		}	

	}




	public void updateSnakeTimers() {

		ListIterator<Snake> iter = snakes.listIterator();

		while(iter.hasNext()){
			
			Snake snake = iter.next();

			if(snake.getInvincibleTimer() > 0) {

				snake.setInvincibleTimer(snake.getInvincibleTimer() - 1);
			}

			if(snake.getSickTimer() > 0) {

				snake.setSickTimer(snake.getSickTimer() - 1);
			}

		}

	}


	public ArrayList<Item> getItems() {
		return items;
	}

	private boolean walls[][];



	public boolean[][] getWalls() {
		return walls;

	}


	public AgentAction getInputMoveHuman1() {
		return inputMoveHuman1;
	}

	public void setInputMoveHuman1(AgentAction inputMoveHuman1) {
		this.inputMoveHuman1 = inputMoveHuman1;
	}

	public ArrayList<Snake> getSnakes() {
		return snakes;
	}

	public void setSnakes(ArrayList<Snake> snakes) {
		this.snakes = snakes;
	}

	public int getSizeX() {
		return sizeX;
	}

	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	
	public int[] getTabTotalScoreSnakes() {
		return tabTotalScoreSnakes;
	}

	public boolean shouldUseManhattanWrap(Snake snake) {
		// Renvoie vrai si le snake est invincible car il peut passer au travers des murs
		if (snake.getInvincibleTimer() > 0) {
			return true;
		}

		// Sinon on regarde si c'est une map sans aucun murs pour globaliser
		if ( this.aucunMur == null ) {
			boolean sansWall = true;
			for (boolean[] row : this.getWalls()) {
				for (boolean wall : row) {
					if (wall) {
						sansWall = false;
						break;
					}
				}
				if (!sansWall) break;
			}
			this.aucunMur = sansWall;
		}
		return this.aucunMur;
	}


	public Set<Position> getWallsPositions() {
		Set<Position> wallsPositions = new HashSet<>();
		for (int x = 0; x < this.getSizeX(); x++) {
			for (int y = 0; y < this.getSizeY(); y++) {
				if (this.getWalls()[x][y]) {
					wallsPositions.add(new Position(x, y));
				}
			}
		}
		return wallsPositions;
	}

	public Set<Position> getItemsPositions(ItemType itemType) {
		return getItemsPositions(Collections.singletonList(itemType));
	}

	public Set<Position> getItemsPositions(List<ItemType> itemTypes) {
		Set<Position> itemsPositions = new HashSet<>();
		for (Item item : items) {
			if (itemTypes.contains(item.getItemType())) {
				itemsPositions.add(item.getPosition());
			}
		}
		return itemsPositions;
	}

	public Set<Position> getSnakesPositions() {
		Set<Position> snakesPositions = new HashSet<>();
		for ( Snake snake : snakes ) {
			snakesPositions.addAll(snake.getPositions());
		}
		return snakesPositions;
	}
	
}
