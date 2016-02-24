package com.nfl.kfb.uc;

public class UcidBindCreateResponse extends BaseResponse {
	UcidBindCreateResponseData data;
	
	public UcidBindCreateResponseData getData(){
		return this.data;
	}
	public void setData(UcidBindCreateResponseData data){
		this.data = data;
	}
	
	public class UcidBindCreateResponseData{
		private int ucid;
		private String sid;
		public int getUcid(){
			return this.ucid;
		}
		
		public void setUcid(int ucid){
			this.ucid = ucid;
		}
		public String getSid(){
			return this.sid;
		}
		public void setSid(String sid){
			this.sid = sid;
		}
	}

}
