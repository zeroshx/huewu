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

public class RecentCallWidget extends AppWidgetProvider {

	final static String ACTION_SCROLL_UP = "apps.huewu.recentcall.action.SCROLL_UP";
	final static String ACTION_SCROLL_DOWN = "apps.huewu.recentcall.action.SCROLL_DOWN";
	final static String ACTION_SENDTO = "apps.huewu.recentcall.action.SENDTO";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.call_list_still);

			Intent up = new Intent(ACTION_SCROLL_UP);
			Intent down = new Intent(ACTION_SCROLL_DOWN);

			PendingIntent pendingUp 
			= PendingIntent.getBroadcast(context, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent pendingDown 
			= PendingIntent.getBroadcast(context, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);

			views.setOnClickPendingIntent(R.id.scrollUp, pendingUp);
			views.setOnClickPendingIntent(R.id.scrollDown, pendingDown);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}				
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if(intent == null)
			return;

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews views = null;
		ComponentName cpName = new ComponentName(context, RecentCallWidget.class);
		if( intent.getAction().equals(ACTION_SCROLL_UP) == true ){
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_up);
		}else if( intent.getAction().equals(ACTION_SCROLL_DOWN) == true ){
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_down);
		}else{
			return;
		}
		
		Intent up = new Intent(ACTION_SCROLL_UP);
		Intent down = new Intent(ACTION_SCROLL_DOWN);	
		Intent contact = new Intent(context, ShowQuickContact.class);

		PendingIntent pendingUp 
			= PendingIntent.getBroadcast(context, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pendingDown 
			= PendingIntent.getBroadcast(context, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pendingContact
			= PendingIntent.getActivity(context, 0, contact, PendingIntent.FLAG_CANCEL_CURRENT);
		

		views.setOnClickPendingIntent(R.id.scrollUp, pendingUp);
		views.setOnClickPendingIntent(R.id.scrollDown, pendingDown);
		views.setOnClickPendingIntent(R.id.call1, pendingContact);
		views.setOnClickPendingIntent(R.id.call2, pendingContact);
		views.setOnClickPendingIntent(R.id.call3, pendingContact);
		views.setOnClickPendingIntent(R.id.call4, pendingContact);
		views.setOnClickPendingIntent(R.id.call5, pendingContact);

		for(int id : appWidgetManager.getAppWidgetIds(cpName) ){
			RemoteViews still = new RemoteViews(context.getPackageName(), R.layout.call_list_still);
			appWidgetManager.updateAppWidget(id, still);
			appWidgetManager.updateAppWidget(id, views);
		}
	}
}//end of class
