package com.nfl.kfb.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DebugOption {
	
	private static final Logger logger = LoggerFactory.getLogger(DebugOption.class);
	public static final boolean ENABLE_EXCEPTION_MESSAGE = true;
	
	public static final int WEB_ROOT_PATH = 1;
	public static final String PRIVATEKEY = "yidengdashi";
	
	public static final Random gRand = new Random();
	
	public static final boolean ENABLE_LIMIT_COMMENT_TIME = false;//是否限制发表评论的时间(如果true 时间是10分钟)
	
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
	
	static{
		CATEGORY.put(APP_TYPE,  new String[]{"应用","app","http://cdn2.image.apk.gfan.com/asdf/PImages/2014/03/27/69e6e65c-75fa-45c5-b3bf-7247093a0c83.png","http://cdn2.image.apk.gfan.com/asdf/PImages/2014/03/27/ldpi_69e6e65c-75fa-45c5-b3bf-7247093a0c83.png"});
		CATEGORY.put(GAME_TYPE, new String[]{"游戏","game","http://cdn2.image.apk.gfan.com/asdf/PImages/2014/03/27/69e6e65c-75fa-45c5-b3bf-7247093a0c83.png","http://cdn2.image.apk.gfan.com/asdf/PImages/2014/03/27/ldpi_69e6e65c-75fa-45c5-b3bf-7247093a0c83.png"});
	}

	
	
}
