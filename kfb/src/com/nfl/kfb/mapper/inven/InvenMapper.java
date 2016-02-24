/**
 * 
 */
package com.nfl.kfb.mapper.inven;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.nfl.kfb.mapper.inv.Inv;

/**
 * @author KimSeongsu
 * @since 2013. 7. 19.
 *
 */
public interface InvenMapper {
	
	List<Inven> selectAllItem(@Param("appId") String appId);
	
	Inven selectItem(@Param("appId") String appId, @Param("chId") int chId, @Param("itemId") int itemId);
	
	int countItem(@Param("appId") String appId, @Param("chId") int chId, @Param("itemId") int itemId);

//	/**
//	 * INVEN 에 아이템 insert on duplicate update<br>
//	 * @param appId
//	 * @param chId
//	 * @param itemId
//	 * @param itemLv
//	 * @param itemCnt
//	 * @param incLv
//	 * @param incCnt
//	 */
//	void insertOrIncItem(@Param("appId") String appId
//			, @Param("chId") int chId
//			, @Param("itemId") int itemId
//			, @Param("itemLv") int itemLv
//			, @Param("itemCnt") int itemCnt
//			, @Param("incLv") int incLv
//			, @Param("incCnt") int incCnt );
	
	/**
	 * 아이템 INSERT<br>
	 * @param item
	 */
	void insertItem(Inven item);
	
//	/**
//	 * 아이템 lv, cnt 를 increase<br>
//	 * @param appId
//	 * @param chId
//	 * @param itemId
//	 * @param itemLv
//	 * @param itemCnt
//	 */
//	int incItem(@Param("appId") String appId
//			, @Param("chId") int chId
//			, @Param("itemId") int itemId
//			, @Param("incLv") int incLv
//			, @Param("incCnt") int incCnt );
	
	void insertIgnoreItem(@Param("appId") String appId
			, @Param("chId") int chId
			, @Param("itemId") int itemId
			, @Param("itemLv") int itemLv
			, @Param("itemCnt") int itemCnt	);

	int updateItem(Inven item);
	
	int deleteItem(Inven item);

	void deleteInven(@Param("appId") String appId);
	
	
	//@Select("SELECT * from inven where APPID = #{appId} and CH_ID = ITEM_ID ORDER BY ITEM_LV desc LIMIT 1")
	Inven selectMaxLvPet(@Param("appId") String appId);
	
	
	@Select("SELECT COUNT(*) FROM  anysdkpay WHERE user_id = #{appId}")
	int countOfAnysdkPay(@Param("appId") String appId);
	
	@Select("SELECT COUNT(*) FROM store_receipt WHERE APPID = #{appId}")
	int countOfApplePay(@Param("appId") String appId);
	
}
