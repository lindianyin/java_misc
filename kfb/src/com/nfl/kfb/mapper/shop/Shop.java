/**
 * 
 */
package com.nfl.kfb.mapper.shop;


/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 *
 */
public class Shop {
	
	private int itemId;
	private String name;
	private int cnt;
	private int priceType;
	private float price;
	private int instant;
	private int lvupPrice;
	private int lvupBase;
	
	
	public int getItemId() {
		return itemId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getInstant() {
		return instant;
	}

	public void setInstant(int instant) {
		this.instant = instant;
	}

	public int getLvupPrice() {
		return lvupPrice;
	}

	public void setLvupPrice(int lvupPrice) {
		this.lvupPrice = lvupPrice;
	}

	public int getPriceType() {
		return priceType;
	}

	public void setPriceType(int priceType) {
		this.priceType = priceType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLvupBase() {
		return lvupBase;
	}

	public void setLvupBase(int lvupBase) {
		this.lvupBase = lvupBase;
	}

}
