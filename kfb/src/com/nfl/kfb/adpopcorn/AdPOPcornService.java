/**
 * 
 */
package com.nfl.kfb.adpopcorn;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.model.AdPOPcornResponse;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
public interface AdPOPcornService {

	@Transactional
	AdPOPcornResponse rewardAdPOPcorn(DateUtil dateUtil, String usn, String rewardkey,
			String itemkey, int itemId, int quantity, String mediakey, String campaignkey) throws DuplicateKeyException;

}
