package strategy;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;

import java.util.List;
import java.util.Random;

public class ApproximateQLearning_duel_fixed_yamis extends Strategy {

    private static final int d = 5; // Nombre de caractéristiques

    private final Random random = new Random();

    private final double[] w;

    double[] current_f;

    public double[] getW() {
        return w;
    }

    public ApproximateQLearning_duel_fixed_yamis() {
        super(4, 0, 0.95, 0.1);

        this.w = new double[]{
                -1.110835399526325,
                0.07571323388807702,
                4.82513943092903,
                5.760921209750668,
                -0.27799864400873875,
                2.2218426602927863
        };

    }

    private double[] extractFeatures(SnakeGame state, AgentAction action, int idxSnake) {
        double[] features = new double[d + 1];

        Snake snake = state.getSnakes().get(idxSnake);

        // Feature 0 : Biais
        features[0] = 1.0;

        // Feature 1 : Présence d'une pomme dans la prochaine position (Boosté)
        features[1] = isAppleInNextMove(state, action, snake) ? 1 : 0;

        // Feature 2 : Distance à la pomme (Inversée pour favoriser la proximité)
        features[2] = (1 - normalizedAppleDistance(state, snake, action));

        // Feature 3 : Risque de mort (Pénalité renforcée)
        features[3] = willDieNextMove(state, action, snake) ? -1 : 0.0;

        // Features 4 : Distance avec un snake plus grand (à eviter)
        features[4] = normalizedNeareastBiggerSnakeDistance(state, snake);

        // Features 5 : Distances avec un snakes plus petit (à focus)
        features[5] = (1 - normalizedNeareastSmallerSnakeDistance(state, snake));

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

        if ( this.epsilon != 0 ) {
            this.epsilon = Double.max(0.2, this.epsilon * 0.9995);
            //System.out.println(this.epsilon + " => " + this.base_epsilon);
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

        // No Update
    }

    private boolean isAppleInNextMove(SnakeGame state, AgentAction action, Snake snake) {
        Position snakeHeadPos = snake.getPositions().getFirst();
        Position nextPos = getNewPosition(snakeHeadPos, action, state.getSizeX(), state.getSizeY());
        int nextX = nextPos.getX();
        int nextY = nextPos.getY();

        for (Item item : state.getItems()) {
            if (item.getItemType() == ItemType.APPLE && item.getX() == nextX && item.getY() == nextY) {
                return true;
            }
        }
        return false;
    }

    private double normalizedAppleDistance(SnakeGame state, Snake snake, AgentAction action) {
        Position snakeHeadPos = snake.getPositions().getFirst();
        Position nextPos = getNewPosition(snakeHeadPos, action, state.getSizeX(), state.getSizeY());
        int headX = nextPos.getX();
        int headY = nextPos.getY();
        int gridSizeX = state.getSizeX();
        int gridSizeY = state.getSizeY();
        int minDistance = Integer.MAX_VALUE;

        for (Item item : state.getItems()) {
            if (item.getItemType() == ItemType.APPLE) {
                int distance = Math.abs(item.getX() - headX) + Math.abs(item.getY() - headY);
                minDistance = Math.min(minDistance, distance);
            }
        }

        // Au cas où il n'y a pas de pomme (ça peut arriver ?)
        if (minDistance == Integer.MAX_VALUE) return 1.0;

        // Normalisation améliorée
        return (double) minDistance / (gridSizeX + gridSizeY);
    }

    private boolean willDieNextMove(SnakeGame state, AgentAction action, Snake snake) {
        Position snakeHeadPos = snake.getPositions().getFirst();
        Position nextPos = getNewPosition(snakeHeadPos, action, state.getSizeX(), state.getSizeY());
        int nextX = nextPos.getX();
        int nextY = nextPos.getY();

        return isWall(state, nextX, nextY) || isOccupiedBySnake(state, nextX, nextY, snake.getId());
    }

    private double normalizedBorderDistance(SnakeGame state, Snake snake, AgentAction action) {
        Position snakeHeadPos = snake.getPositions().getFirst();
        Position nextPos = getNewPosition(snakeHeadPos, action, state.getSizeX(), state.getSizeY());

        int headX = nextPos.getX();
        int headY = nextPos.getY();
        int gridSizeX = state.getSizeX();
        int gridSizeY = state.getSizeY();

        // Distances aux bords
        int distRight = gridSizeX - headX - 1;
        int distDown = gridSizeY - headY - 1;

        // Distance minimale à un bord
        int minDistance = Math.min(Math.min(headX, distRight), Math.min(headY, distDown));

        // Distance maximale possible à un bord (pour normaliser)
        int maxPossibleDistance = Math.min(gridSizeX, gridSizeY) / 2;

        // Évite la division par zéro
        if (maxPossibleDistance == 0) {
            return 0.0;
        }

        return (double) minDistance / maxPossibleDistance;
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

        return (minDistance == Integer.MAX_VALUE) ? 1.0 : (double) minDistance / (gridSizeX + gridSizeY);
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

        return (minDistance == Integer.MAX_VALUE) ? 1.0 : (double) minDistance / (gridSizeX + gridSizeY);
    }

    private Position getNewPosition(Position currentPosition, AgentAction action, int maxX, int maxY) {
        int newX = currentPosition.getX();
        int newY = currentPosition.getY();

        switch (action) {
            case MOVE_UP -> newY = (newY - 1 + maxY) % maxY;
            case MOVE_DOWN -> newY = (newY + 1) % maxY;
            case MOVE_LEFT -> newX = (newX - 1 + maxX) % maxX;
            case MOVE_RIGHT -> newX = (newX + 1) % maxX;
            default -> {}
        }

        return new Position(newX, newY);
    }

    private boolean isWall(SnakeGame state, int x, int y) {
        return state.getWalls()[x][y];
    }

    private boolean isOccupiedBySnake(SnakeGame state, int x, int y, int snakeID) {
        Position pos = new Position(x, y);
        for(Snake snake : state.getSnakes()) {
            if ( snake.getId() == snakeID ) {
                if ( ListPosContains(snake.getPositions().subList(1, snake.getPositions().size()), pos) ) return true;
            }
            else {
                if ( ListPosContains(snake.getPositions(), pos) ) return true;

            }
        }
        return false;
    }

    public static boolean ListPosContains(List<Position> list, Position pos) {
        for (Position p : list) {
            if (p.getX() == pos.getX() && p.getY() == pos.getY()) {
                return true;
            }
        }
        return false;
    }

}
