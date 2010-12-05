package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

/**
 * <p>
 * @file			RecentCallWidget.java
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

public class RecentCallWidget extends AppWidgetProvider {

	final static String ACTION_SCROLL_UP = "apps.huewu.recentcall.action.SCROLL_UP";
	final static String ACTION_SCROLL_DOWN = "apps.huewu.recentcall.action.SCROLL_DOWN";
	final static String ACTION_CALL_1 = "apps.huewu.recentcall.action.CALL_1";
	final static String ACTION_CALL_2 = "apps.huewu.recentcall.action.CALL_2";
	final static String ACTION_CALL_3 = "apps.huewu.recentcall.action.CALL_3";
	
	private static RecentCallWidgetManager mWidgetManager = null;	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		//RecentCallWidgetManager can be finalized at any time(when this process is killed) 
		if(mWidgetManager == null)
			mWidgetManager = new RecentCallWidgetManager(context);
		
		RemoteViews still = mWidgetManager.makeRecentCallWidget(context, RecentCallWidgetManager.MODE_STILL_WIDGET);		

		appWidgetManager.updateAppWidget(appWidgetIds, still);		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if( intent.getAction().equals(ACTION_SCROLL_UP) != true && 
				intent.getAction().equals(ACTION_SCROLL_DOWN) != true)
		return;
		
		if(mWidgetManager == null)
			mWidgetManager = new RecentCallWidgetManager(context);		
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName cpName = new ComponentName(context, RecentCallWidget.class);
		
		RemoteViews views = null;
		views = mWidgetManager.makeRecentCallWidget(context, RecentCallWidgetManager.MODE_STILL_WIDGET);
		appWidgetManager.updateAppWidget(cpName, views);		

		if( intent.getAction().equals(ACTION_SCROLL_UP) == true ){
			views = mWidgetManager.makeRecentCallWidget(context, RecentCallWidgetManager.MODE_UP_SCROLLING_WIDGET);
		}else{
			views = mWidgetManager.makeRecentCallWidget(context, RecentCallWidgetManager.MODE_DOWN_SCROLLING_WIDGET);
		}
		
		appWidgetManager.updateAppWidget(cpName, views);		
	}
}//end of class
