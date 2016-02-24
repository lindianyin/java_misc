/**
 * 
 */
package com.nfl.kfb.mapper.shop;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DuplicateKeyException;

import com.googlecode.ehcache.annotations.Cacheable;


/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 *
 */
public interface ShopMapper {

	public Shop selectShop(@Param("shopVer") int shopVer, @Param("itemId") int itemId);

	public List<Shop> selectShopList(@Param("shopVer") int shopVer);
	
	public ShopCurrency selectShopCurrency(@Param("store") String store, @Param("prodId") String prodId);
	
	@Cacheable(cacheName="shopCurrencyCache")
	public List<ShopCurrency> selectShopCurrencyByStore(@Param("store") String store);
	
	public void insertStoreReceipt(@Param("appId") String appId, @Param("receipt") String receipt, @Param("buyDt") int buyDt)
		throws DuplicateKeyException;
	
	@Select("SELECT COUNT(*) FROM anysdkpay WHERE order_id = #{order_id}")
	public int countOfOrder(@Param("order_id") String order_id);
	
	
}
