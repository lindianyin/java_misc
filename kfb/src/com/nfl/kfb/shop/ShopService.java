/**
 * 
 */
package com.nfl.kfb.shop;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.CurrencyLog;
import com.nfl.kfb.mapper.logging.logs.BuyItemLog;
import com.nfl.kfb.mapper.logging.logs.CurrencyGameLog;
import com.nfl.kfb.mapper.shop.Anysdkpay;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.shop.ShopCurrency;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_OS;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_PALTFORM;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 *
 */
public interface ShopService {

	public ShopCurrency getAppleCurrency(String applePID);
	
	public ShopCurrency getGoogleCurrency(String googlePID);
	
	public ShopCurrency getAnysdkCurrency(String anysdkPID);
	
	@Transactional
	/**
	 * 아이템 구입<br>
	 * @param dateUtil
	 * @param account
	 * @param chId
	 * @param shop
	 * @param store
	 * @param instant
	 * @param discount		할인율(백분위)
	 * @param disPrice		할인된 가격
	 * @param buyItemLog
	 * @param availableGP
	 * @throws Exception
	 * @return
	 */
	public BuyResponse buyItem(DateUtil dateUtil, Account account, int chId, Shop shop
			, STORE_TYPE store, boolean instant, int discount, int disPrice
			, BuyItemLog buyItemLog, boolean availableGP,int cntt) throws Exception;

	/**
	 * 클라에서 받은 애플 영수증을 애플서버에 확인<br>
	 * productMode로 확인. 21007 에러를 받으면(sandbox라는 뜻) developMode로 다시 확인<br>
	 * @param receipt
	 * @return		verifyReceipt Json 결과를 매핑한 클래스 
	 * @throws Exception
	 */
	public AppleVerify verfifyAppleReceipt(String receipt) throws Exception;
	
	@Transactional
	public BuyAppleProdResponse buyAppleItem(DateUtil dateUtil, Account account, int chId, Shop appleItem, String transactionId
			, CurrencyGameLog currencyGameLog, boolean availableGP) throws DuplicateKeyException;

	@Transactional
	public BuyGoogleProdResponse buyGoogleItem(DateUtil dateUtil, Account account, int chId, Shop googleItem, String orderId
			, CurrencyGameLog currencyGameLog, boolean availableGP) throws DuplicateKeyException;
	
	public boolean hasCharacter(String appId, int chId);

	/**
	 * 클라에서 받은 구글플레이 평문영수증/암호화 영수증을 확인<br>
	 * 암호화를 publicKey로 복호, 평문과 일치하는지 확인<br>
	 * @param purchase
	 * @param base64Signature
	 * @return
	 * @throws Exception 
	 */
	public boolean verfifyGoogleReceiptSignature(String purchase, String base64Signature) throws Exception;

	public GooglePurchaseInfo verifyGooglePurchase(String purchase) throws Exception;

	public void sendPaymentInfoToKakao(DateUtil dateUtil, String appId, KAKAO_PAYMENT_PARAM_PALTFORM kakaoPaymentPlatform, float currency, String currencyType
			, KAKAO_PAYMENT_PARAM_OS kakaoPaymentOs, String ccode, int buyNo) throws ClientProtocolException, IOException, Exception;

	
	//buy anysdk item
	@Transactional
	public boolean buyAnysdkItem(Account account,Shop anysdkItem, CurrencyGameLog currencyGameLog,Anysdkpay anysdkPay
			,boolean availableGP);

	
	
}
