/**
 * 
 */
package com.nfl.kfb.mapper.shop;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DuplicateKeyException;


/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 *
 */
public interface AnysdkpayMapper {

//	public Shop selectShop(@Param("shopVer") int shopVer, @Param("itemId") int itemId);
//
//	public List<Shop> selectShopList(@Param("shopVer") int shopVer);
//	
//	public ShopCurrency selectShopCurrency(@Param("store") String store, @Param("prodId") String prodId);
//	
//	public void insertStoreReceipt(@Param("appId") String appId, @Param("receipt") String receipt, @Param("buyDt") int buyDt)
//		throws DuplicateKeyException;
	
	public Anysdkpay selectAnysdkpayByorderid(@Param("order_id") String order_id);
	
	
	public void      insertAnysdkpay(Anysdkpay anysdkpay);
	
	@Select("SELECT IFNULL(SUM(amount),0) FROM anysdkpay WHERE user_id = #{appId}")
	public int sumOfQQPay(@Param("appId") String appId);
	
}
