package strategy.algorithms;

import utils.Position;

public class DistManhattan {

    public static int distManhattan(Position position1, Position position2) {
        return Math.abs(position2.getX() - position1.getX()) + Math.abs(position2.getY() - position1.getY());
    }

    public static int distManhattanWrap(Position position1, Position position2, int width, int height) {
        // Calcul des distances directes
        int dx = Math.abs(position2.getX() - position1.getX());
        int dy = Math.abs(position2.getY() - position1.getY());

        // Prendre en compte la distance via les bords opposés
        dx = Math.min(dx, width - dx);
        dy = Math.min(dy, height - dy);

        // Retourner la distance de Manhattan adaptée
        return dx + dy;
    }

}
