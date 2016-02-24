/**
 * 
 */
package com.nfl.kfb.maintenance;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession;
import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession.DB_TARGET;
import com.nfl.kfb.maintenance.mapper.MaintenanceMapper;

/**
 * 게임끝 획득 엽전/여의주를 큰 값으로 치팅해서 돈을 불림.
 * 그런 애들 소유금액을 (-)로 만듬
 * @author KimSeongsu
 * @since 2013. 10. 15.
 *
 */
public class CheatingUsers {
	
	private static final Logger logger = LoggerFactory.getLogger(CheatingUsers.class);
	
	public static void main(String[] args) {
		
//		// prevent mistake launch
//		new CheatingUsers().changeGoldBallCheatingUser();
		
	}
	
	public void changeGoldBallCheatingUser() {
		SqlSession sqlSession = null;
		try {
			sqlSession = MaintenanceSqlSession.openSession(DB_TARGET.REAL);
			MaintenanceMapper maintenanceMapper = sqlSession.getMapper(MaintenanceMapper.class);
			List<String> cheatingAppIds = maintenanceMapper.selectCheatingAppId();
//			System.out.println("size = " + cheatingAppIds.size());
			logger.info("CHEATING_USERS SIZE[{}]", cheatingAppIds.size());
			
//			cheatingAppIds.clear();
//			cheatingAppIds.add("1000000012");
			
			for (String appId : cheatingAppIds) {
				maintenanceMapper.updateCheatingAppIdGoldBall(appId);
				logger.info("CHEATING_USERS APPID[{}]", appId);
				sqlSession.commit();
			}
			
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("CheatingUsers", e);
			}
		}
	}

}
