/**
 * 
 */
package com.nfl.kfb.mapper.gamble;

/**
 * @author KimSeongsu
 * @since 2013. 11. 7.
 *
 */
public class GambleProb {
	
	private int gambleProbId;
	private int itemId;
	private int itemCnt;
	private int probability;
	private int missProb;
	
	public int getGambleProbId() {
		return gambleProbId;
	}
	
	public void setGambleProbId(int gambleProbId) {
		this.gambleProbId = gambleProbId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemCnt() {
		return itemCnt;
	}

	public void setItemCnt(int itemCnt) {
		this.itemCnt = itemCnt;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public int getMissProb() {
		return missProb;
	}

	public void setMissProb(int missProb) {
		this.missProb = missProb;
	}

}
