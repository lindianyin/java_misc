package com.nfl.kfb.shop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class CmccPay {
	public static Trusted2ServQueryResp queryOrder(Trusted2ServQueryReq req) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		String ret = xmlMapper.writeValueAsString(req);
		ret = "<?xml version=\"1.0\" standalone=\"yes\"?>" + ret;
		ret = ret.replaceFirst(" xmlns=\"\"", "");
		System.out.println("ret:" + ret);
		String strURL = "http://ospd.mmarket.com:8089/trust";
		PostMethod post = new PostMethod(strURL);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				ret.getBytes("UTF-8"));
		RequestEntity entity = new InputStreamRequestEntity(inputStream,
				"text/xml; charset=UTF-8");
		post.setRequestEntity(entity);
		HttpClient httpclient = new HttpClient();
		try {
			int result = httpclient.executeMethod(post);
			// Display status code
			System.out.println("Response status code: " + result);
			// Display response
			System.out.println("Response body: ");
			System.out.println(post.getResponseBodyAsString());
			String retString = post.getResponseBodyAsString();
			Trusted2ServQueryResp readValue2 = xmlMapper.readValue(retString,
					Trusted2ServQueryResp.class);
			System.out.println(readValue2);
			System.out
					.println("Totalprice:" + readValue2.TotalPrice.intValue());

			SimpleDateFormat simpleDataFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss");
			Date startDate = null;
			try {
				startDate = simpleDataFormat.parse(readValue2.StartDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.println(startDate);
			return readValue2;
		} finally {
			post.releaseConnection();
		}
		

	}

}
