/**
 * 
 */
package com.nfl.kfb.maintenance;

import java.io.FileWriter;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession;
import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession.DB_TARGET;
import com.nfl.kfb.maintenance.mapper.MaintenanceMapper;

/**
 * @author KimSeongsu
 * @since 2013. 11. 26.
 *
 */
public class ExportFile {
	
	private static final Logger logger = LoggerFactory.getLogger(ExportFile.class);
	
	public static void main(String[] args) throws Exception {
		ExportFile exportFile = new ExportFile();
		
		exportFile.exportAllAppId("./doc/exportXls/2013-11-26 APPID.txt");
	}
	
	public void exportAllAppId(String fileName) throws Exception {
		logger.info("exportAllAppId. output=[{}]", fileName);
		
		List<String> appIdList;
		SqlSession sqlSession = null;
		try {
			sqlSession = MaintenanceSqlSession.openSession(DB_TARGET.REAL);
			MaintenanceMapper maintenanceMapper = sqlSession.getMapper(MaintenanceMapper.class);
			
			appIdList = maintenanceMapper.selectAllAppId();
			logger.info("appIdList.size={}", appIdList.size());
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String line = System.getProperty("line.separator");
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName, false);
			
			for (String appId : appIdList) {
				fw.write(appId + line);
			}
			
			fw.flush();
			fw.close();
			fw = null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
		
		logger.info("written to {}", fileName);
	}
	
}
