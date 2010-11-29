package com.huewu.example.logreader;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.ToggleButton;
import com.huewu.apps.logtoaster.R;
import com.huewu.example.logreader.LoggingService.LocalBinder;
import com.huewu.example.logreader.LoggingService.OnNewLogListener;

/**
 * <p>
 * @file			LogReader.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * main activity. show log. toast setting. save logs.
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

public class LogReader extends Activity implements OnNewLogListener, DialogInterface.OnClickListener{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.main);

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabHost.addTab(mTabHost.newTabSpec("tab_test1")
				.setIndicator(getString(R.string.tabl_log_viewer),getResources()
						.getDrawable(android.R.drawable.ic_dialog_info))
						.setContent(R.id.log)); 
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2")
				.setIndicator(getString(R.string.tab_toast_setting), getResources()
						.getDrawable(android.R.drawable.ic_dialog_dialer))
						.setContent(R.id.toast_setting)); 
		mTabHost.setCurrentTab(0); 

		mLogView = (ListView) findViewById(R.id.log_list);
		mInputTag = (EditText)findViewById(R.id.input_tag);
		mStartButton = (ToggleButton) findViewById(R.id.start_log);
		mLogAdapter = new LogAdapter(this);
		mLogView.setEmptyView(findViewById(R.id.empty));
		mLogView.setAdapter(mLogAdapter);
		mLogView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE)
					mLogViewLocked = false;
				else
					mLogViewLocked = true;
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		mLogView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				mSelectedLog = mLogAdapter.getItem(position);
				mSaveLogMode = SAVE_PARTIAL_LOG;
				showDialog(DIALOG_SHOW_LOG);
			}
		});

		mToastVisibility = (ToggleButton) findViewById(R.id.toast_setting_visibility);
		mToastLocation = (Button) findViewById(R.id.toast_setting_location);
		mToastSize = (Button) findViewById(R.id.toast_setting_size);
		
		loggingServiceStopped();
		bindService(new Intent(this, LoggingService.class), serviceConn, 0);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mLoggingService != null)
			mLoggingService.setOnNewLogListener(null);
		unbindService(serviceConn);
		
		LogUtils.deleteTempLogFiles();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences preference = getSharedPreferences("setting", 0);
		mPreferenceFilter = preference.getString(LoggingService.EXTRA_FILTER_TAG, "");
		mPreferencePriority = preference.getString(LoggingService.EXTRA_FILTER_PRIORITY, "D");
		mPreferenceShowAll = preference.getBoolean(LoggingService.EXTRA_SHOW_ALL_LOG, true);
		mPreferenceVisibility = preference.getBoolean(LoggingService.EXTRA_TOAST_VISIBILITY, false);
		mPreferenceSize = preference.getInt(LoggingService.EXTRA_TOAST_SIZE, LoggingService.SIZE_30);
		mPreferenceLoc = preference.getInt(LoggingService.EXTRA_TOAST_LOCATION, Gravity.BOTTOM);
		applyPreference();
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePreference();	//save preferences.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menu1 = menu.add(Menu.NONE, R.string.jump_to_error, Menu.NONE, R.string.jump_to_error);
		MenuItem menu2 = menu.add(Menu.NONE, R.string.save_as, Menu.NONE, R.string.save_as);
		MenuItem menu3 = menu.add(Menu.NONE, R.string.send_to, Menu.NONE, R.string.send_to);
		menu1.setIcon(android.R.drawable.ic_menu_revert);
		menu2.setIcon(android.R.drawable.ic_menu_save);
		menu3.setIcon(android.R.drawable.ic_menu_send);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mLogAdapter.isEmpty() == true){
			//if no log to save or send, disable menu items.
			menu.getItem(0).setEnabled(false);	//send to
			menu.getItem(1).setEnabled(false);	//save as
			menu.getItem(2).setEnabled(false);	//save as
		}else{
			menu.getItem(0).setEnabled(true);	//send to
			menu.getItem(1).setEnabled(true);	//save as
			menu.getItem(2).setEnabled(true);	//save as
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.string.send_to:
			mSaveLogMode = SAVE_FULL_LOG;
			sendLogFile();
			break;
		case R.string.save_as:
			mSaveLogMode = SAVE_FULL_LOG;
			showDialog(DIALOG_SAVE_LOG);
			break;
		case R.string.jump_to_error:
			mSaveLogMode = SAVE_FULL_LOG;
			jumpToError();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new Builder(this);
		switch(id){
		case DIALOG_SHOW_LOG:
			return builder.setTitle("LOG")
						  .setMessage("...")
						  .setPositiveButton(R.string.send_to, this)
						  .setNegativeButton(R.string.save_as, this)
						  .create();
		case DIALOG_SAVE_LOG:
			return builder.setTitle("LOG")
						  .setMessage("")
						  .setNeutralButton("OK", this)
						  .create();
		case DIALOG_TOAST_LOCATION:
			mLocationDialog = builder.setTitle(R.string.toats_location)
									 .setSingleChoiceItems(new CharSequence[]{
											 getString(R.string.top),
											 getString(R.string.center),
											 getString(R.string.bottom)}, -1, this)
									 .create();
			return mLocationDialog;
		case DIALOG_TOAST_SIZE:
			mSizeDialog = builder.setTitle(R.string.toats_size)
								 .setSingleChoiceItems(new CharSequence[]{
										 getString(R.string._30_),
										 getString(R.string._50_),
										 getString(R.string._100_)}, -1, this)
								 .create();
			return mSizeDialog;
		case DIALOG_APPLY_FILTER:
			LayoutInflater i = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			mFilterDialog = builder.setTitle("Filter Setting")
				.setView(i.inflate(R.layout.filter_setting, null))
				.setNeutralButton("Start", this)
				.create();
			return mFilterDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id){
		case DIALOG_SHOW_LOG:
			((AlertDialog)dialog).setMessage(mSelectedLog);
			break;
		case DIALOG_SAVE_LOG:
			mLogFile = LogUtils.createLogFile("log");
			if(mLogFile != null){
				((AlertDialog)dialog).setIcon(android.R.drawable.ic_menu_save);
				((AlertDialog)dialog).setTitle(getString(R.string.save_as));
				((AlertDialog)dialog).setMessage(mLogFile.getAbsolutePath());
			}else{
				((AlertDialog)dialog).setIcon(android.R.drawable.ic_menu_delete);
				((AlertDialog)dialog).setTitle("Caution");
				((AlertDialog)dialog).setMessage(getString(R.string.fail_to_create_a_file));
			}
			break;
		case DIALOG_APPLY_FILTER:
			//set preference values.
			AlertDialog ad = ((AlertDialog)dialog);
			EditText et = (EditText) ad.findViewById(R.id.log_tag);
			Spinner sp = (Spinner) ad.findViewById(R.id.select_priority1);
			CheckBox cb = (CheckBox) ad.findViewById(R.id.log_all);
			et.setText(mPreferenceFilter);
			cb.setChecked(mPreferenceShowAll);
			mPrioirties = getResources().getStringArray(R.array.log_level);
			for(int i = 0; i < mPrioirties.length; ++i){
				if(mPrioirties[i].startsWith(mPreferencePriority) == true){
					sp.setSelection(i);
					break;
				}
			}
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	public void onLowMemory() {
		Log.w("LogToaster", "Log Memory. reduce keeped log histroy as half.");
		synchronized (mLogAdapter) {
			int size = mLogAdapter.getCount() / 2;
			for(int i = 0; i < size; ++ size)
				mLogAdapter.remove(mLogAdapter.getItem(0));
		}
		super.onLowMemory();
	}

	public void handleClick(View v){

		switch(v.getId()){
		case R.id.start_log:
			if(mLoggingServiceEnable == true){
				stopService(new Intent(this, LoggingService.class));
			}else{
				startLogging();
			}
			break;
		case R.id.input_tag:
			showDialog(DIALOG_APPLY_FILTER);
			break;
		case R.id.toast_setting_location:
			showDialog(DIALOG_TOAST_LOCATION);
			break;
		case R.id.toast_setting_size:
			showDialog(DIALOG_TOAST_SIZE);
			break;
		case R.id.toast_setting_visibility:
			toggleToastVisibility();
			break;
		}
	}

	private void startLogging() {
		mLogAdapter.clear();
		Intent i = new Intent(this, LoggingService.class);
		i.putExtra(LoggingService.EXTRA_FILTER_TAG, mPreferenceFilter);
		i.putExtra(LoggingService.EXTRA_FILTER_PRIORITY, mPreferencePriority);
		i.putExtra(LoggingService.EXTRA_SHOW_ALL_LOG, mPreferenceShowAll);
		i.putExtra(LoggingService.EXTRA_TOAST_VISIBILITY, mPreferenceVisibility);
		i.putExtra(LoggingService.EXTRA_TOAST_LOCATION, mPreferenceLoc);
		i.putExtra(LoggingService.EXTRA_TOAST_SIZE, mPreferenceSize);
		bindService(i, serviceConn, 0);
		startService(i);
	}

	Handler handler = new Handler(){
		private long lastUpdateTime = 0;
		@Override
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case NEW_MESSAGE:
				this.sendEmptyMessage(SHOW_MESSAGE);
				break;
			case SHOW_MESSAGE:
				long interval = System.currentTimeMillis() - lastUpdateTime;
				if(interval > MIN_UPDATE_INTERVAL){
					synchronized (mLogBuffer) {
						synchronized (mLogAdapter) {
							for(String str : mLogBuffer){
								LogUtils.appendLog(str, mLogAdapter);
							}
						}
						mLogBuffer.clear();				
					}
					if(mLogViewLocked == false)
						mLogView.setSelection(mLogAdapter.getCount());
					lastUpdateTime = System.currentTimeMillis();
				}else{
					this.removeMessages(SHOW_MESSAGE);
					this.sendEmptyMessageDelayed(SHOW_MESSAGE, interval);
				}
				break;
			}
		};
	};	


	ArrayList<String> mLogBuffer = new ArrayList<String>(100);
	@Override
	public void onNewLine(String log) {
		synchronized (mLogBuffer) {
			mLogBuffer.add(log);
		}
		handler.sendEmptyMessage(NEW_MESSAGE);
	}

	@Override
	/**
	 * Dialog Click Listener
	 */
	public void onClick(DialogInterface dialog, int which) {

		if(dialog.equals(mSizeDialog) == true){
			switch(which){
			case 0:
				mPreferenceSize = LoggingService.SIZE_30;
				break;
			case 1:
				mPreferenceSize = LoggingService.SIZE_50;
				break;
			case 2:
				mPreferenceSize = LoggingService.SIZE_100;
				break;
			}
			mSizeDialog.cancel();
			applyPreference();			
		}else if(dialog.equals(mLocationDialog) == true){
			switch(which){
			case 0:
				mPreferenceLoc = Gravity.TOP;
				break;
			case 1:
				mPreferenceLoc = Gravity.CENTER;
				break;
			case 2:
				mPreferenceLoc = Gravity.BOTTOM;
				break;
			}
			mLocationDialog.cancel();
			applyPreference();			
		}else if(dialog.equals(mFilterDialog) == true){
			switch(which){
			case DialogInterface.BUTTON_NEUTRAL:
				AlertDialog ad = ((AlertDialog)dialog);
				EditText et = (EditText) ad.findViewById(R.id.log_tag);
				Spinner sp = (Spinner) ad.findViewById(R.id.select_priority1);
				CheckBox cb = (CheckBox) ad.findViewById(R.id.log_all);
				mPreferenceFilter = et.getText().toString().trim();
				mPreferenceShowAll = cb.isChecked();
				mPreferencePriority = ((String)sp.getSelectedItem()).substring(0,1);
				//start a logging.
				applyPreference();
				startLogging();
				break;
			}
		}else{
			//save one log dialog or save full log dialog.
			switch(which){
			case DialogInterface.BUTTON_POSITIVE:
				//send to...
				sendLogFile();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				//save as...
				showDialog(DIALOG_SAVE_LOG);
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				//confirm file save
				switch(mSaveLogMode){
				case SAVE_FULL_LOG:
					LogUtils.saveLogFile(mLogFile, mLogAdapter);
					break;
				case SAVE_PARTIAL_LOG:
					LogUtils.saveLogFile(mLogFile, mSelectedLog);
					break;
				}			
				break;
			}
		}
	}

	void sendLogFile(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);

		switch(mSaveLogMode){
		case SAVE_FULL_LOG:
			mTempLogFile = LogUtils.createTempLogFile();
			if(mTempLogFile != null && LogUtils.saveLogFile(mTempLogFile, mLogAdapter) == true){
				Uri uri = Uri.fromFile(mTempLogFile);
				intent.setType("application/Octet-Stream");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
			}
			break;
		case SAVE_PARTIAL_LOG:
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, mSelectedLog);
			break;
		}
		startActivity(intent);
	}

	void loggingServiceStarted(){
		mLoggingServiceEnable = true;
		mStartButton.setChecked(mLoggingServiceEnable);
		setTitle(R.string.log_toaster_running);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.checkbox_on_background);
	}

	void loggingServiceStopped(){
		mLoggingServiceEnable = false;
		mStartButton.setChecked(mLoggingServiceEnable);
		setTitle(R.string.log_toaster_stopped);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.checkbox_off_background);
	}
	
	void applyPreference(){
		mInputTag.setText(mPreferenceFilter);
		mToastVisibility.setChecked(mPreferenceVisibility);

		switch(mPreferenceLoc){
		case Gravity.TOP:
			mToastLocation.setText(R.string.top);
			break;
		case Gravity.CENTER:
			mToastLocation.setText(R.string.center);
			break;
		case Gravity.BOTTOM:
			mToastLocation.setText(R.string.bottom);
			break;
		}

		switch(mPreferenceSize){
		case LoggingService.SIZE_100:
			mToastSize.setText(R.string._100_);
			break;
		case LoggingService.SIZE_50:
			mToastSize.setText(R.string._50_);
			break;
		case LoggingService.SIZE_30:
			mToastSize.setText(R.string._30_);
			break;
		}
		
		if(mLoggingServiceEnable == true && mLoggingService != null){
			mLoggingService.setToastSize(mPreferenceSize);
			mLoggingService.setToastGravity(mPreferenceLoc);
			mLoggingService.updateToast();
		}		
	}
	
	void savePreference(){
		SharedPreferences preference = getSharedPreferences("setting", 0);

		Editor edit = preference.edit();
		edit.putString(LoggingService.EXTRA_FILTER_TAG, mPreferenceFilter);
		edit.putString(LoggingService.EXTRA_FILTER_PRIORITY, mPreferencePriority);
		edit.putBoolean(LoggingService.EXTRA_SHOW_ALL_LOG, mPreferenceShowAll);
		edit.putBoolean(LoggingService.EXTRA_TOAST_VISIBILITY, mPreferenceVisibility);
		edit.putInt(LoggingService.EXTRA_TOAST_SIZE, mPreferenceSize);
		edit.putInt(LoggingService.EXTRA_TOAST_LOCATION, mPreferenceLoc);
		edit.commit();		
	}
	
	void toggleToastVisibility(){
		mPreferenceVisibility = !mPreferenceVisibility;
		mToastVisibility.setChecked(mPreferenceVisibility);

		if(mLoggingServiceEnable == true && mLoggingService != null){
			if(mPreferenceVisibility == true)
				mLoggingService.showToast();
			else
				mLoggingService.hideToast();
		}
	}
	
	/**
	 * find a error log in the list, and setSelection of log list to that item.
	 */
	void jumpToError() {
		if(mLogView == null)
			return;
		synchronized(mLogAdapter){
			int currPos = mLogView.getFirstVisiblePosition();
			for(int i = currPos; i >= 0; --i){
				if(i == currPos)
					continue;
				if(	LogUtils.parseLogLevel(mLogAdapter.getItem(i)) == Log.ERROR ){
					mLogView.setSelection(i);
					mLogViewLocked = true;
					return;
				}
			}
			for(int i = currPos; i < mLogAdapter.getCount(); ++i){
				if(i == currPos)
					continue;
				if(	LogUtils.parseLogLevel(mLogAdapter.getItem(i)) == Log.ERROR ){
					mLogView.setSelection(i);
					mLogViewLocked = true;
					return;
				}
			}
			
			//no error.
		}
	}	
	
	//inner constant values//
	protected static final int NEW_MESSAGE = 1001;
	protected static final int SHOW_MESSAGE = 1002;
	protected static final int DIALOG_SHOW_LOG = 2001;
	protected static final int DIALOG_SAVE_LOG = 2002;
	protected static final int DIALOG_TOAST_SIZE= 2003;
	protected static final int DIALOG_TOAST_LOCATION = 2004;
	protected static final int DIALOG_APPLY_FILTER = 2005;
	protected static final int SAVE_FULL_LOG = 3001;
	protected static final int SAVE_PARTIAL_LOG = 3002;
	protected static final long MIN_UPDATE_INTERVAL = 500;

	//private members//
	private TabHost mTabHost = null;
	private LoggingService mLoggingService = null;
	private boolean mLoggingServiceEnable = false;
	private ListView mLogView = null;
	private boolean mLogViewLocked = false;
	private int mSaveLogMode = SAVE_FULL_LOG;
	private String mSelectedLog = "";
	private LogAdapter mLogAdapter = null;
	private EditText mInputTag = null;
	private ToggleButton mStartButton = null;
	private File mLogFile = null;
	private File mTempLogFile = null;
	private ToggleButton mToastVisibility = null;
	private Button mToastLocation = null;
	private Button mToastSize = null;
	private AlertDialog mSizeDialog = null;
	private AlertDialog mLocationDialog = null;
	private AlertDialog mFilterDialog = null;
	private String mPreferenceFilter = "";
	private boolean mPreferenceVisibility = false;
	private int mPreferenceSize = 3;
	private int mPreferenceLoc = Gravity.BOTTOM;
	private String mPreferencePriority = "D";
	private boolean mPreferenceShowAll = false;
	private String[] mPrioirties = null;

	private ServiceConnection serviceConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//logging service started.
			LocalBinder binder = (LocalBinder)service;
			mLoggingService = binder.getService();
			mLoggingService.setOnNewLogListener(LogReader.this);
			loggingServiceStarted();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//logging service stopped.
			if(mLoggingService != null){
				mLoggingService.setOnNewLogListener(null);
				mLoggingService = null;
			}
			Log.i("LogToaster", "service unbinded");			
			loggingServiceStopped();
		}
	};//end of inner class	
}//end of class
