package com.horace.wifilocation.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.horace.wifilocation.R;
import com.horace.wifilocation.bean.LocPoint;
import com.horace.wifilocation.bean.WiFiAP;
import com.horace.wifilocation.comm.MyApplication;

public class RecordActivity extends Activity implements OnClickListener {

	private TextView mDetailTv;
	private Button mStartBtn;
	private Button mLocateBtn;
	private EditText X, Y, mTimesEt, mIntervalEt;
	private LocPoint mPoint;
	private WifiManager mWifiManager;
	private Handler mHandler;
	private List<ScanResult> mWiFiSRs;// 扫描结果
	private Message msg;
	private int count = 0;
	private int times = 0;
	private Signal signal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		inits();
	}

	private void inits() {

		X = (EditText) findViewById(R.id.x);
		Y = (EditText) findViewById(R.id.y);
		// 次数
		mTimesEt = (EditText) findViewById(R.id.times);
		// 间隔
		mIntervalEt = (EditText) findViewById(R.id.interval);
		mDetailTv = (TextView) findViewById(R.id.wifi);
		mStartBtn = (Button) findViewById(R.id.main_start_btn);
		mStartBtn.setOnClickListener(this);
		mLocateBtn = (Button) findViewById(R.id.main_locate_btn);
		mLocateBtn.setOnClickListener(this);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mHandler = new MyHandler();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_start_btn:
			if (isValid()) {
				mPoint = new LocPoint();
				mWifiManager.startScan();
				times = Integer.parseInt(mTimesEt.getText().toString());
				mPoint.aps.clear();
				mPoint.x = Float.parseFloat(X.getText().toString());
				mPoint.y = Float.parseFloat(Y.getText().toString());

				signal = new Signal(Integer.valueOf(mIntervalEt.getText().toString()), mWifiManager.getScanResults()
						.size(), mPoint);

				Log.i("WiFiLocation", "scanresults.size" + mWifiManager.getScanResults().size());
				signal.start();
				mDetailTv.append("\r\n");
				mDetailTv.append("开始记录锚节点\r\n");
			}
			break;

		case R.id.main_locate_btn:
			startActivity(new Intent(this, LocationActivity.class));
			break;

		default:
			break;
		}
	}

	private boolean isValid() {
		if (!TextUtils.isEmpty(X.getText().toString()) && !TextUtils.isEmpty(Y.getText().toString())
				&& !TextUtils.isEmpty(mTimesEt.getText().toString())
				&& !TextUtils.isEmpty(mIntervalEt.getText().toString()))
			return true;
		else {
			Toast.makeText(getApplicationContext(), "输入不能为空。", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private class Signal extends Thread {

		private int period;
		private int size;
		private LocPoint locPoint;
		private int i = 0;

		public Signal(int period, int size, LocPoint pLocPoint) {
			this.period = period;
			this.size = size;
			this.locPoint = pLocPoint;
		}

		@Override
		public void run() {
			super.run();
			msg = mHandler.obtainMessage();
			msg.what = 0x01;
			Bundle bundle = new Bundle();
			bundle.putParcelable("Point", locPoint);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
			mHandler.sendEmptyMessage(0x02);
			mWifiManager.disconnect();
		}
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x01:
				Bundle bundle = msg.peekData();
				LocPoint point = bundle.getParcelable("Point");
				point.aps.clear();
				WiFiAP ap = null;
				if (mWifiManager.getScanResults() != null) {
					mWiFiSRs = mWifiManager.getScanResults();
					for (int i = 0; i < mWiFiSRs.size(); i++) {
						String SSID = mWiFiSRs.get(i).SSID;
						int level = mWiFiSRs.get(i).level;
						if (MyApplication.totalSSIDs.contains(SSID)) {
							if (MyApplication.minLevel.get(SSID) > level)
								MyApplication.minLevel.put(SSID, level);
						} else {
							MyApplication.totalSSIDs.add(SSID);
							MyApplication.minLevel.put(SSID, level);
						}
						ap = new WiFiAP(SSID, level);
						point.aps.add(ap);
					}

					Log.i("WiFiLocation", point.x + "-" + point.y + "---" + point.toString() + point.aps.size());
					// 把锚节点加入表中
					MyApplication.locPoints.add(point);
					Log.i("WiFiLocation", "MyApplication.locPoints.get(0).aps.size()"
							+ MyApplication.locPoints.get(0).aps.size());
				}
				break;

			case 0x02:

				Log.i("WiFiLocation",
						"MyApplication.locPoints.get(0).aps.size()" + MyApplication.locPoints.get(0).aps.size());
				X.getText().clear();
				Y.getText().clear();
				mTimesEt.getText().clear();
				mIntervalEt.getText().clear();
				mDetailTv.append("完成扫描\r\n");
				mDetailTv.append("(X:" + mPoint.x + " Y:" + mPoint.y + ") 已被录入\r\n");
				mDetailTv.append("--------------------------------\r\n");
				break;

			default:
				break;
			}
		}
	}
}
