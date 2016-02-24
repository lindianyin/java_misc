package com.nfl.kfb.tcp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PkArea {
	private Map<String,Player> preMatch = new HashMap<String, Player>();
	private Map<Long,Map<String,Player>> matched = new HashMap<Long, Map<String,Player>>();
	private Map<Long,Map<String,Player>> playing = new HashMap<Long,Map<String,Player>>();
	
	public Map<String, Player> getPreMatch() {
		return preMatch;
	}
	public void setPreMatch(Map<String, Player> preMatch) {
		this.preMatch = preMatch;
	}
	public Map<Long, Map<String, Player>> getMatched() {
		return matched;
	}
	public void setMatched(Map<Long, Map<String, Player>> matched) {
		this.matched = matched;
	}
	public Map<Long, Map<String, Player>> getPlaying() {
		return playing;
	}
	public void setPlaying(Map<Long, Map<String, Player>> playing) {
		this.playing = playing;
	}
	
	
	
	
}
