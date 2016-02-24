package com.nfl.kfb.tcp;


public enum CMD {
	ADD(1),
	ENTER_ARENA(10000),//进入竞技大厅
	LEAVE_ARENA(10001), //离开竞技大厅
	MATCH_SUCCESS(10002),//匹配成功(服务器直接返回数据给客户端)
	ENTER_AREAAA(10003), //进入某个场
	SUBMIT_POINT(10004),  //提交分数
	MATCH_RESULT(10005),  //比赛结果
	THREE_WIN(10006), 	//连胜三场
	GET_WORLD_FIGHT_LIST(10007),//获得世界对战列表
	EXIT_MATCH(10008) //退出匹配
	;
	
	final Integer _cmd;
	private CMD(int cmd){
		this._cmd = cmd;
	}
	public final int getValue()	{
		return _cmd;
	}
	public static final CMD valueOf(int value) {
		for (CMD storeType : CMD.values()) {
			if (storeType.getValue() == value)
				return storeType;
		}
		return null;
	}
	
	
}
