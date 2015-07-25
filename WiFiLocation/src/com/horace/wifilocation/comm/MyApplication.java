package com.horace.wifilocation.comm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Application;

import com.horace.wifilocation.bean.LocPoint;

public class MyApplication extends Application {

	private static MyApplication instance;
	public static List<LocPoint> locPoints;
	public static HashSet<String> totalSSIDs;
	public static Map<String, Integer> minLevel;// 检测到所有的AP点的level的最小值集合

	@Override
	public void onCreate() {
		super.onCreate();
		if (instance == null) {
			instance = this;
		}
		locPoints = new ArrayList<LocPoint>();
		totalSSIDs = new HashSet<String>();
		minLevel = new HashMap<String, Integer>();
	}

	public static MyApplication getInstance() {
		return instance;
	}
}
