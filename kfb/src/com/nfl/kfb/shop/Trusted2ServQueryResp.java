package com.nfl.kfb.shop;

/*
 * 
 * MsgType
 Version
 TradeID
 ReturnCode
 OrderID
 PayCode
 StartDate
 TotalPrice
 ChannelID
 ExData
 SrcIP

 * 
 * 
 * */

public class Trusted2ServQueryResp {
	public String MsgType = "Trusted2ServQueryResp";
	public String Version = "1.0.0";
	public String TradeID;
	public Integer ReturnCode;
	public String OrderID;
	public String PayCode;
	public String StartDate;
	public Double TotalPrice;
	public String ChannelID;
	public String EXDATA;
	public String SrcIP;
	@Override
	public String toString() {
		return "Trusted2ServQueryResp [MsgType=" + MsgType + ", Version="
				+ Version + ", TradeID=" + TradeID + ", ReturnCode="
				+ ReturnCode + ", OrderID=" + OrderID + ", PayCode=" + PayCode
				+ ", StartDate=" + StartDate + ", TotalPrice=" + TotalPrice
				+ ", ChannelID=" + ChannelID + ", EXDATA=" + EXDATA
				+ ", SrcIP=" + SrcIP + "]";
	}
	
}
