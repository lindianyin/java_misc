/**
 * 
 */
package com.nfl.kfb.mapper.adpopcorn;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
public interface AdPOPcornMapper {
	
	int insertAdPOPcorn(@Param("rewardKey") String rewardKey, @Param("usn") String usn, @Param("itemKey") String itemKey
			, @Param("quantity") int quantity, @Param("mediaKey") String mediaKey, @Param("campaignKey") String campaignKey
			, @Param("epoch") int epoch);

}
