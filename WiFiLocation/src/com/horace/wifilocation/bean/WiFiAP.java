package com.horace.wifilocation.bean;

public class WiFiAP {

	private String SSID;
	private int level;

	public WiFiAP(String sSID, int level) {
		super();
		SSID = sSID;
		this.level = level;
	}

	public WiFiAP() {
		super();
	}

	public String getSSID() {
		return SSID;
	}

	public void setSSID(String sSID) {
		SSID = sSID;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public boolean equals(Object o) {
		if (this.getSSID().equals(((WiFiAP) o).getSSID()))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "WiFiAP [SSID=" + SSID + ", level=" + level + "]";
	}

}
