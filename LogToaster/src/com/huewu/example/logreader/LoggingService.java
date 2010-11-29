package com.huewu.example.logreader;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.devtcg.tools.logcat.LogcatProcessor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;
import com.huewu.apps.logtoaster.R;

/**
 * <p>
 * @file			LoggingService.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * Launch Logcat Processor. Show a notification icon. Display toast if an user set toast visibility true.
 * <br>
 * <p>
 * This program is subject to copyright protection in accordance with the
 * applicable law. It must not, except where allowed by law, by any means or
 * in any form be reproduced, distributed or lent. Moreover, no part of the
 * program may be used, viewed, printed, disassembled or otherwise interfered
 * with in any form, except where allowed by law, without the express written
 * consent of the copyright holder.
 * <p>
 * <br>
 * All Rights Reserved.   
 */

public class LoggingService extends Service{

	final static String EXTRA_FILTER_TAG = "filter";			//for intent extra
	final static String EXTRA_FILTER_PRIORITY = "priority";		//for intent extra
	final static String EXTRA_SHOW_ALL_LOG = "show_all";		//for intent extra
	final static String EXTRA_TOAST_VISIBILITY = "visiblity";	//for intent extra
	final static String EXTRA_TOAST_SIZE = "size";				//for intent extra
	final static String EXTRA_TOAST_LOCATION = "location";		//for intent extra

	final static int SIZE_100 = 1;
	final static int SIZE_50 = 2;
	final static int SIZE_30 = 3;

	public class LocalBinder extends Binder {
		LoggingService getService() {
			return LoggingService.this;
		}
	}	

	public interface OnNewLogListener{
		void onNewLine(String log);
	}

	public void setOnNewLogListener(OnNewLogListener listener){
		synchronized(mLogListenerMutex){
			this.mLogListener = listener;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		//no binder.
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Notification n = new Notification(R.drawable.toast_noti_icon, getString(R.string.app_name), System.currentTimeMillis());
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, LogRemocon.class), PendingIntent.FLAG_UPDATE_CURRENT);
		//when a notification is clicked. launch a log remocon activity.
		n.setLatestEventInfo(this,getString(R.string.app_name), getString(R.string.click_this_pannel_to_invoke_a_log_remocon), pi);
		n.tickerText = getString(R.string.logging_service_is_now_running_);
		startForeground(2002, n);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		//whenever startService is called, stop old logcat processor and then start a new logcat processor.

		if(intent == null)
			return;
		else{
			if(mLoggerProcess != null)
				mLoggerProcess.stopCatter();
			
			mLoggerProcess = new LogToaster("Log Toaster Service");
			mTags = intent.getStringExtra(EXTRA_FILTER_TAG);
			mLogPriority = intent.getStringExtra(EXTRA_FILTER_PRIORITY);
			mShowAllLog = intent.getBooleanExtra(EXTRA_SHOW_ALL_LOG, false);
			mVisibility = intent.getBooleanExtra(EXTRA_TOAST_VISIBILITY, false);
			mSize = intent.getIntExtra(EXTRA_TOAST_SIZE, SIZE_30);
			mGravity = intent.getIntExtra(EXTRA_TOAST_LOCATION, Gravity.BOTTOM);
			
			if(mTags == null)
				return;	//do nothing.
			if(mLogPriority == null)
				mLogPriority = "D";	//default value; (Debug)
			
			StringTokenizer st = new StringTokenizer(mTags, ","); //tag strings can be seperated by ','.
			while(st.hasMoreTokens() == true){
				mLoggerProcess.addFilter(st.nextToken() + ":" + mLogPriority);	
			}
			
			if(mShowAllLog == true)
				mLoggerProcess.addFilter("*:" + mLogPriority);
			else
				mLoggerProcess.addFilter("*:F");	//fatal.
						
			mLoggerProcess.start();
			mDirty = false;			
			mToastLogs.clear();
			Log.v("LogToaster", "Logging process is launching with filter : " + mLoggerProcess.getFilterString());
			updateToast();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mLoggerProcess != null)
			mLoggerProcess.stopCatter();
		mHandler.removeMessages(NEW_MESSAGE);
		mHandler.removeMessages(SHOW_MESSAGE);

