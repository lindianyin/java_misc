package com.nfl.kfb.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



public class DebugOption {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DebugOption.class);
	public static final boolean ENABLE_EXCEPTION_MESSAGE = true;
	
	public static final int WEB_ROOT_PATH = 1;
	public static final String PRIVATEKEY = "yidengdashi";
	
	public static final Random gRand = new Random();
	
	public static final boolean ENABLE_LIMIT_COMMENT_TIME = false;
	
	public static  String getSessionKey(Object ...  param){
		StringBuffer sb = new StringBuffer();
		for(Object p : param){
			sb.append(DebugOption.PRIVATEKEY);
			sb.append(p);
		}
		String encodeMD5 = DebugOption.encodeMD5(sb.toString());
		return encodeMD5;
	}
	
	
	public static synchronized boolean checkParameter(String sessionKey,Object ... param){
		StringBuffer sb = new StringBuffer();
		for(Object p : param){
			sb.append(DebugOption.PRIVATEKEY);
			sb.append(p);
		}
		String encodeMD5 = DebugOption.encodeMD5(sb.toString());
		if(!encodeMD5.equals(sessionKey)){
			return false;
		}
		return true;
	}
	
	
	
	public static synchronized String encodeMD5(String plainText){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.reset();
		md.update(plainText.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		String encoded = sb.toString();
		return encoded.toUpperCase();
	}
	
	
	
	public static String getWebInfoPath(String subPath) {
		String path = Thread.currentThread().getContextClassLoader()
				.getResource("").toString();
		path = path.replace('/', '\\');
		path = path.replace("file:", "");
		path = path.replace("classes\\", "");
		path = path.substring(1);
		path += subPath;
		return path;
	}
	
	public static String getWebRootPath() {
		String path = Thread.currentThread().getContextClassLoader()
				.getResource("").toString();
		path = path.replace('/', '\\');
		path = path.replace("file:", "");
		path = path.replace("classes\\", "");
		path = path.substring(1);
		path = path.replace("WEB-INF\\", "");
		//path += subPath;
		return path;
	}
	
	public static final int  APP_TYPE =  1;
	public static final int  GAME_TYPE = 2;

	
	
	public static final HashMap<Integer, String[]> CATEGORY = new HashMap<Integer, String[]>();
	
	public enum GAME_STATUS{
		READY,
		RUNNING,
		ANSWER,
		OVER
	}

	public enum CAMP_STATUS{
		READY(0),//准备
		STOP(2),//停止答题
		OVER(4),//比赛结束
		FIRST_RUNNING(8),//第一阶段奔跑
		SECOND_RUNNING(16),//第二阶段奔跑
		ELIMINATE(32)//淘汰
		;
		private final int value;

		private CAMP_STATUS(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final CAMP_STATUS valueOf(int value) {
			for (CAMP_STATUS v : CAMP_STATUS.values()) {
				if (v.getValue() == value)
					return v;
			}
			return null;
		}
	}

	
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
	
	public static final int	ROAD_SIZE = 13;
	
	
}
