/**
 * 
 */
package com.nfl.kfb.shop;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 11. 21.
 *
 */
public class ShopDataResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public ShopDataResponse(ReturnCode rc) {
		super(rc);
		
		put("IT", new LinkedList<Map<String,Object>>());
	}
	
	public void addIT(int itemId, int itemCnt, int instant, int priceType, int price, int lvupBase, int lvupPrice) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> shopList = (List<Map<String, Object>>) get("IT");
		
		Map<String, Object> shopData = new HashMap<String, Object>();
		shopData.put("ITID", itemId);
		shopData.put("CNT", itemCnt);
		shopData.put("INSTANT", instant);
		shopData.put("TYPE", priceType);
		shopData.put("PRICE", price);
		shopData.put("UPBASE", lvupBase);
		shopData.put("UPPRICE", lvupPrice);
		
		shopList.add(shopData);
	}
	
}
