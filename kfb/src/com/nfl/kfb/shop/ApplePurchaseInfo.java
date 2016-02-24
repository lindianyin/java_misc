/**
 * 
 */
package com.nfl.kfb.shop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

/**
 * @author KimSeongsu
 * @since 2013. 8. 16.
 *
 */
public class ApplePurchaseInfo {
	
//	private String itemId;
//	private String originalTransactionId;
//	private String purchaseDate;
	private String productId;
	private String transactionId;
//	private String quantity;
//	private String originalPurchaseDate;
//	private String bid;
//	private String bvrs;
	private final String rawData;

	public ApplePurchaseInfo(String receipt) {
		rawData = receipt;//new String(new Base64().decode(receipt));
		
		// plainText will be like below...
//		{
//			"signature" = "aaaaaa_base64";
//			"purchase-info" = "bbbb_base64";
//			"environment" = "Sandbox";
//			"pod" = "100";
//			"signing-status" = "0";
//		}
		
		// parse text from "purchase-info"
		Pattern p = Pattern.compile("\"purchase-info\" = \"(.*?)\";");
		Matcher m = p.matcher(rawData);
		
		productId = getValue(rawData, "product_id");
		transactionId = getValue(rawData, "transaction_id");
		
		
		if (m.find()) {
			String purchaseInfoBase64 = m.group(1);
			String purchaseInfo = new String(new Base64().decode(purchaseInfoBase64));
			
//			itemId = getValue(purchaseInfo, "item-id");
//			originalTransactionId = getValue(purchaseInfo, "original-transaction-id");
//			purchaseDate = getValue(purchaseInfo, "purchase-date");

//			quantity = getValue(purchaseInfo, "quantity");
//			originalPurchaseDate = getValue(purchaseInfo, "original-purchase-date");
//			bid = getValue(purchaseInfo, "bid");
//			bvrs = getValue(purchaseInfo, "bvrs");
		}
	
	}
	
	private final String getValue(String text, String key) {
		Pattern p = Pattern.compile("\"" + key + "\":\"(.*?)\"");
		Matcher m = p.matcher(text);
		
		if (m.find()) {
			return m.group(1);
		}
		
		return null;
	}

//	public String getItemId() {
//		return itemId;
//	}

//	public String getOriginalTransactionId() {
//		return originalTransactionId;
//	}

//	public String getPurchaseDate() {
//		return purchaseDate;
//	}

	public String getProductId() {
		return productId;
	}

//	public String getQuantity() {
//		return quantity;
//	}

//	public String getOriginalPurchaseDate() {
//		return originalPurchaseDate;
//	}

//	public String getBid() {
//		return bid;
//	}

//	public String getBvrs() {
//		return bvrs;
//	}

	public String getTransactionId() {
		return transactionId;
	}

	public String getRawData() {
		return rawData;
	}

}
