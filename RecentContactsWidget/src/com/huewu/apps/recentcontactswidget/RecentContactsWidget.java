package com.huewu.apps.recentcontactswidget;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * <p>
 * @file			RecentContactsWidget.java
 * @version			1.0
 * @date 			Dec. 5, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * Implement RecentCall Widget. 
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

public class RecentContactsWidget extends AppWidgetProvider {

	final static String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";	//for checking receiving sms message.
	final static String ACTION_UPDATE = "apps.huewu.recentcall.action.UPDATE";	//user click update button, or updating time is coming(once per 1 min)
	final static String ACTION_SCROLL_UP = "apps.huewu.recentcall.action.SCROLL_UP";
	final static String ACTION_SCROLL_DOWN = "apps.huewu.recentcall.action.SCROLL_DOWN";
	final static String ACTION_CALL_1 = "apps.huewu.recentcall.action.CALL_1";
	final static String ACTION_CALL_2 = "apps.huewu.recentcall.action.CALL_2";
	final static String ACTION_CALL_3 = "apps.huewu.recentcall.action.CALL_3";
	final static String ACTION_SWITCH_MODE = "apps.huewu.recentcall.action.SWITCH_MODE";
	
	private static RecentContactsManager mWidgetManager = null;	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
	}
	
	@Override
	/**
	 * receive broadcast intent list.
	 * android.appwidget.action.APPWIDGET_UPDATE
	 * android.intent.action.PHONE_STATE	//for checking incoming / outcoming call.
	 * android.provider.Telephony.SMS_RECEIVED	//for checking receiving sms message.
	 * apps.huewu.recentcall.action.UPDATE	//user click update button, or updating time is coming(once per 1 min)
	 * apps.huewu.recentcall.action.SCROLL_UP	//user click scroll up button
	 * apps.huewu.recentcall.action.SCROLL_DOWN	//user click scroll down button
	 */
	public void onReceive(Context context, Intent intent) {
		if(mWidgetManager == null)
			mWidgetManager = new RecentContactsManager(context);		
		super.onReceive(context, intent);
		
		RemoteViews stillviews = null;
		RemoteViews aniViews = null;
		long updateTime = 60000;	//defaul 1 min.
		
		String action = intent.getAction();
		if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) == true){
			//update recent call list.
			stillviews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_STILL_WIDGET);
		}else if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) == true){
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			//phone call is offhook, the recent contact list may be changed. let's check it.
			if(state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) == true){
				//update recent call list only after 5secs.
				updateTime = 5000;
				Log.i("RecentCallWidget", "Received Intent: " + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
			}else{
				//do nothing.				
				return;
			}
		}else if(action.equals(ACTION_SMS_RECEIVED) == true){
			//update recent call list.
			updateTime = 5000;
			//mark contacted.
			Bundle bundle = intent.getExtras();
			Object[] pdusObj = (Object[])bundle.get("pdus");
			mWidgetManager.markContacted(pdusObj);
		}else if(action.equals(ACTION_UPDATE) == true){
			//update recent call list.
			stillviews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_STILL_WIDGET);		
		}else if(action.equals(ACTION_SCROLL_UP) == true){
			//update recent call list.
			stillviews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_STILL_WIDGET);		
			aniViews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_UP_SCROLLING_WIDGET);
		}else if(action.equals(ACTION_SCROLL_DOWN) == true){
			//update recent call list.
			stillviews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_STILL_WIDGET);		
			aniViews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_DOWN_SCROLLING_WIDGET);
		}else if(action.equals(ACTION_SWITCH_MODE) == true){
			mWidgetManager.toggleMode();
			stillviews = mWidgetManager.makeRecentCallWidget(context, RecentContactsManager.MODE_STILL_WIDGET);		
		}else{
			//receive non-handled broadcast intent. do nothing.
			return;
		}
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName cpName = new ComponentName(context, RecentContactsWidget.class);
		
		if(stillviews != null)
			appWidgetManager.updateAppWidget(cpName, stillviews);		
		
		if(aniViews != null)	//need to animate views.
			appWidgetManager.updateAppWidget(cpName, aniViews);
		
		//set alarm. (using ELAPSED_REALTIME in order to avoid to wake up device) 
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ACTION_UPDATE);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + updateTime, pending);
	}
}//end of class
