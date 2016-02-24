/**
 * 
 */
package com.nfl.kfb.model;

/**
 * @author KimSeongsu
 * @since 2013. 6. 13.
 *
 */
public enum ReturnCode {
	
	SUCCESS(0)
	, UNKNOWN_ERR(1)
	, WRONG_MD5(2)
	, WRONG_SESSION_KEY(3)
	, WRONG_WEEK(4)						// 주가 안맞음. 아마도 주가 넘어가서 그런듯
	, WRONG_GAME(5)						// 게임끝 요청이 잘못되었음. 해당게임은 없어짐
//	, GOT_REWARD(6)						// 보상을 이미 받았음
//	, INVITED(7)						// 이미 초대한 친구
	, CANNOT_START_NO_PUNCH(8)			// 주먹이 모자라서 게임을 시작할 수 없음
	, NOT_ENOUGH_MONEY(9)				// 돈 부족
	, INVALID_KAKAO_TOKEN(10)			// 카카오 access token check 에러
	, DELETED_MAIL(11)					// 삭제된 메일임. 메일리스트 다시 읽어야함
	, BLOCK_USER(12)					// 너 블럭됨
	, NOT_ENOUGH_DUNGEON_CNT(13)		// 하루 던전 제한에 걸려서 던전을 플레이할 수 없음
	
	
	
	
	
	// sample
	, SAMPLE_ROLLBACK(-1)
	
	
	;

	
	private final int code;
	
	private ReturnCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

}
