package com.entity;

public class Picture {
	private String url;
	private String charac_value;
	public Picture(String pu, String charac_value) {
		super();
		this.url = pu;
		this.charac_value = charac_value;
	}
	public Picture() {
		super();
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCharac_value() {
		return charac_value;
	}
	public void setCharac_value(String charac_value) {
		this.charac_value = charac_value;
	}
	@Override
	public String toString() {
		return "Picture [url=" + url + ", charac_value=" + charac_value + "]";
	}
	
	
}
