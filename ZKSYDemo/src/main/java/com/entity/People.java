package com.entity;

public class People {
	private String offset;
	private String device_id;
	private String picture_url;
	private String charac_value;
	private String picture_time;
	private String key;
	
	
	public People() {
		super();
	}


	public People(String offset, String device_id, String picture_url, String charac_value, String picture_time,
			String key) {
		super();
		this.offset = offset;
		this.device_id = device_id;
		this.picture_url = picture_url;
		this.charac_value = charac_value;
		this.picture_time = picture_time;
		this.key = key;
	}


	public String getOffset() {
		return offset;
	}


	public void setOffset(String offset) {
		this.offset = offset;
	}


	public String getDevice_id() {
		return device_id;
	}


	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}


	public String getPicture_url() {
		return picture_url;
	}


	public void setPicture_url(String picture_url) {
		this.picture_url = picture_url;
	}


	public String getCharac_value() {
		return charac_value;
	}


	public void setCharac_value(String charac_value) {
		this.charac_value = charac_value;
	}


	public String getPicture_time() {
		return picture_time;
	}


	public void setPicture_time(String picture_time) {
		this.picture_time = picture_time;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	@Override
	public String toString() {
		return "People [offset=" + offset + ", device_id=" + device_id + ", picture_url=" + picture_url
				+ ", charac_value=" + charac_value + ", picture_time=" + picture_time + ", key=" + key + "]";
	}
	
}
