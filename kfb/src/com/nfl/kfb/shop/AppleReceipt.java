/**
 * 
 */
package com.nfl.kfb.shop;

/**
 * @author KimSeongsu
 * @since 2013. 7. 22.
 *
 */
public class AppleReceipt {
	
	private String transaction_id;
	private String product_id;
	
	public String getTransaction_id() {
		return transaction_id;
	}
	
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

}
