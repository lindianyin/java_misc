/**
 * 
 */
package com.nfl.kfb.push.collector.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.push.collector.kfbPushDeviceCollector;
import com.nfl.kfb.push.dao.PushSqlSession;
import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice;
import com.nfl.kfb.push.mapper.PushMapper;
import com.nfl.kfb.push.sender.KfbPushSender;


/**
 * @author KimSeongsu
 * @since 2013. 10. 11.
 *
 */
public class CollectAllDevices implements kfbPushDeviceCollector {
	
	private static Logger logger = LoggerFactory.getLogger(KfbPushSender.class);
	
	@Override
	public List<PushDevice> collectPushDevices(DB_TARGET db) {
		SqlSession sqlSession = null;
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			
			List<PushDevice> allDevices = pushMapper.selectAllTokens();
			
//			sqlSession.commit();
			
			return allDevices;
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
				logger.error(this.toString(), e);
			}
		}
	}
	

}
