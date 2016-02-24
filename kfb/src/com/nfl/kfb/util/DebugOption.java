/**
 * 
 */
package com.nfl.kfb.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.inven.Inven;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 * 
 */
public class DebugOption {

	private static final Logger logger = LoggerFactory
			.getLogger(DebugOption.class);

	public static final int MONTH_CARD_BALL = 10;

	/**
	 * JsonResponse의 "EXCEPTION" 필드를 나타냄. 실제 서비스에서는 stackTrace를 숨겨야하기에<br>
	 */
	public static final boolean ENABLE_EXCEPTION_MESSAGE = false;

	public static final boolean ENABLE_EXCEPTION_DB = false;

	/**
	 * MD5 디버그용값을 enable<br>
	 */
	public static final boolean ENABLE_DEBUG_MD5 = true;

	/**
	 * 세션키 디버그용값을 enable<br>
	 */
	public static final boolean ENABLE_DEBUG_SESSION_KEY = true;

	public static String REDIS_URL = null;

	public static synchronized String encodeMD5(String plainText)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		md.update(plainText.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		String encoded = sb.toString();

		logger.debug("MD5({}) => {}", plainText, encoded);

		return encoded;
	}

	/*************************************************************/
	// REWARD 보상
	/*************************************************************/
	public static final int REWARD_ITEM_BALL = 100; // 여의주
	public static final int REWARD_ITEM_GOLD = 200; // 엽전
	public static final int REWARD_ITEM_PUNCH = 300; // 주먹

	/*************************************************************/
	// ACCOUNT
	/*************************************************************/
	/**
	 * 디버그 로그인시에 SEC DEBUG값 enable<br>
	 */
	public static final boolean ENABLE_DEBUG_LOGIN_MD5 = ENABLE_DEBUG_MD5 && true;
	/**
	 * 디버그 로그인시에 SEC DEBUG값<br>
	 */
	public static final String DEBUG_LOGIN_MD5_STRING = "SKIP_LOGIN_MD5_1871";

	/**
	 * 디버그용 세션키<br>
	 */
	public static final int DEBUG_SESSION_KEY = 93677340;

	public static boolean isValidLoginMD5(String MD5, String appId, int nowEpoch)
			throws NoSuchAlgorithmException {
		// 디버그용 SEC값. 언제나 true
		if (ENABLE_DEBUG_LOGIN_MD5 && DEBUG_LOGIN_MD5_STRING.equals(MD5)) {
			return true;
		}

		// MD5 체크, MD5(id + "_goodluck_" + time + "_gg")
		String encodedMD5 = encodeMD5(appId + "_goodluck_" + nowEpoch + "_gg");
		return MD5.equals(encodedMD5);
	}

	/**
	 * 인벤으로 기본 지급될 아이템 - 초기아이템<br>
	 * ACCOUNT가 아닌 INVEN에 insert 쿼리로 들어갈 것임<br>
	 * 캐릭터ID, 아이템ID, 레벨, 개수 순서<br>
	 */
	public static final int[][] DEFAULT_INVEN_ITEM = {
			{ Account.DEFAULT_CH_ID, Account.DEFAULT_CH_ID,
					Account.DEFAULT_CH_LV, 1 } // 아뵤
			,
			{ Inven.CHARACTER_ID_FOR_COMMON_ITEM, Account.DEFAULT_PET_ID, 1, 1 } // 근두운
	};

	/**
	 * 계정 생성시 기본 엽전
	 */
	public static final int DEFAULT_GOLD = 0;

	/**
	 * 게정 생성시 기본 여의주
	 */
	public static final int DEFAULT_BALL = 0;
	// 新手引导奖励
	public static final int[] TUTORIAL_REWARD = { REWARD_ITEM_GOLD, 3000 * 6 };

	public static final int[] REVIEW_REWARD = { REWARD_ITEM_BALL, 5 * 10 };

	/**
	 * 디버그 탈퇴시에 SEC DEBUG값 enable<br>
	 */
	public static final boolean ENABLE_DEBUG_WITHDRAW_MD5 = ENABLE_DEBUG_MD5 && true;
	/**
	 * 디버그 탈퇴시에 SEC DEBUG값<br>
	 */
	public static final String DEBUG_WITHDRAW_MD5_STRING = "SKIP_WITHDRAW_MD5_7002";

	public static boolean isValidWithdrawMD5(String MD5, String appId,
			int nowEpoch, int sessionKey) throws NoSuchAlgorithmException {
		// 디버그용 SEC값. 언제나 true
		if (ENABLE_DEBUG_WITHDRAW_MD5 && DEBUG_WITHDRAW_MD5_STRING.equals(MD5)) {
			return true;
		}

		// MD5 체크, MD5(id + "_ur_" + time + "_hero" + sessionKey)
		String encodedMD5 = encodeMD5(appId + "_ur_" + nowEpoch + "_hero"
				+ sessionKey);
		return MD5.equals(encodedMD5);
	}

