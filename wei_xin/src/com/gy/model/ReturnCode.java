package com.gy.model;

public enum ReturnCode {
	SUCCESS(0)
	, UNKNOWN_ERR(1);
	private final int code;
	
	private ReturnCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

}
