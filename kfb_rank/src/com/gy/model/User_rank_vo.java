package com.gy.model;

public class User_rank_vo implements Comparable<User_rank_vo>{
	private int user_account_id;
	private int score;
	private String nickname;
	
	public int getUser_account_id() {
		return user_account_id;
	}
	public void setUser_account_id(int user_account_id) {
		this.user_account_id = user_account_id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	@Override
	public int compareTo(User_rank_vo o) {
		return -(this.getScore() - o.getScore());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + user_account_id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User_rank_vo other = (User_rank_vo) obj;
		if (user_account_id != other.user_account_id)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "User_rank_vo [user_account_id=" + user_account_id + ", score="
				+ score + ", nickname=" + nickname + "]";
	}
	
	
	
}
