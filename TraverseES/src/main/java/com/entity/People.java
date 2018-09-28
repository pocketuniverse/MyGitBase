package com.entity;

public class People {
	private String offset;  //偏移量
	private String device_id; //设备id
	private String picture_url;  //图片url
	private String charac_value; //特征值
	private String picture_time; //图片时间
//	private String key;	//key键值
	private double likeValue; 	//相似度

	public People() {
		super();
	}

	public People(String offset, String device_id, String picture_url, String charac_value, String picture_time,
			double likeValue) {
		super();
		this.offset = offset;
		this.device_id = device_id;
		this.picture_url = picture_url;
		this.charac_value = charac_value;
		this.picture_time = picture_time;
//		this.key = key;
		this.likeValue = likeValue;
	}

	public double getLikeValue() {
		return likeValue;
	}

	public void setLikeValue(double likeValue) {
		this.likeValue = likeValue;
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

//	public String getKey() {
//		return key;
//	}
//
//	public void setKey(String key) {
//		this.key = key;
//	}

	@Override
	public String toString() {
		return "People [offset=" + offset + ", device_id=" + device_id + ", picture_url=" + picture_url
				+ ", charac_value=" + charac_value + ", picture_time=" + picture_time + ", likeValue="
				+ likeValue + "]";
	}

}