	/*************************************************************/
	// WEEK
	/*************************************************************/
	/**
	 * WEEK(몇주차)가 시작되는 기준일<br>
	 * 2013. 6. 17<br>
	 * 즉, 2013/6/17부터 0주차<br>
	 * 2013/6/24부터 1주차 ..<br>
	 */
	public static final long WEEK_START_TIMESTAMP = 1371394800000L;
	public static final int WEEK_START_EPOCH = (int) (WEEK_START_TIMESTAMP / 1000);

	/**
	 * WEEK를 수정하고 싶을때<br>
	 * WEEK_START_TIMESTAMP 기준으로 WEEK를 계산후 offset값을 더함<br>
	 * 테스트서버에서 다음주 데이터를 확인할때의 용도?<br>
	 */
	public static final int WEEK_OFFSET = 0;

	/**
	 * WEEK 데이터가 없을경우 기본지정된 데이터를 rotation해서 사용한다<br>
	 * 그것에 해당하는 rotation 데이터의 week 값<br>
	 */
	public static final int[] WEEK_MS_ROATATION_WEEK = { 0, -1, -2, -3, -4, -5,
			-6, -7 };
	public static final int[] WEEK_ACH_ROATATION_WEEK = { 0, -1, -2, -3, -4,
			-5, -6, -7, -8, -9, -10, -11, -12, -13, -14 };

	/*************************************************************/
	// ITEM
	/*************************************************************/
	public enum ITEM_TYPE {
		BALL, GOLD, PUNCH, SKILL, EQUIP, CONSUME, RESURRECT, CHARACTER, PET;
	}

	public static final ITEM_TYPE getItemType(int itemId) {
		int hundred = itemId / 100;
		switch (hundred) {
		case 1:
			// case 100: // 여의주1
			// case 101: // 여의주10
			// case 102: // 여의주33
			// case 103: // 여의주60
			// case 104: // 여의주130
			// case 105: // 여의주420
			// case 106: // 여의주750
			return ITEM_TYPE.BALL;

		case 2:
			// case 200: // 엽전1
			// case 201: // 엽전2000
			// case 202: // 엽전4800
			// case 203: // 엽전11200
			// case 204: // 엽전19200
			// case 205: // 엽전36000
			// case 206: // 엽전80000
			return ITEM_TYPE.GOLD;

		case 3:
			// case 300: // 주먹1
			// case 301: // 주먹5
			// case 302: // 주먹12
			// case 303: // 주먹28
			// case 304: // 주먹48
			// case 305: // 주먹90
			// case 306: // 주먹200
			return ITEM_TYPE.PUNCH;

		case 4:
			// case 400: // 광권법
			// case 401: // 소환공
			// case 402: // 한덩치
			// case 403: // 염장파
			return ITEM_TYPE.SKILL;

		case 5:
			// case 500: // 부리가면
			// case 501: // 꼬리장식
			// case 502: // 수련장갑
			// case 503: // 수련신발
			if (itemId < 510) {
				return ITEM_TYPE.EQUIP;
			}
			// case 599: // 부활하기
			if (itemId == 599) {
				return ITEM_TYPE.RESURRECT;
			}

			// case 510: // 조공수
			// case 511: // 기회환
			// case 512: // 절반서
			// case 513: // 막판 근두운
			// case 514: // 이어서
			// case 515: // 다막아
			// case 560: // 조공수2
			// case 561: // 기회환2
			// case 562: // 절반서2
			// case 563: // 막판 근두운2
			// case 564: // 이어서2
			// case 565: // 다막아2

			return ITEM_TYPE.CONSUME;

		case 6:
			// case 600: // 아뵤
			// case 601: // 뽀까
			// case 602: // 아려
			return ITEM_TYPE.CHARACTER;

		case 7:
			// case 700: // 근두운
			// case 701: // 학
			// case 702: // 자라
			// case 703: // 호랑이
			// case 704: // 악어왕
			// case 705: // 용
			return ITEM_TYPE.PET;

		default:
			throw new RuntimeException("Define ITEM_TYPE for itemId:" + itemId);
		}
	}

	/*************************************************************/
	// PLAY
	/*************************************************************/
	/**
	 * 게임끝 API에서의 PK MD5 디버그값 enable<br>
	 */
	public static final boolean ENABLE_DEBUG_FINISH_GAME_MD5 = ENABLE_DEBUG_MD5 && true;

