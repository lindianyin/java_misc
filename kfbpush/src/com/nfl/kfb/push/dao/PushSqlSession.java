/**
 * 
 */
package com.nfl.kfb.push.dao;

import java.io.FileInputStream;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KimSeongsu
 * @since 2013. 10. 11.
 *
 */
public class PushSqlSession {
	
	private static final Logger logger = LoggerFactory.getLogger(PushSqlSession.class);
	
	private static SqlSessionFactory sqlSessionFactory_test = null;
	private static SqlSessionFactory sqlSessionFactory_real = null;
	
	public enum DB_TARGET {
		TEST
		, REAL
	}
	
	public static synchronized SqlSession openSession(DB_TARGET db) {
		switch (db) {
		case TEST:
			return openTestSession();
			
		case REAL:
			return openRealSession();
			
		default:
			return null;
		}
	}
		
	private static SqlSession openTestSession() {
		if (sqlSessionFactory_test == null) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("./config/test_mybatis-config.xml");
				sqlSessionFactory_test = new SqlSessionFactoryBuilder().build(fis);
			} catch (Exception e) {
				logger.error("fail to build SqlSessionFactory", e);
			} finally {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error("fail to close FileInputStream", e);
				}
			}
		}
		
		return sqlSessionFactory_test.openSession();
	}

	private static SqlSession openRealSession() {
		if (sqlSessionFactory_real == null) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("./config/real_mybatis-config.xml");
				sqlSessionFactory_real = new SqlSessionFactoryBuilder().build(fis);
			} catch (Exception e) {
				logger.error("fail to build SqlSessionFactory", e);
			} finally {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error("fail to close FileInputStream", e);
				}
			}
		}
		
		return sqlSessionFactory_real.openSession();
	}

}
