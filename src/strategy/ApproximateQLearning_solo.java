package strategy;

import java.awt.*;
import java.util.Random;
import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;

public class ApproximateQLearning_solo extends Strategy {
	private double[] w; // Poids du modèle
	private static final int numFeatures = 5; // Nombre de caractéristiques
	private Random random;

	public ApproximateQLearning_solo(int nbAction, double espilon, double gamma, double alpha) {
		super(nbAction, alpha, espilon, gamma);
		this.w = new double[ApproximateQLearning_solo.numFeatures];
		this.random = new Random();

		// Initialisation aléatoire des poids
		for (int i = 0; i < numFeatures; i++) {
			w[i] = random.nextDouble();
		}
	}

	@Override
	public AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {
		if (random.nextDouble() < epsilon) {
			// Exploration : choix aléatoire
			return AgentAction.values()[random.nextInt(AgentAction.values().length)];
		} else {
			// Exploitation : choisir la meilleure action estiméé
			double maxQValue = Double.NEGATIVE_INFINITY;
			AgentAction bestAction = null;
			for (AgentAction action : AgentAction.values()) {
				double[] f = extractFeatures(snakeGame, action , idxSnake);
				double QValue = 0;
				for (int i = 0; i < numFeatures; i++) {
					QValue += w[i] * f[i];
				}
				if ( QValue > maxQValue ) {
					maxQValue = QValue;
					bestAction = action;
				}
			}
			return bestAction;
		}
	}

	@Override
	public void update(int idxSnake, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {

		double maxQValue = Double.NEGATIVE_INFINITY;
		AgentAction bestAction = null;
		double[] bestActionFeatures = null;
		for (AgentAction a : AgentAction.values()) {
			double[] f = extractFeatures(state, a , idxSnake);
			double QValue = 0;
			for (int i = 0; i < numFeatures; i++) {
				QValue += w[i] * f[i];
			}
			if ( QValue > maxQValue ) {
				maxQValue = QValue;
				bestAction = a;
				bestActionFeatures = f;
			}
		}

		double targetQ = reward + this.gamma * maxQValue;

		double[] f = extractFeatures(state, action , idxSnake);
		double QValue =

		for (int i = 0; i < numFeatures; i++) {
			w[i] = w[1] - 2 * alpha * f[i] * (maxQValue - targetQ);
		}
	}

	private double[] extractFeatures(SnakeGame state, AgentAction action, int idxSnake) {
		double[] features = new double[ApproximateQLearning_solo.numFeatures];

		Snake snake = state.getSnakes().get(idxSnake);

		// Feature 1 : présence d'une pomme dans la prochaine position
		features[0] = isAppleInNextMove(state, action, snake) ? 1.0 : 0.0;

		// Feature 2 : distance de la pomme (normalisée)
		features[1] = normalizedAppleDistance(state, snake);

		// Feature 3 : risque de mort (collision avec mur ou corps)
		features[2] = willDieNextMove(state, action, snake) ? 1.0 : 0.0;

		// Feature 4 : distance normalisée au mur
		features[3] = normalizedWallDistance(state, snake);

		// Feature 5 : alignement avec la pomme (1 si aligné sur X ou Y, sinon 0)
		features[4] = isAlignedWithApple(state, snake) ? 1.0 : 0.0;

		return features;
	}

	private boolean isAppleInNextMove(SnakeGame state, AgentAction action, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		Position nextPos = Position.getNewPosition(snakeHeadPos, action);
		int nextX = nextPos.getX();
		int nextY = nextPos.getY();

		for (Item item : state.getItems()) {
			if (item.getItemType() == ItemType.APPLE && item.getX() == nextX && item.getY() == nextY) {
				return true;
			}
		}
		return false;
	}

	private double normalizedAppleDistance(SnakeGame state, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		int headX = snakeHeadPos.getX();
		int headY = snakeHeadPos.getY();
		int minDistance = Integer.MAX_VALUE;

		for (Item item : state.getItems()) {
			if (item.getItemType() == ItemType.APPLE) {
				int distance = Math.abs(item.getX() - headX) + Math.abs(item.getY() - headY);
				minDistance = Math.min(minDistance, distance);
			}
		}
		if ( minDistance == Integer.MAX_VALUE) return 0;
		return 1.0 / (1.0 + minDistance);
	}

	private boolean willDieNextMove(SnakeGame state, AgentAction action, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		Position nextPos = Position.getNewPosition(snakeHeadPos, action);
		int nextX = nextPos.getX();
		int nextY = nextPos.getY();

        return state.isWall(nextX, nextY) || state.isOccupiedBySnake(nextX, nextY);
    }

	private double normalizedWallDistance(SnakeGame state, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		int headX = snakeHeadPos.getX();
		int headY = snakeHeadPos.getY();
		int gridSizeX = state.getSizeX();
		int gridSizeY = state.getSizeY();

		int distLeft = headX;
		int distRight = gridSizeX - headX - 1;
		int distUp = headY;
		int distDown = gridSizeY - headY - 1;

		int minDistance = Math.min(Math.min(distLeft, distRight), Math.min(distUp, distDown));
		return 1.0 / (1.0 + minDistance);
	}

	private boolean isAlignedWithApple(SnakeGame state, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		int headX = snakeHeadPos.getX();
		int headY = snakeHeadPos.getY();

		for (Item item : state.getItems()) {
			if (item.getItemType() == ItemType.APPLE) {
				if (item.getX() == headX || item.getY() == headY) {
					return true;
				}
			}
		}
		return false;
	}
}
