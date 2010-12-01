package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

public class RecentCallWidgetSetting extends Activity{
	
	int mAppWidgetId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void handleClick(View v){
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		    
		    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		    RemoteViews views = new RemoteViews(getPackageName(), R.layout.call_list);
		    appWidgetManager.updateAppWidget(mAppWidgetId, views);
		    
		    Intent resultValue = new Intent();
		    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		    setResult(RESULT_OK, resultValue);
		    finish();		    
		}		
	}

}; //end of class
