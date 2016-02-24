/**
 * 
 */
package com.nfl.kfb.shop;

/**
 * @author KimSeongsu
 * @since 2013. 7. 22.
 *
 */
public class AppleVerify {
	
//	{ "receipt" : { "bid" : "com.aaa.InAppPurchaseTest00",
//	      "bvrs" : "1.0",
//	      "item_id" : "472882111",
//	      "original_purchase_date" : "2011-10-31 09:43:51 Etc/GMT",
//	      "original_transaction_id" : "1000000011611111",
//	      "product_id" : "com.aaa.InAppPurchaseTest00.ProductTest00",
//	      "purchase_date" : "2011-10-31 09:43:51 Etc/GMT",
//	      "quantity" : "1",
//	      "transaction_id" : "1000000011611111"
//	    },
//	  "status" : 0
//	}
	
	private int status;
	private AppleReceipt receipt;

	public String receiptString;
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public AppleReceipt getReceipt() {
		return receipt;
	}

	public void setReceipt(AppleReceipt receipt) {
		this.receipt = receipt;
	}

}
