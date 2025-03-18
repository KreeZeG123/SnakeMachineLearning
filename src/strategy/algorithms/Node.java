package strategy.algorithms;

import utils.Position;

public class Node {

    private Node parent;

    private Position pos;

    int realCost;

    int heuristicCost;

    public Node(Node parent, Position pos, int realCost, int heuristicCost) {
        this.parent = parent;
        this.pos = pos;
        this.realCost = realCost;
        this.heuristicCost = heuristicCost;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Position getPos() {
        return pos;
    }

    public int getRealCost() {
        return realCost;
    }

    public void setRealCost(int realCost) {
        this.realCost = realCost;
    }

    public int getHeuristicCost() {
        return heuristicCost;
    }

    public void setHeuristicCost(int heuristicCost) {
        this.heuristicCost = heuristicCost;
    }

    public String toStringFull() {
        return "Node{" +
                "parent=" + parent +
                ", pos=" + pos +
                ", realCost=" + realCost +
                ", heuristicCost=" + heuristicCost +
                '}';
    }

    @Override
    public String toString() {
        return pos.toString();
    }
}
