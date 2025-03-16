package strategy;

import model.SnakeGame;
import utils.AgentAction;
import utils.Position;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TabularQLearning_duel extends Strategy {

	private HashMap<String, double[]> QTable;

	private static final Random rand = new Random();

	public TabularQLearning_duel(int nbAction, double espilon, double gamma, double alpha) {
		super(nbAction, espilon, gamma, alpha);
		this.QTable = new HashMap<String, double[]>();

	}

	@Override
	public synchronized AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {

		String currentEncodedState = encodeState(idxSnake, snakeGame);
		putIfAbsent(currentEncodedState);

		double p = rand.nextDouble();
		if ( p < this.epsilon ) {
			return AgentAction.values()[rand.nextInt(4)];
		}else {
			double[] qValues = QTable.get(currentEncodedState);
			int maxQValueIdx = 0;
			for (int i = 1; i < qValues.length; i++) {
				if (qValues[i] > qValues[maxQValueIdx]) {
					maxQValueIdx = i;
				}
			}
			return AgentAction.values()[maxQValueIdx];
		}
	}

	@Override
	public synchronized void update(int idx, SnakeGame state, AgentAction action, SnakeGame nextState, int reward,
									boolean isFinalState) {

		String currentEncodedState = encodeState(idx, state);
		putIfAbsent(currentEncodedState);

		String nextEncodedState = encodeState(idx, nextState);
		putIfAbsent(nextEncodedState);

		double[] qValues = QTable.get(nextEncodedState);
		int maxQValueIdx = 0;
		for (int i = 1; i < qValues.length; i++) {
			if (qValues[i] > qValues[maxQValueIdx]) {
				maxQValueIdx = i;
			}
		}
		AgentAction nextAction = AgentAction.values()[maxQValueIdx];

		if (!isFinalState) {
			QTable.get(currentEncodedState)[action.ordinal()] = ((1 - alpha) * QTable.get(currentEncodedState)[action.ordinal()]) + (alpha * (reward + gamma * QTable.get(nextEncodedState)[nextAction.ordinal()]));
		} else {
			QTable.get(currentEncodedState)[action.ordinal()] = ((1 - alpha) * QTable.get(currentEncodedState)[action.ordinal()]) + (alpha * reward);
		}
	}

	public String encodeState(int idxSnake, SnakeGame snakeGame) {
		StringBuilder strBuilder = new StringBuilder();

		boolean[][] walls = snakeGame.getWalls();
		AtomicReference<Position> mainSnakeHead = new AtomicReference<>();
		Set<Position> mainSnakePos = new HashSet<>();
		Set<Position> snakeHeadPos = new HashSet<>();
		Set<Position> snakesBodyPos = new HashSet<>();
		snakeGame.getSnakes().forEach((snake) -> {
			ArrayList<Position> snakePos = snake.getPositions();
			if (snake.getId() == idxSnake) {
				mainSnakeHead.set(snakePos.getFirst());
				mainSnakePos.addAll(snakePos.subList(1, snakePos.size()));
			}
			else {
				snakeHeadPos.add(snakePos.getFirst());
				snakesBodyPos.addAll(snakePos.subList(1, snakePos.size()));
			}
		});
		Set<Position> applesPos = new HashSet<>();
		Set<Position> boxsPos = new HashSet<>();
		Set<Position> sickBallsPos = new HashSet<>();
		Set<Position> invincibilityBallsPos = new HashSet<>();
		snakeGame.getItems().forEach((item) -> {
			switch (item.getItemType()) {
				case APPLE:
					applesPos.add(new Position(item.getX(), item.getY()));
					break;
				case BOX:
					boxsPos.add(new Position(item.getX(), item.getY()));
					break;
				case SICK_BALL:
					sickBallsPos.add(new Position(item.getX(), item.getY()));
					break;
				case INVINCIBILITY_BALL:
					invincibilityBallsPos.add(new Position(item.getX(), item.getY()));
					break;
			}
		});

		for (int x = 0; x < snakeGame.getSizeX(); x++) {
			for (int y = 0; y < snakeGame.getSizeY(); y++) {
				Position p = new Position(x, y);
				if (walls[x][y]) {
					continue;
				} else if (mainSnakeHead.get().equals(p)) {
					strBuilder.append('H');
				} else if (mainSnakePos.contains(p)) {
					strBuilder.append('S');
				} else if (snakeHeadPos.contains(p)) {
					strBuilder.append('h');
				} else if (snakesBodyPos.contains(p)) {
					strBuilder.append('s');
				} else if (applesPos.contains(p)) {
					strBuilder.append('A');
				} else if (boxsPos.contains(p)) {
					strBuilder.append('B');
				} else if (sickBallsPos.contains(p)) {
					strBuilder.append('W');
				} else if (invincibilityBallsPos.contains(p)) {
					strBuilder.append('I');
				} else {
					strBuilder.append(' ');
				}
			}
		}

		return strBuilder.toString();
	}

	void putIfAbsent(String state) {
		if (!QTable.containsKey(state)) {
			QTable.put(state, new double[4]);
		}
	}


}
