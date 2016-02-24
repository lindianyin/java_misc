/**
 * 
 */
package com.nfl.kfb.adpopcorn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.adpopcorn.AdPOPcornMapper;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.model.AdPOPcornResponse;
import com.nfl.kfb.model.AdPOPcornResponse.ADPOPCORN_RESULT_CODE;
import com.nfl.kfb.shop.ShopService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
@Service(value="AdPOPcornServiceImpl")
public class AdPOPcornServiceImpl implements AdPOPcornService {
	
	@Autowired
	@Qualifier("ShopServiceImpl")
	private ShopService shopService;
	
	@Autowired
	private MailMapper mailMapper;
	
	@Autowired
	private AdPOPcornMapper adPOPcornMapper;
	
	@Transactional
	@Override
	public AdPOPcornResponse rewardAdPOPcorn(DateUtil dateUtil, String usn, String rewardkey,
			String itemkey, int itemId, int quantity, String mediakey, String campaignkey) throws DuplicateKeyException {
		// check duplicate
		adPOPcornMapper.insertAdPOPcorn(rewardkey, usn, itemkey, quantity, mediakey, campaignkey, dateUtil.getNowEpoch());
		
		// add to mailbox
		final int mailDelDt = dateUtil.getNowEpoch() + DebugOption.ADPOPCORN_ITEM_MAIL_KEEP;
		Mail mail = new Mail();
		mail.setSender(Mail.SENDER_ID_ADMIN);
		mail.setOwner(usn);
		mail.setItem(itemId);
		mail.setCnt(quantity);
		mail.setDelDt(mailDelDt);
		mail.setMsg(DebugOption.ADPOPCORN_ITEM_MAIL_MSG);
//		mailMapper.insertMail(Mail.SENDER_ID_ADMIN, usn, itemId, quantity, mailDelDt, DebugOption.ADPOPCORN_ITEM_MAIL_MSG);
		mailMapper.insertMail(mail);
		
		return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.SUCCESS);
	}
	


}
