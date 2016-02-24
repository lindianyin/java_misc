/**
 * 
 */
package com.nfl.kfb.maintenance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.ibatis.session.SqlSession;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession;
import com.nfl.kfb.maintenance.dao.MaintenanceSqlSession.DB_TARGET;
import com.nfl.kfb.maintenance.mapper.GameLog;
import com.nfl.kfb.maintenance.mapper.Mail;
import com.nfl.kfb.maintenance.mapper.MaintenanceMapper;

/**
 * 메일 보내기
 * @author KimSeongsu
 * @since 2013. 10. 22.
 *
 */
public class MailSender {
	
	private static final Logger logger = LoggerFactory.getLogger(MailSender.class);
	
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
		MailSender mailSender = new MailSender();
		
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-10-22 10관문돌파보상.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-10-29 이벤트보상.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-11-05 할로윈 이벤트 보상.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-11-05 10관문 돌파 보상.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-11-18 빼빼로데이 보상.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-11-28 열삼2상고 크로스프로모.xlsx");
//		mailSender.batchSendMailFromXls("./doc/MailSender/2013-12-30 25관문 돌파.xlsx");
	}
	
	
	/**
	 * 같은 sender, itemid, itemcnt, keepdays, msg 끼리 owner를 묶어서 batch로 실행
	 * @param filename
	 * @throws InvalidFormatException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void batchSendMailFromXls(String filename) throws InvalidFormatException, FileNotFoundException, IOException {
		Calendar nowCalendar = Calendar.getInstance();
		
		Workbook wb = WorkbookFactory.create(new FileInputStream(filename));
		
		Sheet sheet = wb.getSheetAt(0);
		int rowMax = sheet.getLastRowNum();
		logger.info("MAIL_SENDER FILE[{}] ROWS[{}]", filename, rowMax-1);
		
		HashMap<String, SendMailOwnerGroup> sendMailOwnerGroupMap = new HashMap<String, SendMailOwnerGroup>();
		
		for (int rowIdx = 1; rowIdx <= rowMax; rowIdx++) {
//			logger.debug("MAIL_SENDER PROCESSING[{}/{}]", rowIdx, rowMax);
			
			Row row = null;
			String sender;
			String owner;
			int itemId;
			int itemCnt;
			int keepDays;
			String msg;
			try {
				row = sheet.getRow(rowIdx);
				
				sender = row.getCell(0).getStringCellValue();
				if (sender == null || sender.trim().length() < 1)
					throw new RuntimeException();
				owner = row.getCell(1).getStringCellValue();
				if (owner == null || owner.trim().length() < 1)
					throw new RuntimeException();
				itemId = (int) row.getCell(2).getNumericCellValue();
				itemCnt = (int) row.getCell(3).getNumericCellValue();
				if (itemCnt == 0)
					throw new RuntimeException();
				keepDays = (int) row.getCell(4).getNumericCellValue();
				if (keepDays == 0)
					throw new RuntimeException();
				msg = row.getCell(5).getStringCellValue();
				if (msg == null || msg.trim().length() < 1)
					throw new RuntimeException();
				
//				logger.debug("MAIL_SENDER PROCESSING[{}/{}] SENDER[{}] OWNER[{}] ITEM_ID[{}] ITEM_CNT[{}] KEEP_DAYS[{}] MSG[{}]"
//						, rowIdx, rowMax, sender, owner, itemId, itemCnt, keepDays, msg);
			} catch (Exception e) {
//				logger.error(this.toString(), e);
				continue;
			}
			
			final String key = SendMailOwnerGroup.genKey(sender, itemId, itemCnt, keepDays, msg);
			SendMailOwnerGroup sendMailOwnerGroup = sendMailOwnerGroupMap.get(key);
			if (sendMailOwnerGroup == null) {
				sendMailOwnerGroup = new SendMailOwnerGroup(sender, itemId, itemCnt, keepDays, msg);
				sendMailOwnerGroupMap.put(key, sendMailOwnerGroup);
			}
			
			sendMailOwnerGroup.addOwner(owner);
		}
		
		int success = 0;
		
		Collection<SendMailOwnerGroup> sendMailOwnerGroups = sendMailOwnerGroupMap.values();
		
		// logging
		for (SendMailOwnerGroup sendMailOwnerGroup : sendMailOwnerGroups) {
			logger.info("MAIL_SENDER SENDER[{}] ITEM[{}] CNT[{}] MSG[{}] KEEPDAYS[{}] OWNERS[{}]"
					, sendMailOwnerGroup.getSender(), sendMailOwnerGroup.getItemId(), sendMailOwnerGroup.getItemCnt(), sendMailOwnerGroup.getMsg()
					, sendMailOwnerGroup.getKeepDays(), sendMailOwnerGroup.getOwners().size());
		}
		
		try {
			logger.info("Waiting 5 seconds to start job");
			Thread.sleep(1000 * 5);
		} catch (Exception e) {}
		
		for (SendMailOwnerGroup sendMailOwnerGroup : sendMailOwnerGroups) {
			final String sender = sendMailOwnerGroup.getSender();
			final int itemId = sendMailOwnerGroup.getItemId();
			final int itemCnt = sendMailOwnerGroup.getItemCnt();
			final int keepDays = sendMailOwnerGroup.getKeepDays();
			final String msg = sendMailOwnerGroup.getMsg();
			
			try {
				success += sendMail(sender, itemId, itemCnt, msg, nowCalendar, keepDays
									, sendMailOwnerGroup.getOwners().toArray(new String[sendMailOwnerGroup.getOwners().size()]));
			} catch (Exception e) {
				logger.error(this.toString(), e);
			}
		}
		
		logger.info("MAIL_SENDER SUCCESS[{}]", success);
	}
	
	public void sendMailFromXls(String filename) throws InvalidFormatException, FileNotFoundException, IOException {
		Calendar nowCalendar = Calendar.getInstance();
		
		Workbook wb = WorkbookFactory.create(new FileInputStream(filename));
		
		Sheet sheet = wb.getSheetAt(0);
		int rowMax = sheet.getLastRowNum();
		logger.info("MAIL_SENDER FILE[{}] ROWS[{}]", filename, rowMax-1);
		int success = 0;
		
		for (int rowIdx = 1; rowIdx <= rowMax; rowIdx++) {
			logger.debug("MAIL_SENDER PROCESSING[{}/{}]", rowIdx, rowMax);
			
			Row row = null;
			String sender;
			String owner;
			int itemId;
			int itemCnt;
			int keepDays;
			String msg;
			try {
				row = sheet.getRow(rowIdx);
				
				sender = row.getCell(0).getStringCellValue();
				if (sender == null || sender.trim().length() < 1)
					throw new RuntimeException();
				owner = row.getCell(1).getStringCellValue();
				if (owner == null || owner.trim().length() < 1)
					throw new RuntimeException();
				itemId = (int) row.getCell(2).getNumericCellValue();
				itemCnt = (int) row.getCell(3).getNumericCellValue();
				if (itemCnt == 0)
					throw new RuntimeException();
				keepDays = (int) row.getCell(4).getNumericCellValue();
				if (keepDays == 0)
					throw new RuntimeException();
				msg = row.getCell(5).getStringCellValue();
				if (msg == null || msg.trim().length() < 1)
					throw new RuntimeException();
				
//				logger.debug("MAIL_SENDER PROCESSING[{}/{}] SENDER[{}] OWNER[{}] ITEM_ID[{}] ITEM_CNT[{}] KEEP_DAYS[{}] MSG[{}]"
//						, rowIdx, rowMax, sender, owner, itemId, itemCnt, keepDays, msg);
			} catch (Exception e) {
//				logger.error(this.toString(), e);
				continue;
			}
			
			try {
				success += sendMail(sender, itemId, itemCnt, msg, nowCalendar, keepDays, owner);
				row.createCell(6).setCellValue("success");
			} catch (Exception e) {
				logger.error(this.toString(), e);
			}
			
			logger.info("MAIL_SENDER SUCCESS[{}]", success);
		}
	}
	
	public int sendMail(String senderAppId, int itemId, int itemCnt, String msg, Calendar now, int keepDays, String ... ownerAppId) throws Exception {
		logger.info("MAIL_SENDER SENDER[{}] OWNERS[{}] ITEM[{}] CNT[{}] MSG[{}] NOW[{}] KEEPDAYS[{}]"
				, senderAppId, ownerAppId==null? 0 : ownerAppId.length, itemId, itemCnt, msg, now.getTime(), keepDays);
		
		final int COMMIT_PER = 10;
		final int nowEpoch = (int)(now.getTimeInMillis() / 1000);
		final int month = now.get(Calendar.YEAR) * 100 + (now.get(Calendar.MONTH) + 1);
		
		int success = 0;
		
		SqlSession sqlSession = null;
		try {
			sqlSession = MaintenanceSqlSession.openSession(DB_TARGET.REAL);
			MaintenanceMapper maintenanceMapper = sqlSession.getMapper(MaintenanceMapper.class);
			
			int successCommit = 0;
			for (int i=0; i<ownerAppId.length; i++) {
				successCommit += 1;
				
				Mail mail = new Mail();
				mail.setSender(senderAppId);
				mail.setOwner(ownerAppId[i]);
				mail.setItem(itemId);
				mail.setCnt(itemCnt);
				mail.setDelDt(nowEpoch + (60 * 60 * 24) * keepDays);
				mail.setMsg(msg);
				
//				logger.debug("MAIL_SENDER SENDER[{}] OWNER[{}] ITEM[{}] CNT[{}] DEL_DT[{}] MSG[{}]"
//					, mail.getSender(), mail.getOwner(), mail.getItem(), mail.getCnt(), mail.getDelDt(), mail.getMsg());
				
				maintenanceMapper.insertMail(mail);
				
				GameLog gameLog = new GameLog();
				gameLog.setLogType(100);			// 관리자가 메일 발송
				gameLog.setAppId(ownerAppId[i]);
				gameLog.setItemId(mail.getItem());
				gameLog.setItemCnt(mail.getCnt());
				gameLog.setReserved0(mail.getMailKey());
				gameLog.setMonth(month);
				gameLog.setEpoch(nowEpoch);
				
				maintenanceMapper.insertGameLog(gameLog);
				
				logger.info("MAIL_SENDER PROCESSING[{}/{}] RESULT[SUCCESS] SENDER[{}] OWNER[{}] ITEM[{}] CNT[{}] DEL_DT[{}] MSG[{}] MAIL_KEY[{}]"
						, i+1, ownerAppId.length, mail.getSender(), mail.getOwner(), mail.getItem(), mail.getCnt(), mail.getDelDt(), mail.getMsg(), mail.getMailKey());
				
				if (i==ownerAppId.length-1 || (i%COMMIT_PER) == (COMMIT_PER-1)) {
					sqlSession.commit();
					logger.info("MAIL_SENDER PROCESSING[{}/{}] COMMIT[SUCCESS]", i+1, ownerAppId.length);
					success += successCommit;
					successCommit = 0;
				}
			}
		} catch (Exception e) {
			sqlSession.rollback();
			logger.info("MAIL_SENDER COMMIT[FAIL]");
			throw e;
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		return success;
	}
	
	
}

class SendMailOwnerGroup {
	
	private final String sender;
	private final int itemId;
	private final int itemCnt;
	private final int keepDays;
	private final String msg;
	
	private final Collection<String> owners = new LinkedList<String>();
	
	public SendMailOwnerGroup(String sender, int itemId, int itemCnt, int keepDays, String msg) {
		this.sender = sender;
		this.itemId = itemId;
		this.itemCnt = itemCnt;
		this.keepDays = keepDays;
		this.msg = msg;
	}
	
	public Collection<String> getOwners() {
		return owners;
	}

	public void addOwner(String owner) {
		owners.add(owner);
	}

	public static final String genKey(String sender, int itemId, int itemCnt, int keepDays, String msg) {
		return "SENDER=" + sender + ",ITEM_ID=" + itemId + ",ITEM_CNT=" + itemCnt + ",KEEP_DAYS=" + keepDays + ",MSG=" + msg;
	}

	public String getSender() {
		return sender;
	}

	public int getItemId() {
		return itemId;
	}

	public int getItemCnt() {
		return itemCnt;
	}

	public int getKeepDays() {
		return keepDays;
	}

	public String getMsg() {
		return msg;
	}
	
}