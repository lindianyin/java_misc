/**
 * 
 */
package com.nfl.kfb.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.HashAttributeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.logs.BuyItemLog;
import com.nfl.kfb.mapper.logging.logs.CurrencyGameLog;
import com.nfl.kfb.mapper.shop.Anysdkpay;
import com.nfl.kfb.mapper.shop.AnysdkpayMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.shop.ShopCurrency;
import com.nfl.kfb.mapper.shop.ShopMapper;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_OS;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_PALTFORM;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 * 
 */
@Service(value = "ShopServiceImpl")
public class ShopServiceImpl extends AbstractKfbService implements ShopService {

	private static final Logger logger = LoggerFactory
			.getLogger(ShopServiceImpl.class);

	@Autowired
	private ShopMapper shopMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	private GambleMapper gambleMapper;

	@Autowired
	private AnysdkpayMapper anysdkpayMapper;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	private TaskController taskController;

	@Override
	@Transactional
	public BuyResponse buyItem(DateUtil dateUtil, Account account, int chId,
			Shop shop, STORE_TYPE store, boolean instant, int discount,
			int disPrice, BuyItemLog buyItemLog, boolean availableGP,int cntt)
			throws Exception {
		logger.debug("BUY_ITEM : APPID={}, STORE={}, INST={}, DC={}, DCP={}",
				account.getAppId(), store.getValue(), instant, discount,
				disPrice);

		int itemCnt = cntt; // 무조건 1개 구매
		if(itemCnt == -1){
			itemCnt = 1;
		}
		
		int priceType = shop.getPriceType();
		int price = (int) shop.getPrice();
		
		if(cntt != -1){
			
		}
		
		

		Inven newItem = null;
		Inven existItem = null;

		// 캐릭터, 장비, 펫은 업그레이드 가격이 따로 존재하므로,
		// 이 경우에는 이미 아이템을 가지고 있는지 확인
		final ITEM_TYPE itemType = DebugOption.getItemType(shop.getItemId());
		switch (itemType) {
		case CHARACTER:
		case EQUIP:
		case PET:
			newItem = createInvenItemFromShop(account.getAppId(), chId, shop,
					itemCnt, true);
			existItem = invenMapper.selectItem(account.getAppId(),
					newItem.getChId(), newItem.getItemId());
			if (existItem != null) {
				price = shop.getLvupBase() + shop.getLvupPrice()
						* existItem.getItemLv();
				priceType = DebugOption.SHOP_PRICE_GOLD;
			}
			break;
		default:
			break;
		}

		// 즉시구매는 무조건 여의주로만 살 수 있음
		if (instant) {
			priceType = DebugOption.SHOP_PRICE_BALL;
			price = shop.getInstant();
		}

		switch (discount) {
		case 0:
			// 일반상점 할인율
			break;
		case 20:
			// 추천아이템 할인율
			if (itemType != ITEM_TYPE.SKILL && itemType != ITEM_TYPE.CONSUME) {
				throw new RuntimeException("This item is not for discount");
			}
			break;
		case 30:
			// 사용아이템 재구매 할인율
			if (itemType != ITEM_TYPE.SKILL && itemType != ITEM_TYPE.CONSUME) {
				throw new RuntimeException("This item is not for discount");
			}
			break;
		default:
			// 그외의 할인율은 없음
			throw new RuntimeException("Unknown Discount Rate");
		}

		// 할인율 적용
		if (discount > 0) {
			price = (price * (100 - discount) / 100) / 10 * 10; // 10단위 절삭을 위해
																// /10 * 10함
		}
		// 구입개수 적용
		price = price * itemCnt;
		if(itemCnt > 1){
			price = (int)(price  * 0.6);//快速购买打折
		}
		if (price != disPrice) {
			// throw new Exception("Wrong DiscountPrice, ItemId=" +
			// shop.getItemId() +", DC=" + discount + ", DCP=" + disPrice +
			// ", instant=" + instant + ", EXPECTED_DCP=" + price);
		}

		if (price <= 0) {
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			map.put(602, 689);
			map.put(603, 1980);
			map.put(704, 880);
			map.put(705, 1680);
			if(map.containsKey(shop.getItemId())){
				price = map.get(shop.getItemId());
			}
			//throw new Exception("This item is not for sale. price:" + price); // 가격이
		}

		if (account.getBall() < 0 || account.getGold() < 0
				|| account.getPunch() < 0)
			throw new RuntimeException("Wrong now accountItem. NOW_GOLD="
					+ account.getGold() + ", NOW_BALL=" + account.getBall()
					+ ", NOW_PUNCH=" + account.getPunch());

		buyItemLog.setDiscount(discount);
		buyItemLog.setStore(store);

		// 구입 결제 단위에 따라서 처리
		switch (priceType) {
		case DebugOption.SHOP_PRICE_BALL: // 여의주로 구입
			if (account.getBall() < price) {
				BuyResponse buyResponse = new BuyResponse(
						ReturnCode.NOT_ENOUGH_MONEY); // 돈이 모자람
				buyResponse.setITID(0);
				buyResponse.setITCNT(0);
				buyResponse.setGD(account.getGold());
				buyResponse.setBL(account.getBall());
				buyResponse.setPN(account.getPunch());
				buyResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				buyResponse.setADDGP(0);
				return buyResponse;
			}
			addBall(account, -price);
			buyItemLog.setAddBall(buyItemLog.getAddBall() - price);
			buyItemLog.setNowBall(account.getBall());
			break;

		// case DebugOption.SHOP_PRICE_DOLLAR: // default에서 처리
		// throw new Exception("Couldn't buy item by dollar"); // 여기서는 현금으로는 살 수
		// 없음. 현금결제 api는 따로 있음.

		case DebugOption.SHOP_PRICE_GOLD:
			if (account.getGold() < price) {
				BuyResponse buyResponse = new BuyResponse(
						ReturnCode.NOT_ENOUGH_MONEY); // 돈이 모자람
				buyResponse.setITID(0);
				buyResponse.setITCNT(0);
				buyResponse.setGD(account.getGold());
				buyResponse.setBL(account.getBall());
				buyResponse.setPN(account.getPunch());
				buyResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				buyResponse.setADDGP(0);
				return buyResponse;
			}
			addGold(account, -price);
			buyItemLog.setAddGold(buyItemLog.getAddGold() - price);
			buyItemLog.setNowGold(account.getGold());

			break;

		case DebugOption.SHOP_PRICE_PUNCH:
			// 주먹이 리젠될게 있는지 계산
			account.regenPunch(dateUtil.getNowEpoch());
			if (account.getPunch() < price) {
				BuyResponse buyResponse = new BuyResponse(
						ReturnCode.NOT_ENOUGH_MONEY); // 돈이 모자람
				buyResponse.setITID(0);
				buyResponse.setITCNT(0);
				buyResponse.setGD(account.getGold());
				buyResponse.setBL(account.getBall());
				buyResponse.setPN(account.getPunch());
				buyResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				buyResponse.setADDGP(0);
				return buyResponse;
			}
			// 주먹 사용
			account.usePunch(dateUtil.getNowEpoch(), price);
			buyItemLog.setAddPunch(buyItemLog.getAddPunch() - price);
			buyItemLog.setNowPunch(account.getPunch());

			break;

		default:
			// 그 외의 결제단위는 없음
			throw new Exception("Cannot buy priceType, priceType=" + priceType
					+ ", ItemId=" + shop.getItemId() + ", DC=" + discount
					+ ", DCP=" + disPrice + ", instant=" + instant
					+ ", EXPECTED_DCP=" + price);
		}

		BuyResponse buyResponse = new BuyResponse(ReturnCode.SUCCESS);

		if (isAccountItem(shop.getItemId())) {
			addAccountItem(dateUtil, account, shop, itemCnt, buyItemLog);
			buyResponse.setITID(shop.getItemId());
			buyResponse.setITCNT(itemCnt);

			ITEM_TYPE itemType2 = DebugOption.getItemType(shop.getItemId());
			switch (itemType2) {
			case BALL:

				break;
			case GOLD: {
				{
					// tasktask
					List<TaskDetail> newTaskDetailList = taskController
							.newTaskDetailList(TASK_TYPE.BUY_GOLD_TIMES, 1);
					taskController.submitTaskDetailInfo(account.getAppId(),
							newTaskDetailList);
					// System.out.println("BUY_GOLD_TIMES");
				}
				{
					// tasktask
					List<TaskDetail> newTaskDetailList = taskController
							.newTaskDetailList(TASK_TYPE.BUY_GOLD,
									shop.getCnt() * itemCnt);
					taskController.submitTaskDetailInfo(account.getAppId(),
							newTaskDetailList);
					// System.out.println("BUY_GOLD");
				}
			}
				break;
			case PUNCH: {
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.BUY_PUNCH_TIMES, 1);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
				// System.out.println("SHOP_PRICE_PUNCH");
			}
				break;
			default:
				break;
			}

		} else if (isInvenItem(shop.getItemId())) {
			buyItemLog.setItemId(shop.getItemId());
			buyItemLog.setItemCnt(buyItemLog.getItemCnt() + itemCnt);

			if (newItem == null) {
				newItem = createInvenItemFromShop(account.getAppId(), chId,
						shop, itemCnt, true);
				existItem = invenMapper.selectItem(account.getAppId(),
						newItem.getChId(), newItem.getItemId());
			}

			buyResponse.setITID(newItem.getItemId());
			buyResponse.setITCNT(itemCnt);

			switch (itemType) {
			case CHARACTER:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.BUY_ROLE, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("BUY_ROLE");
					}
				} else {
					int newLv = existItem.getItemLv() + newItem.getItemLv();
					if (newLv > DebugOption.MAX_CHARACTER_LV) {
						throw new RuntimeException(
								"Cannot buy character item. character_lv exceed limit.");
					}
					existItem.setItemLv(newLv);
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.UP_ROLE, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("UP_ROLE");
					}

				}

				break;

			case SKILL:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.BUY_SKILL, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("BUY_SKILL");
					}
				} else {
					existItem.setItemLv(Math.min(DebugOption.MAX_SKILL_LV,
							existItem.getItemLv() + newItem.getItemLv()));
					existItem.setItemCnt(existItem.getItemCnt()
							+ newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
					
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.BUY_SKILL, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("BUY_SKILL");
					}
					
					
					
					
				}
				break;

			case CONSUME:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemCnt(existItem.getItemCnt()
							+ newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}

				{
					// tasktask
					List<TaskDetail> newTaskDetailList = taskController
							.newTaskDetailList(TASK_TYPE.SHOP_BUY_ITEM, 1);
					taskController.submitTaskDetailInfo(account.getAppId(),
							newTaskDetailList);
					// System.out.println("SHOP_BUY_ITEM");
				}
				break;

			case EQUIP:
				//System.out.println("equip");
				if (existItem == null) {
					invenMapper.insertItem(newItem);
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.BUY_EQUIP, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("BUY_EQUIP");
					}
				} else {
					int newLv = existItem.getItemLv() + newItem.getItemLv();
					if (newLv > DebugOption.MAX_EQUIP_LV) {
						throw new RuntimeException(
								"Cannot buy equip item. equip_lv exceed limit.");
					}
					existItem.setItemLv(newLv);
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");

					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.UP_EQUIP, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("UP_EQUIP");
					}
				}
				break;

			case PET:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					int newLv = existItem.getItemLv() + newItem.getItemLv();
					if (existItem.getItemLv() + newItem.getItemLv() > DebugOption.MAX_PET_LV) {
						throw new RuntimeException(
								"Cannot buy pet. pet_lv exceed limit.");
					}
					existItem.setItemLv(newLv);
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
					{
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.UP_PET, 1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
						// System.out.println("UP_PET");
					}

				}
				break;

			default: // 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException(
						"Cannot add item to inven. this is not inven item");
			}
		}

		// ACCOUNT는 무조건 바뀌므로 update
		accountMapper.updateAccountItem(account);

		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_BUY_ITEM;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}

		buyResponse.setGD(account.getGold());
		buyResponse.setBL(account.getBall());
		buyResponse.setPN(account.getPunch());
		buyResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		buyResponse.setADDGP(gamblePoint);

		buyItemLog.setNowGold(account.getGold());
		buyItemLog.setNowBall(account.getBall());
		buyItemLog.setNowPunch(account.getPunch());
		buyItemLog.setSuccess(true);

		return buyResponse;
	}

	@Override
	public ShopCurrency getAppleCurrency(String applePID) {
		return shopMapper.selectShopCurrency(STORE_TYPE.APPLE.name(), applePID);
	}

	@Override
	public ShopCurrency getGoogleCurrency(String googlePID) {
		return shopMapper.selectShopCurrency(STORE_TYPE.GOOGLE.name(),
				googlePID);
	}

	@Override
	public ShopCurrency getAnysdkCurrency(String anysdkPID) {
		// TODO Auto-generated method stub
		return shopMapper.selectShopCurrency(STORE_TYPE.ANYSDK.name(),
				anysdkPID);
	}

	@Override
	public AppleVerify verfifyAppleReceipt(String receipt) throws Exception {
		// 항상 프로덕션 URL을 먼저 호출하고, 21007이라는 상태 코드를 반환하면 샌드박스를 호출하도록 한다.
		// 위와 같이 하면, 어플리케이션이 테스트 중일 때나, 검수 중일 때나 앱스토어에 라이브 되고 있을 때나 URL을 변경해 줄
		// 필요가 없다.
		// 상태코드 21007은 해당 Receipt가 샌드박스용 임을 뜻한다.
		// {"status":21007}

		final String jsonData = "{\"receipt-data\":\"" + receipt + "\"}";

		DefaultHttpClient httpclient = null;
		HttpPost httppost;
		HttpResponse response;
		HttpEntity entity;
		String responseJson;
		try {
			httpclient = new DefaultHttpClient();
			HttpParams params = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);

			httppost = new HttpPost(
					"https://buy.itunes.apple.com/verifyReceipt");
			httppost.setEntity(new StringEntity(jsonData));
			response = httpclient.execute(httppost);
			entity = response.getEntity();
			responseJson = EntityUtils.toString(entity);
			EntityUtils.consume(entity);

			AppleVerify appleVerify = parseJson(AppleVerify.class, responseJson);
			if (appleVerify.getStatus() == 21007) {
				httppost = new HttpPost(
						"https://sandbox.itunes.apple.com/verifyReceipt");
				httppost.setEntity(new StringEntity(jsonData));
				response = httpclient.execute(httppost);
				entity = response.getEntity();
				responseJson = EntityUtils.toString(entity);
				EntityUtils.consume(entity);

				appleVerify = parseJson(AppleVerify.class, responseJson);
			}
			appleVerify.receiptString = responseJson;
			logger.info(responseJson);

			return appleVerify;
		} finally {
			if (httpclient != null && httpclient.getConnectionManager() != null) {
				httpclient.getConnectionManager().shutdown();
			}
		}
	}

	@Override
	@Transactional
	public BuyAppleProdResponse buyAppleItem(DateUtil dateUtil,
			Account account, int chId, Shop appleItem, String transactionId,
			CurrencyGameLog currencyGameLog, boolean availableGP)
			throws DuplicateKeyException {
		final int itemCnt = 1;

		// 애플 상품은 항상 여의주에 적용
		final ITEM_TYPE itemType = DebugOption.getItemType(appleItem
				.getItemId());
		if (itemType != ITEM_TYPE.BALL)
			throw new RuntimeException("AppleProdItem must be for ball");

		// 이미 사용한 transactionId 이면 DuplicateKeyException 던짐
		shopMapper.insertStoreReceipt(account.getAppId(), transactionId,
				dateUtil.getNowEpoch());

		final int addCnt = appleItem.getCnt() * itemCnt;

		addBall(account, addCnt);

		currencyGameLog.setStore(STORE_TYPE.APPLE);
		currencyGameLog.setItemId(appleItem.getItemId());
		currencyGameLog.setItemCnt(itemCnt);
		currencyGameLog.setNowGold(account.getGold());
		currencyGameLog.setNowPunch(account.getPunch());
		currencyGameLog.setAddBall(currencyGameLog.getAddBall() + addCnt);
		currencyGameLog.setNowBall(account.getBall());

		// 애플 상품은 BALL만 변화시킴
		accountMapper.updateAccountItem(account);

		// 겜블 포인트 지급
		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_CURRENCY_BALL * addCnt;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}

		BuyAppleProdResponse buyAppleProdResponse = new BuyAppleProdResponse(
				ReturnCode.SUCCESS);
		buyAppleProdResponse.setITID(appleItem.getItemId());
		buyAppleProdResponse.setITCNT(itemCnt);
		buyAppleProdResponse.setGD(account.getGold());
		buyAppleProdResponse.setBL(account.getBall());
		buyAppleProdResponse.setPN(account.getPunch());
		buyAppleProdResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));
		buyAppleProdResponse.setADDGP(gamblePoint);

		return buyAppleProdResponse;
	}

	@Override
	@Transactional
	public BuyGoogleProdResponse buyGoogleItem(DateUtil dateUtil,
			Account account, int chId, Shop googleItem, String orderId,
			CurrencyGameLog currencyGameLog, boolean availableGP)
			throws DuplicateKeyException {
		final int itemCnt = 1;

		// 구글 상품은 항상 여의주에 적용
		final ITEM_TYPE itemType = DebugOption.getItemType(googleItem
				.getItemId());
		if (itemType != ITEM_TYPE.BALL)
			throw new RuntimeException("GoogleProdItem must be for ball");

		// 이미 사용한 transactionId 이면 DuplicateKeyException 던짐
		shopMapper.insertStoreReceipt(account.getAppId(), orderId,
				dateUtil.getNowEpoch());

		final int addCnt = googleItem.getCnt() * itemCnt;

		addBall(account, addCnt);

		currencyGameLog.setStore(STORE_TYPE.GOOGLE);
		currencyGameLog.setItemId(googleItem.getItemId());
		currencyGameLog.setItemCnt(itemCnt);
		currencyGameLog.setNowGold(account.getGold());
		currencyGameLog.setNowPunch(account.getPunch());
		currencyGameLog.setAddBall(currencyGameLog.getAddBall() + addCnt);
		currencyGameLog.setNowBall(account.getBall());

		// 구글 상품은 BALL만 변화시킴
		accountMapper.updateAccountItem(account);

		// 겜블 포인트 지급
		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_CURRENCY_BALL * addCnt;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}

		BuyGoogleProdResponse buyGoogleProdResponse = new BuyGoogleProdResponse(
				ReturnCode.SUCCESS);
		buyGoogleProdResponse.setITID(googleItem.getItemId());
		buyGoogleProdResponse.setITCNT(itemCnt);
		buyGoogleProdResponse.setGD(account.getGold());
		buyGoogleProdResponse.setBL(account.getBall());
		buyGoogleProdResponse.setPN(account.getPunch());
		buyGoogleProdResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));
		buyGoogleProdResponse.setADDGP(gamblePoint);

		return buyGoogleProdResponse;
	}

	@Override
	public boolean hasCharacter(String appId, int chId) {
		return DebugOption.getItemType(chId) == ITEM_TYPE.CHARACTER
				&& invenMapper.countItem(appId, chId, chId) == 1;
	}

	@Override
	public boolean verfifyGoogleReceiptSignature(String purchase,
			String base64Signature) throws Exception {
		return DebugOption.verfifyGoogleReceiptSignature(purchase,
				base64Signature);
	}

	@Override
	public GooglePurchaseInfo verifyGooglePurchase(String purchase)
			throws Exception {
		return parseJson(GooglePurchaseInfo.class, purchase);
	}

	@Override
	public void sendPaymentInfoToKakao(DateUtil dateUtil, String appId,
			KAKAO_PAYMENT_PARAM_PALTFORM kakaoPaymentPlatform, float currency,
			String currencyType, KAKAO_PAYMENT_PARAM_OS kakaoPaymentOs,
			String ccode, int buyNo) throws Exception {
		DefaultHttpClient httpclient = null;
		String responseJson = null;
		String uriString = null;
		try {
			httpclient = new DefaultHttpClient();
			HttpParams params = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id",
					DebugOption.KAKAO_CLINET_ID));
			nameValuePairs.add(new BasicNameValuePair("secret_key",
					DebugOption.KAKAO_SERVER_TO_SERVER_SECRET_KEY));
			nameValuePairs
					.add(new BasicNameValuePair("service_user_id", appId));
			nameValuePairs.add(new BasicNameValuePair("platform",
					kakaoPaymentPlatform.name()));
			nameValuePairs.add(new BasicNameValuePair("price", String
					.valueOf(currency)));
			nameValuePairs
					.add(new BasicNameValuePair("currency", currencyType));
			nameValuePairs.add(new BasicNameValuePair("os", kakaoPaymentOs
					.name()));
			// nameValuePairs.add(new BasicNameValuePair("country_iso", ccode));
			nameValuePairs.add(new BasicNameValuePair("buy_no", String
					.valueOf(buyNo)));

			HttpPost httppost = new HttpPost(DebugOption.KAKAO_PAYMENT_URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			uriString = DebugOption.KAKAO_PAYMENT_URL;
			boolean isFirstParam = true;
			for (NameValuePair nameValuePair : nameValuePairs) {
				uriString += isFirstParam ? "?" : "&" + nameValuePair.getName()
						+ "=" + nameValuePair.getValue();
				isFirstParam = false;
			}

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			responseJson = EntityUtils.toString(entity);
			EntityUtils.consume(entity);

			logger.info(uriString + " - " + responseJson);

			KakaoPayment kakaoPayment = parseJson(KakaoPayment.class,
					responseJson);

			if (kakaoPayment.getStatus() != 0) {
				loggingService.insertCurrencyErr(dateUtil, appId,
						"Fail KakaoPayment," + " response[" + responseJson
								+ "], uri[" + uriString + "]");
			}

		} catch (Exception e) {
			loggingService.insertCurrencyErr(dateUtil, appId,
					"Fail KakaoPayment, e[" + e.getMessage() + "],"
							+ " response[" + responseJson + "], uri["
							+ uriString + "]");
		} finally {
			if (httpclient != null && httpclient.getConnectionManager() != null) {
				httpclient.getConnectionManager().shutdown();
			}
		}

	}

	@Override
	@Transactional
	public boolean buyAnysdkItem(Account account, Shop anysdkItem,
			CurrencyGameLog currencyGameLog, Anysdkpay anysdkPay,
			boolean availableGP) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		final int itemCnt = 1;
		final ITEM_TYPE itemType = DebugOption.getItemType(anysdkItem
				.getItemId());
		if (itemType != ITEM_TYPE.BALL)
			throw new RuntimeException("AppleProdItem must be for ball");
		shopMapper.insertStoreReceipt(account.getAppId(),
				anysdkPay.getOrder_id(), dateUtil.getNowEpoch());

		int pay_status = anysdkPay.getPay_status();
		if (1 != pay_status) {
			return false;
		}
		Anysdkpay selectAnysdkpayByorderid = anysdkpayMapper
				.selectAnysdkpayByorderid(anysdkPay.getOrder_id());
		if (null != selectAnysdkpayByorderid) {
			return false;
		}
		anysdkpayMapper.insertAnysdkpay(anysdkPay);

		final int addCnt = anysdkItem.getCnt() * itemCnt;
		addBall(account, addCnt);
		currencyGameLog.setStore(STORE_TYPE.ANYSDK);
		currencyGameLog.setItemId(anysdkItem.getItemId());
		currencyGameLog.setItemCnt(itemCnt);
		currencyGameLog.setNowGold(account.getGold());
		currencyGameLog.setNowPunch(account.getPunch());
		currencyGameLog.setAddBall(currencyGameLog.getAddBall() + addCnt);
		currencyGameLog.setNowBall(account.getBall());

		accountMapper.updateAccountItem(account);

		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_CURRENCY_BALL * addCnt;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}

		return true;
	}
}