		if(mLogToast != null)
			mLogToast.cancel();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//update toast, when a device is rotated.
		updateToast();
	};

	private String formatLog(){

		String log = "";
		synchronized(LoggingService.this){
			while(mToastLogs.size() > mMaxLogLine)
				mToastLogs.remove(0);
			for(String str : mToastLogs){
				//parse tokens.
				String[] tokens = LogUtils.parseTokens(str);
				if(tokens.length != 3){
					log += str + "<br>";
				}else{
					String color = "";
					switch(tokens[0].charAt(0)){
					case 'V':
						color = "#555555";	//GRAY
						break;
					case 'D':
						color = "#2828C8";	//40,40,200
						break;
					case 'I':
						color = "#28C828";	//40,200,40
						break;
					case 'W':
						color = "#C87800";	//200,120,0
						break;
					case 'E':
						color = "#C82800";	//200,40,0
						break;
					}
					
					log += "<i><b><font color='" + color + "'>" + tokens[0] + "&nbsp;&nbsp;&nbsp;&nbsp;" + tokens[1] + "</font></i><br>" + tokens[2] + "<br>";
				}
			}
			mDirty = false;
		}
		return log;
	}

	private void createToast(){
		if(mLogToast == null)
			mLogToast = new Toast(this);

		mLogToast.setDuration(Toast.LENGTH_LONG);
		mLogToast.setGravity(mGravity, 0, 0);

		LogView lv = new LogView(this);
		lv.setBackgroundColor(Color.argb(120, 0, 0, 0));
		lv.setTextSize(11.0f);
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		lv.setWidth(w);
		lv.setHeight(h / mSize);
		lv.setPadding(30, 0, 0, 0);
		mLogToast.setView(lv);

		synchronized(LoggingService.this){
			mDirty = true;
		}	           
	}
	
	boolean getToastVisibility(){
		return mVisibility;
	}
	
	void setToastGravity(int gravity){
		//adjust toast position.
		switch(gravity){
		case Gravity.TOP:
			mGravity = Gravity.TOP;
			break;
		case Gravity.CENTER:
			mGravity = Gravity.CENTER;
			break;
		case Gravity.BOTTOM:
			mGravity = Gravity.BOTTOM;
			break;
		}
	}

	void setToastSize(int size){
		//adjust toast size.
		switch(size){
		case 1:	//100% (size / 1)
			mMaxLogLine = 24;
			mSize = 1;
			break;
		case 2:	//50% (size / 2)
			mMaxLogLine = 12;
			mSize = 2;
			break;
		case 3:	//33% (size / 3)
			mMaxLogLine = 8;
			mSize = 3;
			break;
		}
	}

	void showToast(){
		mVisibility = true;
		//whenever an user change toast visibility, save it instantly. 
		//because, toast visibility can be changed log remocon and log reader.
		//so, both activities gui should be synchronized.
		savePreference();
		mHandler.removeMessages(NEW_MESSAGE);
		mHandler.sendEmptyMessage(NEW_MESSAGE);
	}

	void hideToast(){
		mVisibility = false;
		savePreference();
		mHandler.removeMessages(NEW_MESSAGE);
		mHandler.removeMessages(SHOW_MESSAGE);

		if(mLogToast != null){
			mLogToast.cancel();
		}
	}

	void updateToast(){
		if(mVisibility == true){
			hideToast();
			createToast();
			showToast();
		}else{
			createToast();
		}
	}

	String getFilterString(){
		if(mLoggerProcess == null)
			return "";
		return mLoggerProcess.getFilterString();
	}

	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(android.os.Message msg) {

			switch(msg.what){
			case NEW_MESSAGE:
				//new log message is added. 
				this.sendEmptyMessage(SHOW_MESSAGE);
				break;
			case SHOW_MESSAGE:
				//for avoid overwhelming logs, log toast is update only once per given period time (1sec). 
				long interval = System.currentTimeMillis() - mLastUpdateTime;
				if(interval > MIN_UPDATE_INTERVAL){
					if(mDirty == true){
						String logs = formatLog();
						LogView lv = (LogView) mLogToast.getView();
						lv.setText(Html.fromHtml(logs));
						interval = MIN_UPDATE_INTERVAL - interval;
					}else{
						//no update.
						interval = MAX_UPDATE_INTERVAL;
					}

					mLogToast.show();
					mLastUpdateTime = System.currentTimeMillis();
				}
				this.removeMessages(SHOW_MESSAGE);
				this.sendEmptyMessageDelayed(SHOW_MESSAGE, interval);
				break;
			}
		};
	};	
	
	void savePreference(){
		SharedPreferences preference = getSharedPreferences("setting", 0);
		Editor edit = preference.edit();
		edit.putBoolean(EXTRA_TOAST_VISIBILITY, mVisibility);
		edit.commit();		
	}
	
	class LogToaster extends LogcatProcessor{

		public LogToaster(String name) {
			super(name);
		}

		@Override
		public void onError(String msg, Throwable e) {
			//do nothing.
		}

		@Override
		public void onNewline(String line) {
			synchronized(LoggingService.this){
				synchronized (mLogListenerMutex) {
					if(mLogListener != null)
						mLogListener.onNewLine(line);
				}

				if(mVisibility == true){
					mToastLogs.add(line);
					mDirty = true;	//toast should be updated.
					mHandler.removeMessages(NEW_MESSAGE);
					mHandler.sendEmptyMessage(NEW_MESSAGE);
					return;
				}
			}
		}
	}//end of inner class
	
	//priavte event-related predefines.
	private final static int NEW_MESSAGE = 1001;
	private final static int SHOW_MESSAGE = 1002;
	private final static long MIN_UPDATE_INTERVAL = 1000;
	private final static long MAX_UPDATE_INTERVAL = 3000;
	
	//Member Variables.
	private LogcatProcessor mLoggerProcess = null;
	private Toast mLogToast = null;	
	private boolean mDirty = false;
	private boolean mVisibility = false;
	private OnNewLogListener mLogListener = null;
	private Object mLogListenerMutex = new Object();
	private long mLastUpdateTime = 0;
	private ArrayList<String> mToastLogs = new ArrayList<String>();
	private int mGravity = Gravity.BOTTOM;
	private int mSize = 3;;    
	private int mMaxLogLine = 8;
	private String mTags = "";
	private String mLogPriority = "S";
	private boolean mShowAllLog = false;	
	
	// This is the object that receives interactions from clients.  See
	// RemoteService for a more complete example.
	private final IBinder binder = new LocalBinder();	
}//end of class
