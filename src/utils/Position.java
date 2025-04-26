package utils;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable{

	private int x;
	private int y;


	public Position(int x, int y) {
		
		this.x = x;
		this.y = y;
		
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Position position)) return false;
        return x == position.x && y == position.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return "Pos{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public static Position getNewPosition(Position currentPosition, AgentAction action, int maxX, int maxY) {
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

}
