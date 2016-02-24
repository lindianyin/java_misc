package com.nfl.kfb.account;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.zip.CRC32;


public class ActiveCode {
	private int crc32; // 4
	private short name;// 2
	private short start;// 2
	private short end;// 2
	private byte type;// 1
	private int id; // 4
	private short[] items;

	private final byte[] key = new byte[] { 11, 23, 88, 43, 55, 26, 77, 45 };
	private final byte multiServer = 1;
	private final String zeorDate = "2014-11-11 00:00:00";
	
	private ActiveCode(){
		
	}
	
	private ActiveCode(short name, short start, short end, byte type,
			short[] items,int id) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
		this.type = type;
		this.items = items;
		this.id = id;
	}
	
	private static  ActiveCode ac = null;
	
	public static ActiveCode getInstance(){
		if(ac == null){
			ac = new ActiveCode();
		}
		return ac;
	}
	
	

	/**
	 * 
	 * @param name 渠道号
	 * @param startDate 开始时间  
	 * @param h 持续时间（单位小时）
	 * @param isMultiServer （是否多服使用）
	 * @param items 物品[物品id，物品类型,....]
	 * @return
	 * @throws Exception 
	 */
	public ActiveCode getActivityCode(short name, Date startDate, short h,
			boolean isMultiServer, short[] items,int id) throws Exception {
		if(items.length % 2 != 0){
			throw new Exception("items 不是偶数");
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date _zeorDate = format.parse(zeorDate);
		long seconds = startDate.toInstant().toEpochMilli()
				- _zeorDate.toInstant().toEpochMilli();
		short start = (short) (seconds / (3600 * 1000L));
		byte type = 0;
		if (isMultiServer) {
			type = multiServer;
		}
		short end = (short) (start + h);
		return new ActiveCode(name, start, end, type, items,id);
	}

	public String getActivityCode() {
		byte[] buff = getBytes();
		java.util.zip.CRC32 crc = new CRC32();
		crc.update(buff, 4, buff.length - 4);
		int crcVal = (int) crc.getValue();
		byte[] byteCrc = intToBytes(crcVal);
		System.arraycopy(byteCrc, 0, buff, 0, byteCrc.length);
		buff = encrypt(buff);
		return Base64.getEncoder().encodeToString(buff);
	}

	public byte[] encrypt(byte[] src) {
		byte[] copyOf = Arrays.copyOf(src, src.length);

		for (int i = 0; i < copyOf.length; i++) {
			copyOf[i] ^= key[i % key.length];
		}
		return copyOf;
	}

	public byte[] deEncrypt(byte[] src) {
		byte[] copyOf = Arrays.copyOf(src, src.length);
		return encrypt(copyOf);
	}

	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(4 + 2 + 2 + 2 + 1+4
				+ items.length*2);
		buffer.putInt(0);
		buffer.putShort(name);
		buffer.putShort(start);
		buffer.putShort(end);
		buffer.put(type);
		buffer.putInt(id);
		for (int i = 0; i < items.length; i++) {
			buffer.putShort(items[i]);
		}
		return buffer.array();
	}

	public byte[] intToBytes(int src) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(src);
		return buffer.array();
	}
	
	public boolean isTimeOut() throws ParseException{
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date _zeorDate = format.parse(zeorDate);
		long seconds = now.toInstant().getEpochSecond()
				- _zeorDate.toInstant().getEpochSecond();
		
		short nowHour = (short)(seconds / 3600);
		if(start <= nowHour && nowHour <= end){
			return false;
		}
		return true;
	}
	
	public boolean isMultiServer(){
		return type == multiServer ? true : false;
	}
	
	public boolean isValidName(short _name){
		return name == _name;
	}
	
	public short[] getItems(){
		return items;
	}
	
	public boolean isValidCrc32(byte[] src){
		ByteBuffer allocate = ByteBuffer.allocate(4);
		allocate.put(src, 0, 4);
		allocate.rewind();
		int crc = allocate.getInt();
		
		java.util.zip.CRC32 crc32 = new CRC32();
		crc32.update(src, 4, src.length-4);
		int crc1 = (int)crc32.getValue();
		return crc == crc1;
	}
	
	public ActiveCode parseActive(String code) throws Exception{
		byte[] deBytes = Base64.getDecoder().decode(code);
		byte[] data = deEncrypt(deBytes);
		if(!isValidCrc32(data)){
			throw new Exception("校验不正确");
		}
		ByteBuffer buff = ByteBuffer.allocate(data.length-4);
		buff.put(data, 4, data.length-4);
		buff.rewind();
		short _name = buff.getShort();//2
		short _start = buff.getShort(); // 2
		short _end = buff.getShort(); //2
		byte _type = buff.get();//1
		int _id = buff.getInt();
		short[] _items = new short[(data.length-4-2-2-2-1-4) / 2];
		for(int i=0;i<_items.length;i++){
			_items[i] = buff.getShort();
		}
		ActiveCode ac = new ActiveCode(_name,_start,_end,_type,_items,_id);
		return ac;
		
	}
}
