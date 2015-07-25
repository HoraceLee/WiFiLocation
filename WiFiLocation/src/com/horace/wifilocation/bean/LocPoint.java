package com.horace.wifilocation.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class LocPoint implements Parcelable {

	public List<WiFiAP> aps = new ArrayList<WiFiAP>();
	public float x;
	public float y;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

	@Override
	public String toString() {
		return super.toString();
	}

}
