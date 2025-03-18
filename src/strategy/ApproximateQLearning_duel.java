package strategy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import strategy.algorithms.AlgoAEtoile;
import strategy.algorithms.DistManhattan;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;

public class ApproximateQLearning_duel extends Strategy {

	private static final int d = 6; // Nombre de caractéristiques

	private final Random random = new Random();

	private double[] w;

	double[] current_f;

	public double[] getW() {
		return w;
	}

	public double[] getCurrent_f() {
		return current_f;
	}

	public ApproximateQLearning_duel(int nbAction, double espilon, double gamma, double alpha) {
		super(nbAction, espilon, gamma, alpha);

		this.w = new double[d + 1];

		// Initialisation aléatoire des poids
		for (int i = 1; i <= d; i++) {
			w[i] = random.nextGaussian();
		}
	}

	private double[] extractFeatures(SnakeGame state, AgentAction action, int idxSnake) {
		double[] features = new double[d + 1];

		Snake snake = state.getSnakes().get(idxSnake);

		// Feature 0 : Biais
		features[0] = 1.0;

		// Feature 1 : Présence d'une pomme dans la prochaine position (Boosté)
		features[1] = isAppleInNextMove(state, action, snake) ? 10.0 : 0.0;

		// Feature 2 : Distance à la pomme (Inversée pour favoriser la proximité)
		features[2] = 5 * (1 - normalizedAppleDistance(state, snake));

		// Feature 3 : Risque de mort (Pénalité renforcée)
		features[3] = willDieNextMove(state, action, snake) ? -15 : 0.0;

		// Feature 4 : Distance au mur (Inversée pour favoriser les zones sûres)
		features[4] = -2.5 * (1 - normalizedWallDistance(state, snake));

		// Features 5 : Distance avec un snake plus grand (à eviter)
		features[5] = -2.5 * (1 - normalizedNeareastBiggerSnakeDistance(state, snake));

		// Features 6 : Distances avec un snakes plus petits (à focus)
		features[6] = 2.5 * (1 - normalizedNeareastSmallerSnakeDistance(state, snake));

		return features;
	}

	public double scalarProduct(double[] w, double[] f) {
		double q = 0;

		for(int i = 0; i < w.length; i++) {
			q += w[i]*f[i];
		}

		return q;
	}

	@Override
	public AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {

		int actionNumbers = AgentAction.values().length;

		double[][] fs = new double[actionNumbers][d + 1];
		double[] qs = new double[actionNumbers];

		for (int i = 0; i < actionNumbers; i++) {
			AgentAction action = AgentAction.values()[i];
			fs[i] = extractFeatures(snakeGame, action, idxSnake);
			qs[i] = scalarProduct(w, fs[i]);
		}

		if(random.nextDouble() < this.epsilon) {

			int rdmActionNumber = random.nextInt(actionNumbers);
			this.current_f = fs[rdmActionNumber];
			return AgentAction.values()[rdmActionNumber];

		} else {

			int idxMaxQ = random.nextInt(actionNumbers);
			for (int i = 0; i < actionNumbers; i++) {
				if ( qs[i] > qs[idxMaxQ]) {
					idxMaxQ = i;
				}
			}

			this.current_f = fs[idxMaxQ];
			return AgentAction.values()[idxMaxQ];

		}

	}



	@Override
	public void update(int idxSnake, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {

		epsilon = Double.max(0.2, epsilon * 0.95);

		int actionNumbers = AgentAction.values().length;

		// New State Features
		double[][] nfs = new double[actionNumbers][d + 1];
		// New Qs
		double[] nqs = new double[actionNumbers];

		// Computes Fs and Qs
		for (int i = 0; i < actionNumbers; i++) {
			AgentAction a = AgentAction.values()[i];
			nfs[i] = extractFeatures(nextState, a, idxSnake);
			nqs[i] = scalarProduct(w, nfs[i]);
		}

		// Max
		int idxMaxQ = random.nextInt(actionNumbers);
		for (int i = 0; i < actionNumbers; i++) {
			if ( nqs[i] > nqs[idxMaxQ]) {
				idxMaxQ = i;
			}
		}
		double maxQ = nqs[idxMaxQ];

		// Target
		double target = reward + this.gamma * maxQ;

		// Qstate
		double Qstate = scalarProduct(this.w, this.current_f);

		// Update Weights
		for (int i = 0; i < d + 1; i++) {
			this.w[i] = this.w[i] - 2 * this.alpha * this.current_f[i] * (Qstate - target);
		}

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

		return (double) minDistance / (gridSizeX * gridSizeY);

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
		return (double) minDistance / (gridSizeX * gridSizeY);
	}

	private double normalizedNeareastBiggerSnakeDistance(SnakeGame state, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		int minDistance = Integer.MAX_VALUE;
		int gridSizeX = state.getSizeX();
		int gridSizeY = state.getSizeY();

		for (Snake otherSnake : state.getSnakes()) {
			if (otherSnake.getId() != snake.getId() && otherSnake.getPositions().size() >= snake.getPositions().size()) {
				for (Position bodyPart : otherSnake.getPositions()) { // Vérifier toutes les parties du corps
					int distance = Math.abs(bodyPart.getX() - snakeHeadPos.getX()) + Math.abs(bodyPart.getY() - snakeHeadPos.getY());
					minDistance = Math.min(minDistance, distance);
				}
			}
		}

		return (minDistance == Integer.MAX_VALUE) ? 1.0 : (double) minDistance / (gridSizeX * gridSizeY);
	}

	private double normalizedNeareastSmallerSnakeDistance(SnakeGame state, Snake snake) {
		Position snakeHeadPos = snake.getPositions().getFirst();
		int minDistance = Integer.MAX_VALUE;
		int gridSizeX = state.getSizeX();
		int gridSizeY = state.getSizeY();

		for (Snake otherSnake : state.getSnakes()) {
			if (otherSnake.getId() != snake.getId() && otherSnake.getPositions().size() < snake.getPositions().size()) {
				for (Position bodyPart : otherSnake.getPositions()) {
					int distance = Math.abs(bodyPart.getX() - snakeHeadPos.getX()) + Math.abs(bodyPart.getY() - snakeHeadPos.getY());
					minDistance = Math.min(minDistance, distance);
				}
			}
		}

		return (minDistance == Integer.MAX_VALUE) ? 1.0 : (double) minDistance / (gridSizeX * gridSizeY);
	}



}