	/**
	 * 게임끝 API에서의 PK MD5 디버그값<br>
	 */
	public static final String DEBUG_FINISH_MD5_STRING = "SKIP_FINISH_GAME_MD5_9099";

	public static boolean isValidFinishGameMD5(String MD5, String appId,
			int playKey) throws NoSuchAlgorithmException {
		// 디버그용 SEC값. 언제나 true
		return true;
		// if (ENABLE_DEBUG_FINISH_GAME_MD5 &&
		// DEBUG_FINISH_MD5_STRING.equals(MD5)) {
		// return true;
		// }
		//
		// // MD5 체크, MD5(id +"_dae_" + playKey +"_bak")
		// String encodedMD5 = encodeMD5(appId +"_dae_" + playKey +"_bak");
		// return MD5.equals( encodedMD5 );
	}

	public static final int gold_rate = 6;
	public static final int ball_rate = 10;
	/**
	 * 관문별 별 개수에 따른 {기준점수, 보상아이템ID, 개수}<br>
	 * GATE_STAR_REWARD[관문번호][별개수]<br>
	 * GATE_STAR_REWARD[关卡号码]
	 */
	public static final int[][][] GATE_STAR_REWARD = {
			// 별1개{기준점수, 보상아이템ID, 보상개수}, 별2개{}, 별3개{}
			{ { 1000, 0, 0 }, { 2000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 4000, REWARD_ITEM_GOLD, 200 * gold_rate } } // 0 -> 1관문
			,
			{ { 1500, 0, 0 }, { 2500, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 6000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 2000, 0, 0 }, { 4000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 8000, REWARD_ITEM_GOLD, 200 * gold_rate } },

			{ { 3500, 0, 0 }, { 7000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 14000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 4000, 0, 0 }, { 8000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 16000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 4500, 0, 0 }, { 9000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 18000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 5000, 0, 0 }, { 10000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 20000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 5500, 0, 0 }, { 11000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 22000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 6000, 0, 0 }, { 12000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 24000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 6500, 0, 0 }, { 13000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 26000, REWARD_ITEM_BALL, 1 * ball_rate } } // 9 -> 10관문
			,
			{ { 7000, 0, 0 }, { 14000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 28000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 7500, 0, 0 }, { 15000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 30000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 8000, 0, 0 }, { 16000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 32000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 8500, 0, 0 }, { 17000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 34000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 9000, 0, 0 }, { 18000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 36000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 9500, 0, 0 }, { 19000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 38000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 10000, 0, 0 }, { 20000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 40000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 10500, 0, 0 }, { 21000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 42000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 11000, 0, 0 }, { 22000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 44000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 11500, 0, 0 }, { 23000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 46000, REWARD_ITEM_BALL, 1 * ball_rate } },
			{ { 12000, 0, 0 }, { 24000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 48000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 12500, 0, 0 }, { 25000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 50000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 13000, 0, 0 }, { 26000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 52000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 13500, 0, 0 }, { 27000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 54000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 14000, 0, 0 }, { 28000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 56000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 14500, 0, 0 }, { 29000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 58000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 15000, 0, 0 }, { 30000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 60000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 15500, 0, 0 }, { 31000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 62000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 16000, 0, 0 }, { 32000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 64000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 16500, 0, 0 }, { 33000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 66000, REWARD_ITEM_BALL, 2 * ball_rate } },
			{ { 17000, 0, 0 }, { 34000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 68000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 17500, 0, 0 }, { 35000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 70000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 18000, 0, 0 }, { 36000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 72000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 18500, 0, 0 }, { 37000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 74000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 19000, 0, 0 }, { 38000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 76000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 19500, 0, 0 }, { 39000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 78000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 20000, 0, 0 }, { 40000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 80000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 20500, 0, 0 }, { 41000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 82000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 21000, 0, 0 }, { 42000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 84000, REWARD_ITEM_GOLD, 200 * gold_rate } },
			{ { 21500, 0, 0 }, { 43000, REWARD_ITEM_GOLD, 150 * gold_rate },
					{ 86000, REWARD_ITEM_BALL, 2 * ball_rate } } };

	public static final int LAST_GATE = 39; // 40번째 관문에 도착하면 보상

	private static final int[][] GATE_ALL_CLEAR_REWARD = {
			// 보상타입, 보상값
			{ REWARD_ITEM_GOLD, 7000 * gold_rate} // 1등
			, { REWARD_ITEM_GOLD, 5000 * gold_rate } // 2등
			, { REWARD_ITEM_GOLD, 3000 * gold_rate } // 3등
			, { REWARD_ITEM_GOLD, 1000 * gold_rate} // 4등 ~ 99등
			, { REWARD_ITEM_GOLD, 1000 * gold_rate } // 100등 이하
	};

