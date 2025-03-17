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
	private static final int d = 6; // Nombre de caractéristiques
	private final Random random;

	public ApproximateQLearning_solo(int nbAction, double espilon, double gamma, double alpha) {
		super(nbAction, alpha, espilon, gamma);
		this.w = new double[ApproximateQLearning_solo.d];
		this.random = new Random();

		// Initialisation aléatoire des poids
		for (int i = 0; i < d; i++) {
			w[i] = (random.nextDouble() - 0.5) * 0.1; // Petites valeurs entre -0.05 et 0.05
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
				for (int i = 0; i < d; i++) {
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
		for (AgentAction a : AgentAction.values()) {
			double[] f = extractFeatures(nextState, a , idxSnake);
			double QValue = 0;
			for (int i = 0; i < d; i++) {
				QValue += w[i] * f[i];
			}
			if ( QValue > maxQValue ) {
				maxQValue = QValue;
			}
		}

		double targetQ = reward + this.gamma * maxQValue;

		double[] f = extractFeatures(state, action , idxSnake);
		double QValue = 0;
		for (int i = 0; i < d; i++) {
			QValue += w[i] * f[i];
		}

		for (int i = 0; i < d; i++) {
			w[i] = w[i] - 2 * alpha * f[i] * (QValue - targetQ);
		}
	}

	private double[] extractFeatures(SnakeGame state, AgentAction action, int idxSnake) {
		double[] features = new double[ApproximateQLearning_solo.d];

		Snake snake = state.getSnakes().get(idxSnake);

		// Feature 0 : Biais
		features[0] = 1.0;

		// Feature 1 : présence d'une pomme dans la prochaine position
		features[1] = isAppleInNextMove(state, action, snake) ? 1.0 : 0.0;

		// Feature 2 : distance de la pomme (normalisée)
		features[2] = normalizedAppleDistance(state, snake);

		// Feature 3 : risque de mort (collision avec mur ou corps)
		features[3] = willDieNextMove(state, action, snake) ? 1.0 : 0.0;

		// Feature 4 : distance normalisée au mur
		features[4] = normalizedWallDistance(state, snake);

		// Feature 5 : alignement avec la pomme (1 si aligné sur X ou Y, sinon 0)
		features[5] = isAlignedWithApple(state, snake) ? 1.0 : 0.0;

		return features;
	}

	private boolean isAppleInNextMove(SnakeGame state, AgentAction action, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		Position nextPos = Position.getNewPosition(snakeHeadPos, action, snake.getX(), state.getSizeY());
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
		int gridSizeX = state.getSizeX();
		int gridSizeY = state.getSizeY();
		int minDistance = Integer.MAX_VALUE;

		for (Item item : state.getItems()) {
			if (item.getItemType() == ItemType.APPLE) {
				int distance = Math.abs(item.getX() - headX) + Math.abs(item.getY() - headY);
				minDistance = Math.min(minDistance, distance);
			}
		}

		return (double) minDistance / (gridSizeX + gridSizeY);

	}

	private boolean willDieNextMove(SnakeGame state, AgentAction action, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		Position nextPos = Position.getNewPosition(snakeHeadPos, action, state.getSizeX(), state.getSizeY());
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
		return (double) minDistance / (gridSizeX + gridSizeY);
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
