package com.huewu.example.logreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.huewu.apps.logtoaster.R;
import com.huewu.example.logreader.LoggingService.LocalBinder;

/**
 * <p>
 * @file			LogRemocon.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 *
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

public class LogRemocon extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener{

	private final static int LOG_REMOCON = 1001;
	private LoggingService mLoggingService = null;
	private ServiceConnection mServiceConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder)service;
			mLoggingService = binder.getService();
			Log.i("LogToaster", "service binded");			
			showDialog(LOG_REMOCON);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mLoggingService = null;
			Log.i("LogToaster", "service unbinded");			
		}
	};//end of inner class

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}	

	@Override
	protected void onResume() {
		super.onResume();

		if(mLoggingService == null)
			this.bindService(new Intent(this, LoggingService.class), mServiceConn, 0);
		else
			showDialog(LOG_REMOCON);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try{
			this.unbindService(mServiceConn);	
		}catch(Exception e){

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case LOG_REMOCON:
			LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			AlertDialog.Builder builder = new Builder(this);
			AlertDialog dialog = builder.setTitle(R.string.rc_title)
				.setPositiveButton(R.string.show_toast, this)
				.setNeutralButton(R.string.launch_viewer, this)
				.setNegativeButton(R.string.stop_service, this)
				.setView(li.inflate(R.layout.remocon, null))
				.setOnCancelListener(this)
				.create();
			return  dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		Button btn = (Button) dialog.findViewById(android.R.id.button1);

		if(mLoggingService.getToastVisibility() == true)
			btn.setText(R.string.hide_toast);
		else
			btn.setText(R.string.show_toast);
		super.onPrepareDialog(id, dialog);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which){
		case Dialog.BUTTON_POSITIVE:
			//pause or resume toasting
			if(mLoggingService.getToastVisibility() == true)
				mLoggingService.hideToast();
			else
				mLoggingService.showToast();
			finish();
			break;
		case Dialog.BUTTON_NEUTRAL:
			//open setting activity
			startActivity(new Intent(this, LogReader.class));
			finish();
			break;
		case Dialog.BUTTON_NEGATIVE:
			//stop service. terminate an application.
			stopService(new Intent(this, LoggingService.class));
			finish();
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}
	
	//when a user click a banner image.
	public void handleClick(View v){
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SENDTO);
		i.setData(Uri.parse("mailto:chansuk.yang@gmail.com"));
//		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"chansuk.yang@gmail.com"});
//		i.putExtra(Intent.EXTRA_TEXT, "I have a question about log toaster...\n");
//		i.setType("text/plain");
		startActivity(i);
	}
}//end of class
