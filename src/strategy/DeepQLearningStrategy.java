package strategy;



import model.SnakeGame;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public class DeepQLearningStrategy extends Strategy {

	NeuralNetWorkDL4J nn;

	double baseEpsilon;
	double epsilonMul;
	double epsilonMin;

	int d = 5;

	int nEpochs;
	int batchSize;

	Random random = new Random();
	int actionsNumbers = AgentAction.values().length;
	ArrayList<TrainExample> trainExamples;

	public DeepQLearningStrategy(int nbAction, double epsilon, double epsilonMul, double epsilonMin, double gamma, double alpha, int nEpochs, int batchSize, int encdodeStageSize) {
		super(nbAction, epsilon, gamma, alpha);

		this.nn = new NeuralNetWorkDL4J(alpha, 0,  encdodeStageSize, 4 );

		this.baseEpsilon = epsilon;
		this.epsilonMul = epsilonMul;
		this.epsilonMin = epsilonMin;

		this.nEpochs = nEpochs;
		this.batchSize = batchSize;

		this.trainExamples = new ArrayList<TrainExample>();
	}

	@Override
	public AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {

		if(random.nextDouble() < this.epsilon) {
			int rdmActionNumber = random.nextInt(actionsNumbers);
			return AgentAction.values()[rdmActionNumber];
		} else {
			double[] encodedState = encodeState(idxSnake, snakeGame);
			double[] outPut = this.nn.predict(encodedState);

			int maxQValueIdx = random.nextInt(actionsNumbers);
			for (int i = 0; i < actionsNumbers; i++) {
				if (outPut[i] > outPut[maxQValueIdx]) {
					maxQValueIdx = i;
				}
			}

			return AgentAction.values()[maxQValueIdx];
		}
	}

	@Override
	public void update(int idxSnake, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {

		if (isModeTrain()) {
			this.baseEpsilon = Math.max(this.epsilonMin, this.baseEpsilon * this.epsilonMul);
		}

		double[] nextEncodedState = encodeState(idxSnake, nextState);
		double[] qValues_nextState = this.nn.predict(nextEncodedState);

		int maxQValueIdx = random.nextInt(actionsNumbers);
		for (int i = 0; i < actionsNumbers; i++) {
			if (qValues_nextState[i] > qValues_nextState[maxQValueIdx]) {
				maxQValueIdx = i;
			}
		}
		double maxQvalue_nextState = qValues_nextState[maxQValueIdx];


		double[] encodedState = encodeState(idxSnake, state);
		double[] targetQ = this.nn.predict(encodedState);

		double targetValue = isFinalState ? reward : reward + gamma * maxQvalue_nextState;
		targetQ[action.ordinal()] = targetValue;

		TrainExample trainExample = new TrainExample(encodedState, targetQ);

		int maxMemory = 10000;
		if (trainExamples.size() > maxMemory) {
			trainExamples.removeFirst();
		}

		this.trainExamples.add(trainExample);

	}

	private double[] encodeState(int idxSnake, SnakeGame snakeGame) {

		// Champ de vision de 5 x 5 et 5 features
		int fieldSize = 5;
		double[] stateVector = new double[fieldSize * fieldSize * 5];

		boolean[][] walls = snakeGame.getWalls();
		AtomicReference<Position> mainSnakeHead = new AtomicReference<>();
		Set<Position> mainSnakePos = new HashSet<>();
		Set<Position> snakeHeadPos = new HashSet<>();
		Set<Position> snakesBodyPos = new HashSet<>();

		// Récupération des informations sur les serpents et objets
		snakeGame.getSnakes().forEach((snake) -> {
			if (!snake.isDead()) {
				ArrayList<Position> snakePos = snake.getPositions();
				if (snake.getId() == idxSnake) {
					Position hPos = snakePos.getFirst();
					mainSnakeHead.set(hPos);
					mainSnakePos.addAll(snakePos.subList(1, snakePos.size()));
				} else {
					snakeHeadPos.add(snakePos.getFirst());
					snakesBodyPos.addAll(snakePos.subList(1, snakePos.size()));
				}
			}
		});

		Set<Position> applesPos = new HashSet<>();
		snakeGame.getItems().forEach((item) -> {
			if (item.getItemType() == ItemType.APPLE) {
				applesPos.add(new Position(item.getX(), item.getY()));
			}
		});

		// Indice du centre du champ d'observation (autour de la tête du serpent)
		Position tempHPos = snakeGame.getSnakes().get(idxSnake).getPositions().getFirst();
		int headX = tempHPos.getX();
		int headY = tempHPos.getY();

		// Remplir le vecteur d'état pour chaque case du champ 5x5 autour de la tête
		int featureIndex = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				int x = headX + dx;
				int y = headY + dy;

				// Vérifier si la position est dans les limites du jeu
				if (x >= 0 && x < snakeGame.getSizeX() && y >= 0 && y < snakeGame.getSizeY()) {

					// Définir un index unique pour chaque case (5 caractéristiques par case)
					int index = (dx + 2) * fieldSize + (dy + 2);

					// Valeurs booléennes pour les caractéristiques : [mur, tête du main serpent, corps du main serpent, corps ou tete des autre serpent, pomme]
					boolean isWall = walls[x][y];
					boolean isSnakeHead = mainSnakeHead.get() != null && mainSnakeHead.get().equals(new Position(x, y));  // Tête du serpent
					boolean isSnakeBody = mainSnakePos.contains(new Position(x, y)); // Corps de notre serpent
					boolean isOtherSnakeBody = snakesBodyPos.contains(new Position(x, y)) || snakeHeadPos.contains(new Position(x, y)); // Corps des autre serpent
					boolean isApple = applesPos.contains(new Position(x, y)); // Pomme

					// Remplir les 6 valeurs par case (booléen 0 ou 1 pour chaque caractéristique)
					stateVector[featureIndex++] = isWall ? 1 : 0; // Mur
					stateVector[featureIndex++] = isSnakeHead ? 1 : 0; // Tête du main serpent
					stateVector[featureIndex++] = isSnakeBody ? 1 : 0; // Corps du main serpent
					stateVector[featureIndex++] = isOtherSnakeBody ? 1 : 0; // Corps ou tete des autre serpent
					stateVector[featureIndex++] = isApple ? 1 : 0; // Pomme
				} else {
					// Si la position est en dehors des limites, on affecte 0 à toutes les caractéristiques
					stateVector[featureIndex++] = 0;
					stateVector[featureIndex++] = 0;
					stateVector[featureIndex++] = 0;
					stateVector[featureIndex++] = 0;
					stateVector[featureIndex++] = 0;
				}
			}
		}

		return stateVector;
	}


	@Override
	public void learn() {
		this.nn.fit(trainExamples, this.nEpochs, this.batchSize);
	}

	
}