	public static final int[] getGateAllClearReward(int rank) {
		if (rank == 0) {
			return GATE_ALL_CLEAR_REWARD[0];
		} else if (rank == 1) {
			return GATE_ALL_CLEAR_REWARD[1];
		} else if (rank == 2) {
			return GATE_ALL_CLEAR_REWARD[2];
		} else if (rank < 99) {
			return GATE_ALL_CLEAR_REWARD[3];
		} else {
			return GATE_ALL_CLEAR_REWARD[4];
		}
	}

	/*************************************************************/
	// SHOP
	/*************************************************************/
	public static final int SHOP_PRICE_DOLLAR = 1000; // 현금 美元
	public static final int SHOP_PRICE_BALL = 100; // 여의주如意珠
	public static final int SHOP_PRICE_GOLD = 200; // 엽전 金币
	public static final int SHOP_PRICE_PUNCH = 300; // 주먹 拳
	public static final int SHOP_PRICE_NOT_SALE = 0; // 상점에서 판매하지 않음 在商店没有卖

	public static final int MAX_CHARACTER_LV = 20;
	public static final int MAX_SKILL_LV = 20;
	public static final int MAX_EQUIP_LV = 20;
	public static final int MAX_PET_LV = 10;

	/*************************************************************/
	// KAKAO
	/*************************************************************/
	public static final String KAKAO_CLINET_ID = "90038672305671633";

	public static final String KAKAO_SERVER_TO_SERVER_SECRET_KEY = "c5a953708f3af33bf28102dbd94aa022835653e5b820b7b7e06c284a5f89f4d5";

	public static final String KAKAO_PAYMENT_URL = "https://gameapi.kakao.com/payment_v1/payments.json";

	public enum KAKAO_PAYMENT_PARAM_PALTFORM {
		google, apple, tstore, uplus
	}

	public enum KAKAO_PAYMENT_PARAM_OS {
		android, apple
	}

	/*************************************************************/
	// ATTENDANCE 출석 考勤 签到奖励
	/*************************************************************/
	public static final int[][] ATTENDANCE_REWARD = {
			// 보상타입, 보상값 奖励类型，奖励价值
			{ 510, 1 } // 1일차. 조공수 1개
			, { 512, 2 } // 2일차. 절반서 2개
			, { 513, 3 } // 3일차. 막판근두운 3개
			, { REWARD_ITEM_GOLD, 3000 * gold_rate } // 4일차
			, { REWARD_ITEM_GOLD, 3500* gold_rate } // 5일차
			, { REWARD_ITEM_GOLD, 4000* gold_rate } // 6일차
			, { REWARD_ITEM_BALL, 10 * ball_rate } // 7일차
	};

	/*************************************************************/
	// RANK 랭킹 排行 周排行奖励
	/*************************************************************/
	public static final int[][] LAST_WEEK_RANK_REWARD = {
			// 보상타입, 보상값
			{ REWARD_ITEM_BALL, 7 *ball_rate} // 1등
			, { REWARD_ITEM_BALL, 5 *ball_rate } // 2등
			, { REWARD_ITEM_BALL, 3 *ball_rate } // 3등
			, { REWARD_ITEM_GOLD, 1000 * gold_rate} // 그외
	};

	public static final int[] getLastWeekRankReward(int rank) {
		if (rank == 0) {
			return LAST_WEEK_RANK_REWARD[0];
		} else if (rank == 1) {
			return LAST_WEEK_RANK_REWARD[1];
		} else if (rank == 2) {
			return LAST_WEEK_RANK_REWARD[2];
		} else {
			return LAST_WEEK_RANK_REWARD[3];
		}
	}

	/*************************************************************/
	// FRIEND 친구
	/*************************************************************/
	/**
	 * 친구를 재초대 할 수 있는 시간. 31일<br>
	 */
	public static final int INV_COOL_TIME_EPOCH = DateUtil.ONE_DAY_EPOCH * 31;
	// public static final int INV_COOL_TIME_EPOCH = 60; // 디버그 1분

	/**
	 * 24시간내에 초대할 수 있는 제한 수<br>
	 * 클라에서 컨트롤<br>
	 */
	public static final int INV_LIMIT_ON_DAY = 30;

	public static final boolean ENABLE_DEBUG_INV_FRIEND_MD5 = ENABLE_DEBUG_MD5 && true;
	public static final String DEBUG_INV_FRIEND_MD5_STRING = "SKIP_FR_INV_MD5_1128"; // 디버그용
																						// MD5

