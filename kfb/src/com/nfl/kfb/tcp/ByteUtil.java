package com.nfl.kfb.tcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteUtil {
	public static int byteArray2Integer(byte[] src) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(src);
		DataInputStream dintput = new DataInputStream(bais);
		int ret =  dintput.readInt();
		dintput.close();
		bais.close();
		return ret;
	}
	
	public static byte[] Integer2ByteArray(int src) throws IOException{
		byte[] b = new byte[4];  
	    for(int i = 0;i < 4;i++){  
	        b[i] = (byte)(src >> (24 - i * 8));   
	    }  
	    return b;  
	}
	
	public static long byteArray2Long(byte[] src) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(src);
		DataInputStream dintput = new DataInputStream(bais);
		long ret =  dintput.readLong();
		dintput.close();
		bais.close();
		return ret;
	}
	
}
