/**
 * 
 */
package com.nfl.kfb.shop;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 26.
 *
 */
public class GooglePurchaseInfo {
	
//	{ "orderId" : "12999763169054705758.1345892456829793",
//		  "packageName" : "com.nfl.game.kr.kungfubird",
//		  "productId" : "com.nfl.game.kr.kungfubird_02",
//		  "purchaseState" : 0,
//		  "purchaseTime" : 1377500707000,
//		  "purchaseToken" : "cbcsivqwceitzgfezjcpfbsq.AO-J1OwOvzdeKhJ-A0bm_5MlhrnlB-ugvTyILowrsybaSKY7c-GXcH8FTHj_GtfxQXzKtfFnMMQ_F9-Msw81b3bIZsDYcVyAjh8pqmPHtTOSlrbPg_IQfLvf4ZIUzOJlB-mzgtSGOtDIxPw0nHqkLFTlOCZ4CyPSjw"
//		}
	
	private String orderId;
	private String productId;
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

}