	/**
	 * 친구 초대시 MD5 체크<br>
	 * 
	 * @param iNV
	 * @param iD
	 * @param sK
	 * @param invCnt
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean isValidFriendInvMD5(String MD5, String appId,
			int sessionKey, int invCnt) throws NoSuchAlgorithmException {
		// 디버그용 SEC값. 언제나 true
		if (ENABLE_DEBUG_INV_FRIEND_MD5
				&& DEBUG_INV_FRIEND_MD5_STRING.equals(MD5)) {
			return true;
		}

		// MD5 체크, MD5(ID+"_IDD_"+SK+"_372Q")
		String encodedMD5 = encodeMD5(appId + "_IDD_" + sessionKey + "_372Q"
				+ invCnt);
		return MD5.equals(encodedMD5);
	}

	/**
	 * 초대할때마다 기본적으로 주는 보상<br>
	 * 클라에 고정되어있기때문에 이 값은 수정하지 말 것<br>
	 */
	public static final int[] INV_DEFAULT_REWARD = { REWARD_ITEM_PUNCH, 1 };

	private static final int[][] INV_REWARD = {
			// RIDX, 초대수, 보상아이템, 개수
			{ 0, 10, 512, 5 } // 이어하기 아이콘 x5개
			, { 1, 30, 513, 5 } // 엽전 아이콘 3000개
			, { 2, 50, 514, 5 } // 여의주 아이콘 5개
			, { 3, 70, REWARD_ITEM_GOLD, 20000 } // 소환수 아이콘 학
			, { 4, 90, REWARD_ITEM_BALL, 30 } // 소환수 아이콘 학
			, { 5, 150, REWARD_ITEM_BALL, 100 } // 소환수 아이콘 학
	};

	/**
	 * 초대수를 서버에서 수정 가능하도록 초대명수 정보를 로그인시에 클라에 내려줌. 초대명수 정보 String<br>
	 */
	public static final String INV_CNT_INFO;
	static {
		String invCntInfo = null;
		for (int[] invReward : INV_REWARD) {
			final int reqCnt = invReward[1];
			if (invCntInfo == null) {
				invCntInfo = String.valueOf(reqCnt);
			} else {
				invCntInfo += "," + String.valueOf(reqCnt);
			}
		}
		INV_CNT_INFO = invCntInfo;
	}

	public static final int[] getInvReward(int invCnt) {
		for (int[] invReward : INV_REWARD) {
			final int reqCnt = invReward[1];
			if (reqCnt == invCnt)
				return invReward;
		}
		return null;
	}

	public static final String INV_ITEM_INFO;
	static {
		String invItemInfo = null;
		for (int[] invReward : INV_REWARD) {
			final int invItemInfoItemId = invReward[2];
			final int invItemInfoItemCnt = invReward[3];
			if (invItemInfo == null) {
				invItemInfo = String.valueOf(invItemInfoItemId) + ","
						+ String.valueOf(invItemInfoItemCnt);
			} else {
				invItemInfo += "," + String.valueOf(invItemInfoItemId) + ","
						+ String.valueOf(invItemInfoItemCnt);
			}
		}
		INV_ITEM_INFO = invItemInfo;
	}

	/*************************************************************/
	// PUNCH 주먹
	/*************************************************************/
	/**
	 * 주먹 선물 쿨타임
	 */
	public static final int SEND_PUNCH_COOL_TIME_EPOCH = DateUtil.ONE_HOUR_EPOCH + 1800; // 1시간

	/**
	 * 주먹 선물 메일 보관시간
	 */
	public static final int MAIL_PUNCH_KEEP_EPOCH = DateUtil.ONE_DAY_EPOCH * 3;

	/**
	 * 현재 주먹개수마다 리젠되는 시간
	 */
	// public static final int[] PUNCH_REGEN_TIME_EPOCH = { 2 * 60, 3 * 60,
	// 4 * 60, 5 * 60, 6 * 60 };
	public static final int[] PUNCH_REGEN_TIME_EPOCH = { 5 * 60, 5 * 60,
			5 * 60, 5 * 60, 5 * 60 };
	// public static final int[] PUNCH_REGEN_TIME_EPOCH = {10, 10, 10, 10, 10};
	// // 테스트용

	/**
	 * 리젠되는 주먹 최대값
	 */
	public static final int PUNCH_REGEN_MAX = PUNCH_REGEN_TIME_EPOCH.length;

	/**
	 * 로그인시에도 리젠주먹을 계산할 것인가
	 */
	public static final boolean PUNCH_REGEN_ON_LOGIN = true;

	/*************************************************************/
	// MAIL
	/*************************************************************/
	/**
	 * sql selectList 할때 limit 수<br>
	 */
	public static final int MAIL_BOX_SQL_LIMIT = 50;

