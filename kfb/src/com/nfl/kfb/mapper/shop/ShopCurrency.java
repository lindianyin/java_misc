/**
 * 
 */
package com.nfl.kfb.mapper.shop;

import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 8. 20.
 *
 */
public class ShopCurrency {
	
	private STORE_TYPE store;
	private int itemId;
	private String prodId;
	private float currency;
	private String currencyType;
	private String name;
	
	public STORE_TYPE getStore() {
		return store;
	}
	
	public void setStore(String store) {
		this.store = STORE_TYPE.valueOf(store);
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public float getCurrency() {
		return currency;
	}

	public void setCurrency(float currency) {
		this.currency = currency;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
