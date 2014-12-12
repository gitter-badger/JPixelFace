package net.rainbowcode.jpixelface;

public enum RedisKey {
	PROFILE_UUID("profileuuid"),
	UUID_NAME("nameuuid"),
	SKIN("skin");
	
	
	private final String key;
	
	private RedisKey(String key) {
		this.key = key;
	}
	
	public String buildKey(String param) {
		return key + ":" + param;
	}
	
	public byte[] buildByteKey(String param) {
		return buildKey(param).getBytes();
	}
}
