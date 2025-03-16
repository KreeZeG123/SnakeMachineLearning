package item;

import java.io.Serializable;

import utils.ItemType;

public class Item implements Serializable {

	private int x;
	private int y;
	
	private ItemType itemType;
	
	
	public Item(int x, int y, ItemType itemType) {
		
		this.setX(x);
		this.setY(y);
		this.setItemType(itemType);
		
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


	public ItemType getItemType() {
		return itemType;
	}


	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}
	
}
