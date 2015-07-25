package com.horace.wifilocation.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.horace.wifilocation.R;
import com.horace.wifilocation.bean.LocPoint;
import com.horace.wifilocation.bean.WiFiAP;
import com.horace.wifilocation.comm.MyApplication;

public class LocationActivity extends Activity implements Runnable {

	private WifiManager mWifiManager;
	private List<ScanResult> mWifiList;
	private Handler mHandler;
	private LocPoint locatePoint;
	private List<WiFiAP> aps;
	private int index;// 根据距离远近，对所有的锚节点进行排序
	private int mini1, mini2;
	private double mini1Dis, mini2Dis;
	private LocPoint tempPoint;
	private Paint paint;
	private MyView myView;
	private int width, height;
	private Button mLocateBtn;
	private Map<Integer, Double> map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inits();
		myView = new MyView(this);
		myView.setBackgroundResource(R.drawable.pic_cell);
		mLocateBtn = new Button(this);
		mLocateBtn.setLayoutParams(new LayoutParams(80, 65));
		mLocateBtn.setText("定位");
		FrameLayout frameLayout = new FrameLayout(getApplicationContext());
		frameLayout.addView(myView);
		frameLayout.addView(mLocateBtn);
		setContentView(frameLayout);
		mLocateBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Thread thread = new Thread(LocationActivity.this);
				thread.start();
			}
		});
	}

	private void inits() {
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mHandler = new MyHandler();
		aps = new ArrayList<WiFiAP>();
		paint = new Paint();
		paint.setColor(Color.RED);
		locatePoint = new LocPoint();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels; // 屏幕宽度（像素）
		height = metric.heightPixels; // 屏幕高度（像素）
		mLocateBtn = (Button) findViewById(R.id.locate_btn_locate);
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x01:
				locate();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	private void locate() {
		mWifiManager.startScan();
		if (mWifiManager.getScanResults() != null) {
			mWifiList = mWifiManager.getScanResults();
			aps.clear();
			for (Iterator<ScanResult> iterator = mWifiList.iterator(); iterator.hasNext();) {
				ScanResult sr = (ScanResult) iterator.next();
				WiFiAP ap = new WiFiAP(sr.SSID, sr.level);
				aps.add(ap);
			}
			locatePoint.aps = aps;
			Log.i("LocationActivity", "locPoints.size()" + String.valueOf(MyApplication.locPoints.size()));
			Log.i("LocationActivity",
					"MyApplication.locPoints.get(0).aps.size()" + MyApplication.locPoints.get(0).aps.size() + "--"
							+ MyApplication.locPoints.get(0).x + "-" + MyApplication.locPoints.get(0).y);
			Log.i("LocationActivity", "locateaps.size()" + String.valueOf(aps.size()));
			mini1Dis = calSimilarity(locatePoint, MyApplication.locPoints.get(0));
			mini2Dis = mini1Dis;

			map = new HashMap<Integer, Double>();

			// 每个锚节点
			for (Iterator<LocPoint> iterator = MyApplication.locPoints.iterator(); iterator.hasNext();) {
				tempPoint = iterator.next();
				double tempDis = calSimilarity(locatePoint, tempPoint);

				if (tempDis < mini1Dis) {
					mini1Dis = tempDis;
					mini1 = index;
				}
				map.put(index, tempDis);
				index++;
			}

			ArrayList<Integer> keys = new ArrayList<Integer>(map.keySet());// 得到key集合

			Collections.sort(keys, new Comparator<Object>() {
				// 重新来实现这个比较方法
				@Override
				public int compare(Object o1, Object o2) {

					// 按照value的值降序排列，若要升序，则这里小于号换成大于号
					if (Double.parseDouble(map.get(o1).toString()) > Double.parseDouble(map.get(o2).toString()))
						return 1;

					else if (Double.parseDouble(map.get(o1).toString()) == Double.parseDouble(map.get(o2).toString()))
						return 0;

					else
						return -1;
				}
			});

			Log.i("LocationActivity", "map.get(keys.get(0)):***" + map.get(keys.get(0)) + "---mini1Dis" + mini1Dis);
			Log.i("LocationActivity", "keys.get(0):***" + keys.get(0) + "---mini1Dis" + mini1Dis);
			List<LocPoint> temPoints = new ArrayList<LocPoint>();
			temPoints.add(MyApplication.locPoints.get(keys.get(0)));
			temPoints.add(MyApplication.locPoints.get(keys.get(1)));
			temPoints.add(MyApplication.locPoints.get(keys.get(2)));
			drawPoint(temPoints);

			Log.i("LocationActivity", "mini_index:***" + mini1 + "---mini1Dis" + mini1Dis);
		}

	}

	// 计算定位点与锚节点AP的相似度
	private double calSimilarity(LocPoint lp1, LocPoint lp2) {
		float result = 0.0f;
		String str;
		Map<String, Integer> tempMap1 = new HashMap<String, Integer>();
		Map<String, Integer> tempMap2 = new HashMap<String, Integer>();

		int i, j;

		for (j = 0; j < lp2.aps.size(); j++) {
			tempMap2.put(lp2.aps.get(j).getSSID(), lp2.aps.get(j).getLevel());
		}

		for (i = 0; i < lp1.aps.size(); i++) {
			tempMap1.put(lp1.aps.get(i).getSSID(), lp1.aps.get(i).getLevel());
		}

		Iterator<String> iterator = MyApplication.totalSSIDs.iterator();
		while (iterator.hasNext()) {
			str = iterator.next();
			if (tempMap1.containsKey(str) && tempMap2.containsKey(str)) {
				result += (tempMap1.get(str) - tempMap2.get(str)) * (tempMap1.get(str) - tempMap2.get(str));
			}

			if (tempMap1.containsKey(str) && !tempMap2.containsKey(str)) {
				result += (tempMap1.get(str) - MyApplication.minLevel.get(str) * 1.01)
						* (tempMap1.get(str) - MyApplication.minLevel.get(str) * 1.01);
			}

			if (!tempMap1.containsKey(str) && tempMap2.containsKey(str)) {
				result += (tempMap2.get(str) - MyApplication.minLevel.get(str) * 1.01)
						* (tempMap2.get(str) - MyApplication.minLevel.get(str) * 1.01);
			}
		}
		return Math.sqrt(result);
	}

	// 定位完成时，通知handler进行下一次定位
	private void notifyStartLocate() {
		Log.i("LocationActivity", "notifyStartLocate()******");
		index = 0;
		mHandler.sendEmptyMessage(0x01);
	}

	private void drawPoint(List<LocPoint> locPoint) {

		myView.x = locPoint.get(0).x / 2 + locPoint.get(1).x / 3 + locPoint.get(2).x / 6;
		myView.y = locPoint.get(0).y / 2 + locPoint.get(1).y / 3 + locPoint.get(2).y / 6;
		myView.invalidate();
		Toast.makeText(getApplicationContext(), "X:" + myView.x + "  Y:" + myView.y, Toast.LENGTH_SHORT).show();
	}

	private class MyView extends View {
		float x = 0, y = 0;
		Paint mPaint;
		Bitmap bitmap;

		public MyView(Context context) {
			super(context);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(0xFFFF0000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(12);
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_locate);
		}

		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawBitmap(bitmap, x / 8 * width, y / 11 * width, mPaint);
		}
	}

	@Override
	public void run() {
		while (true) {
			notifyStartLocate();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
