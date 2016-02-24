package com.nfl.kfb.uc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import org.apache.commons.lang.StringUtils;

/**
 * 
 * 配置文件操作类。
 *
 */
public class Config {

    private static Properties properties = null;
    
    static{
    	try {
    		InputStream in = Util.class.getResourceAsStream("/conf.Properties");
    		properties = new Properties();
	        properties.load(in);
		} catch (Exception e) {
			System.err.println("读取配置文件失败,"+e.toString());
		}
    }

    /**
     * 设置key=>value集合（方便单元测试的时候调用）。
     */
    public static void setConfig(Properties  pros) {
        properties = pros;
    }
    
    /**
     * 获取配置项的值。如果配置项不存在，将返回null
     * 
     * @param key 配置项名称
     * @return 配置项的值
     */
    public static String get(String key) {
        return StringUtils.trim(properties.getProperty(key));
    }

    /**
     * 获取配置项的值。如果配置项不存在，将返回指定的默认值
     * 
     * @param key 配置项名称
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        
        return (null == value ? defaultValue : value);
    }

    /**
     * 获取配置项的值。如果配置项不存在或无法转换成整型值，将返回null
     * 
     * @param key 配置项名称
     * @return 配置项的值
     */
    public static Integer getInt(String key) {
        String value = get(key);
        try {
            return new Integer(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取配置项的值。如果配置项不存在或无法转换成整型值，将返回指定的默认值
     * 
     * @param key 配置项名称
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public static Integer getInt(String key, int defaultValue) {
        Integer value = getInt(key);
        
        return (null == value ? defaultValue : value);
    }
    
    /**
     * 获取配置项的值。如果配置项不存在或无法转换成整型值，将返回null
     * 
     * @param key 配置项名称
     * @return 配置项的值
     */
    public static Long getLong(String key) {
        String value = get(key);
        try {
            return new Long(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取配置项的值。如果配置项不存在或无法转换成整型值，将返回指定的默认值
     * 
     * @param key 配置项名称
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public static Long getLong(String key, long defaultValue) {
        Long value = getLong(key);
        
        return (null == value ? defaultValue : value);
    }
    
    /**
     * 
     * 设置配置项数据。
     * @param key
     * @param value
     * @return
     */
    public static void set(String key, String value){
    	properties.setProperty(key, value);
    }
    /**
	 * 设置配置项数据,并保存到配置文件。
	 * 
	 * @param key
	 * @param value
	 */
	public static boolean set(String file, String key, String value) throws IOException {
		file = StringUtils.trim(file);
		if(StringUtils.isEmpty(file) || StringUtils.isEmpty(key)) return false;
		properties.setProperty(key, value);
		File f = new File(file);
		boolean res = false;
		if (f.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String outstr = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				if ("".equals(line)) { // 如果为空
					outstr += "\n";
					continue;
				}
				if (line.startsWith("#")) { // 如果为注释
					outstr += line + "\n";
					continue;
				}
				if (line.trim().startsWith(key)) {
					String[] keyandvalue = line.split("=");
					outstr += keyandvalue[0].toString() + "=" + value.toString() + "\n";
					res = true;
					continue;
				}
				outstr += line + "\n";
			}
			//System.out.println(outstr);
			if (res) {
				FileWriter fw = new FileWriter(file, false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(outstr);
				bw.close();
				fw.close();
				return true;
			}
		}
		return false;
	}
	
}