	/*************************************************************/
	// 구입, 결제
	/*************************************************************/
	public enum STORE_TYPE {
		APPLE(0), GOOGLE(1), ANYSDK(2);
		private final int value;

		private STORE_TYPE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final STORE_TYPE valueOf(int value) {
			for (STORE_TYPE storeType : STORE_TYPE.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
	}

	private static final String GOOGLE_STORE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmbxcSQzHf6U4zk1bAw7XVOauNbizVqTTRj7e7tpqy0Kss17F/AfVY4sD9qsttr3lrwWkxjZWSbYQ2vQXRDeb3ldlROq23QSxJYnYyVzzBYQ180INhQXE6I7/HXUOtApMOxwVVbdNlc6Q1KLzKs1R0tDg3Z3zbyo/KNKvOR5fwx6KMN7tqrKZpO57WcW2Xcd6kTuXq/AMFWOhqyZed5sDNkb5uKBkrJ6YcbwbTYLIt0+mvA9J/swZnfmfc7dG6QDdW3IuDgMRzyHbNDaHyQl01t5b9xOtO4VNAIJk8pDH9qM14g16b266X8VY5ak2u8nJF9xdof04/+oel8jjlTKz1QIDAQAB";

	/**
	 * 구글 영수증 복호화 verify<br>
	 * 
	 * @param purchase
	 * @param base64Signature
	 * @return
	 * @throws Exception
	 */
	public static synchronized boolean verfifyGoogleReceiptSignature(
			String purchase, String base64Signature) throws Exception {
		Base64 base64 = new Base64();

		byte[] publicKeyBytes = base64
				.decode(DebugOption.GOOGLE_STORE_PUBLIC_KEY);
		byte[] signatureBytes = base64.decode(base64Signature);

		// generate public key
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

		// signature
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(purchase.getBytes());

		return signature.verify(signatureBytes);
	}

	/*************************************************************/
	// Gamble
	/*************************************************************/
	/**
	 * 겜블에서 선택되지 않은 아이템 개수
	 */
	public static final int GAMBLE_MISSED_CNT = 2;

	/**
	 * 겜블 한번 할때 필요한 양
	 */
	public static final int GAMBLE_POINT_TO_OPEN = 100;

	public static final int GAMBLE_POINT_ATTENDANCE = 5; // 출석
	public static final int GAMBLE_POINT_START_GAME = 5; // 게임시작
	public static final int GAMBLE_POINT_SEND_PUNCH = 3; // 주먹보내기
	public static final int GAMBLE_POINT_INV = 10; // 초대하기
	public static final int GAMBLE_POINT_BOAST = 5; // 자랑하기
	public static final int GAMBLE_POINT_BUY_ITEM = 5; // 아이템구입
	public static final int GAMBLE_POINT_CURRENCY_BALL = 5; // 여의주구입 - 여의주 개수당
															// 포인트
	public static final int[] GAMBLE_POINT_MISSION_STAR = { 3, 6, 9 }; // 별 개수당
																		// 포인트
	public static final int GAMBLE_POINT_START_DUNGEON = 10; // 던전시작

	/*************************************************************/
	// Dungeon
	/*************************************************************/
	public static final int DUNGEON_GATE_NUMBER = -1;

	/*************************************************************/
	// Exception
	/*************************************************************/
	/**
	 * ExceptionHandler 가 에러를 받았을때, JsonResponse에 errorMsg를 포함할 것인가<br>
	 */
	public static final boolean ENABLE_EXCEPTION_HANDLER_ERRMSG = false;

	/**************** 任务的类型 ***************/
	/*
	 * 1、完成首充 2、升级召唤兽 3、购买装备 4、升级装备 5、购买技能 6、购买角色 7、升级角色 8、星级通关 9、加好友 10、攻打墙壁
	 * 11、攻打怪物（累计） 12、轻功时间（累计） 13、游戏内超越玩家个数（累计） 14、连续跳跃次数（累计） 15、瞬间移动次数（累计）
	 * 16、天下无敌次数（累计） 17、商店购买道具 18、游戏内累计获得铜钱 19、攻打福袋次数 20、连续攻击次数 21.炫耀一下 22.购买铜钱
	 * 23.都来玩呀 24.勇闯魔窟 25.不死凤凰 26、穿越道路（累计） 27、获得分数（累计） 28、击碎铁锤（累计） 29、获得星星（累计）
	 * 30、获赠拳头（累计） 31、购买金钱次数（累计） 32、购买拳头次数（累计） 33、进行游戏次数（累计） 34、完成任务（累计）
	 * 35、使用技能（累计） 36、获得羽毛 37、获得磁石 38、获得饭团 39、完成成就 40、不使用穿越跑的分数(无尽)
	 * 41、充值任意金额（累计）42、不使用穿越跑的分数(无尽) 43 、单日单局距离500米 44、活动期间在无尽模式下不复活达到的最高分
	 * 45、单笔充值 46、无尽模式最高分
	 */

	public enum TASK_TYPE {
		FIRST_RECHARGE(1), UP_PET(2), BUY_EQUIP(3), UP_EQUIP(4), BUY_SKILL(5), BUY_ROLE(
				6), UP_ROLE(7), STAR_PASS(8), ADD_FRIEND(9), ATT_WALL(10), ATT_BOSS(
				11), LIGHT_TIME(12), BEYOND_COUNT(13), CONTINUE_JUMP(14), TELEPORT_COUNT(
				15), INVINCIBLE_COUNT(16), SHOP_BUY_ITEM(17), GET_ALL_GOLD(18), FIGHT_FU_BAG(
				19), CONTINUE_ATT_TIME(20), SHARE_SCORE(21), BUY_GOLD(22), INVIVE_FRIEND(
				23), GO_MO_KU(24), RELIVE(25), THROUGH_WAY(26), GET_POINT(27), CRUSH_HAMMER(
				28), GET_START(29), GET_REWARD_PUNCH(30), BUY_GOLD_TIMES(31), BUY_PUNCH_TIMES(
				32), PLAY_TIMES(33), FINISH_TASK_TIMES(34), USE_SKILL(35), GET_FEATHER(
				36), GET_MAGNET(37), GET_RICE(38), FINISH_ACHIEVE(39), UNUSE_GO_THROUGH_SCORE(
				40), RECHARGE(41), UNUSE_GO_THROUGH_SCORE_FIGHT(42), SINGLE_RUN_MAX(
				43), UNLIMIT_NOT_ALIVE_MAX_POINT(44), ONE_RECHARGE(45), MAX_UNLIMIT_POINT(
				46);
		private final int value;

		private TASK_TYPE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final TASK_TYPE valueOf(int value) {
			for (TASK_TYPE storeType : TASK_TYPE.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
	}

	public enum TASK_STATE {
		NOT_OVER(0), OVER(1);

		private final int value;

		private TASK_STATE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final TASK_STATE valueOf(int value) {
			for (TASK_STATE storeType : TASK_STATE.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
	}

	// 购买类型
	public static final String PAY_TYPE_COMMON = "Common";
	public static final String PAY_TYPE_MONTHCARD = "MonthCard";
	public static final String PAY_TYPE_GIFTS = "Gifts";

	public enum TASK_CATEGORY {
		ACHIEVE(1), DAILY_TASK(2), ACTIVITY(3);
		// DAILY_ACTIVITY(4);
		private final int value;

		private TASK_CATEGORY(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final TASK_CATEGORY valueOf(int value) {
			for (TASK_CATEGORY storeType : TASK_CATEGORY.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
	}

	// 奖励的类型
	public enum REWARD_TYPE {
		YESTERDAY_GLOBAL_REWARD(1), UNLIMIT_RANK_REWARD(2), EVENT_REWARD(3), GIFT_REWARD(
				4), GIFT_REWARD_UNGET(5);
		private final int value;

		private REWARD_TYPE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final REWARD_TYPE valueOf(int value) {
			for (REWARD_TYPE storeType : REWARD_TYPE.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
	}

	/*************************************************************/
	// AdPOPcorn
	/*************************************************************/
	public static final int ADPOPCORN_ITEM_MAIL_KEEP = DateUtil.ONE_DAY_EPOCH * 7; // 애드팝
																					// 아이템
																					// 메일
																					// 보관
																					// 시간
	public static final String ADPOPCORN_ITEM_MAIL_MSG = "무료 선물 여의주 도착";

	public static final String ADPOPCORN_HASHKEY_AND = "75fda48dfa7e454c"; // 애드팝콘
																			// 해쉬키
	public static final String ADPOPCORN_ITEMKEY_AND = "315525229"; // 애드팝콘 아이템키
	public static final int ADPOPCORN_ITEMID_AND = REWARD_ITEM_BALL;; // 지급될
																		// 아이템ID

	public static synchronized boolean isValidAdPOPcornHashKeyAndroid(
			String expectedHash, String queryString)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeyException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(
				DebugOption.ADPOPCORN_HASHKEY_AND.getBytes("UTF-8"), "hmacMD5");
		Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
		mac.init(secretKeySpec);

		byte[] hashBytes = mac.doFinal(queryString.getBytes("UTF-8"));
		String actualHash = Hex.encodeHexString(hashBytes);

		logger.debug(
				"ADPOPCORN_HASHKEY queryString[{}], expectedHash[{}], actualHash[{}]",
				queryString, expectedHash, actualHash);

		return actualHash.equals(expectedHash);
	}

	public static final String ADPOPCORN_HASHKEY_IOS = "ecde5bfec5fe4f04"; // 애드팝콘
																			// 해쉬키
	public static final String ADPOPCORN_ITEMKEY_IOS = "665822545"; // 애드팝콘 아이템키
	public static final int ADPOPCORN_ITEMID_IOS = REWARD_ITEM_BALL;

	public static final String CONFIG_TASK_APPID = "fe80::e:19d7:c59:ae95%12"; // 지급될
																				// 아이템ID

	public static synchronized boolean isValidAdPOPcornHashKeyIos(
			String expectedHash, String queryString)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeyException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(
				DebugOption.ADPOPCORN_HASHKEY_IOS.getBytes("UTF-8"), "hmacMD5");
		Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
		mac.init(secretKeySpec);

		byte[] hashBytes = mac.doFinal(queryString.getBytes("UTF-8"));
		String actualHash = Hex.encodeHexString(hashBytes);

		logger.debug(
				"ADPOPCORN_HASHKEY queryString[{}], expectedHash[{}], actualHash[{}]",
				queryString, expectedHash, actualHash);

		return actualHash.equals(expectedHash);
	}

	// AnySdk
	// public static final String ANYSDK_PRIVATE_KEY_OPTION_KEY =
	// "ANYSDK_PRIVATE_KEY";

	public static <T> T parseJson(Class<T> clazz, String jsonString) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			T returnClazz = objectMapper.readValue(jsonString, clazz);
			return returnClazz;
		} catch (Exception e) {
			logger.error(jsonString, e);
			return null;
		}
	}

	public static String toJson(Object obj) throws Exception {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			// ObjectWriter writerWithDefaultPrettyPrinter =
			// objectMapper.writerWithDefaultPrettyPrinter();
			objectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			String strJson = objectMapper.writeValueAsString(obj);
			return strJson;
		} catch (Exception e) {
			logger.error(obj.getClass().getName(), e);
			throw e;
		}
	}

	public static int getStarByPoint(int point, int gate) {
		int s1 = 2000 + gate * 500;
		int s2 = 4000 + gate * 1000;
		int s3 = 8000 + gate * 2000;
		if (point < s1) {
			return 0;
		} else if (s1 <= point && point < s2) {
			return 1;
		} else if (s2 <= point && point < s3) {
			return 2;
		} else {
			return 3;
		}
	}

	public static final String FINISH_GUILD = "FINISH_GUILD";// 结束引导

	public static final String MONTH_CARD_ID = "com.nfl.game.kr.kungfubird_07";
	
	public static final String BIG_GIFTS_ID = "com.nfl.game.kr.kungfubird_08";

	public static final int DEFAULT_GAMBLE_POINT = 100;

	public static final String REDIS_RANK_KEY = "rank";

	public static final String REDIS_UNLIMIT_RNANK_KEY = "unlimitrank";

	public static List<String> splitString(String src, String token) {
		StringTokenizer st = new StringTokenizer(src, token);
		ArrayList<String> arrList = new ArrayList<String>();
		while (st.hasMoreElements()) {
			String str = (String) st.nextElement();
			arrList.add(str);
		}
		return arrList;
	}

	private static JedisPool jedisPool = null;

	public static Date OPEN_SERVER_TIME = null;

	public static final int UNLIMIT_GATE = 9999;

	public static synchronized JedisPool getJedisPool() {
		if (jedisPool == null) {
			jedisPool = new JedisPool(DebugOption.REDIS_URL);
		}
		return jedisPool;
	}

	public static boolean isSameDay(Date date1, Date date2) {
		Date d1 = new Date(date1.getYear(), date1.getMonth(), date1.getDay());
		Date d2 = new Date(date2.getYear(), date2.getMonth(), date2.getDay());

		return d1.compareTo(d2) == 0;
	}

	public static AtomicLong maxArenaId = new AtomicLong(100000L);

	public static long getMaxArenaId() {
		return maxArenaId.incrementAndGet();
	}

	// [物品id，赢的奖励，消耗]
	public static final int[][] arenaReward = {
			{ REWARD_ITEM_GOLD, 1000, 388 }, { REWARD_ITEM_GOLD, 4000, 1888 },
			{ REWARD_ITEM_BALL, 4, 1 }, { REWARD_ITEM_BALL, 30, 18 } };

	public static ExecutorService poolExecuteTask = Executors
			.newFixedThreadPool(10);

	public static ExecutorService timerThread = Executors.newFixedThreadPool(1);

}
