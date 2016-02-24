package com.nfl.kfb.uc;

/**
 * 
 * 配置文件辅助类。
 *
 */
public class ConfigHelper {

	/**
	 * sdk server域名+端口。
	 * @return
	 */
	public static String getServerHost(){
		return Config.get("serverHost");
	}
	/**
	 * 分配给游戏合作商的接入密钥,请做好安全保密
	 */
	public static String getApiKey(){
		return Config.get("apiKey");
	}
	/**
	 * 游戏编号
	 */
	public static Integer getGameId(){
		return Config.getInt("gameId");
	}
	
	
	
	/**
	 * 根据接口名称获取访问路径
	 * @param 接口名称
	 * @return 访问路径
	 */
	public static String getServiceUrl(String service){
		return Config.get(service+"_url","ss");
	}
	
	/**
	 * 
	 */
	public static Long getCheckTime(){
		return Config.getLong("checkTime",120);
	}
	/**
	 * 
	 */
	public static Integer getConnectTimeOut(){
		return Config.getInt("connectTimeOut",5000);
	}
	/**
	 * 
	 */
	public static Integer getSocketTimeOut(){
		return Config.getInt("socketTimeOut",5000) ;
	}
	
	/**
	 * 
	 */
	public static Integer getIntervalTime(){
		return Config.getInt("intervalTime",24);
	}
	
}
