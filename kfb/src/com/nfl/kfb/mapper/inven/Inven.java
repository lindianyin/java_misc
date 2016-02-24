/**
 * 
 */
package com.nfl.kfb.mapper.inven;



/**
 * @author KimSeongsu
 * @since 2013. 7. 19.
 *
 */
public class Inven {
	
	/**
	 * 캐릭터 공통 적용 아이템일 경우 'CH_ID' 값<br>
	 * e.g. 소모아이템(조공수, 기회환, ...)<br>
	 */
	public static final int CHARACTER_ID_FOR_COMMON_ITEM = 0;
	
	private String appId;
	private int chId;
	private int itemId;
	private int itemLv = 1;
	private int itemCnt = 1;
	private boolean valueChanged = false;		// InvenManager에서 사용
	
	public Inven() {
	}
	
	public Inven(String appId, int chId, int itemId, int itemLv, int itemCnt) {
		setAppId(appId);
		setChId(chId);
		setItemId(itemId);
		setItemLv(itemLv);
		setItemCnt(itemCnt);
	}
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getChId() {
		return chId;
	}

	public void setChId(int chId) {
		this.chId = chId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemLv() {
		return itemLv;
	}

	public void setItemLv(int itemLv) {
		this.itemLv = itemLv;
	}

	public int getItemCnt() {
		return itemCnt;
	}

	public void setItemCnt(int itemCnt) {
		this.itemCnt = itemCnt;
	}

	public boolean isValueChanged() {
		return valueChanged;
	}

	public void setValueChanged(boolean valueChanged) {
		this.valueChanged = valueChanged;
	}

}
