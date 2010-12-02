package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.QuickContact;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class RecentCallWidgetSetting extends Activity{
	
	int mAppWidgetId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void handleClick(View v){
		
		switch(v.getId()){
		case R.id.start:
			LinearLayout layout = (LinearLayout) findViewById(R.id.list);
			layout.startLayoutAnimation();
			break;
		case R.id.register:
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
			    mAppWidgetId = extras.getInt(
			            AppWidgetManager.EXTRA_APPWIDGET_ID, 
			            AppWidgetManager.INVALID_APPWIDGET_ID);
			    
			    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			    RemoteViews views = new RemoteViews(getPackageName(), R.layout.call_list_still);
			    
				Intent up = new Intent(RecentCallWidget.ACTION_SCROLL_UP);
				Intent down = new Intent(RecentCallWidget.ACTION_SCROLL_DOWN);
				
				PendingIntent pendingUp 
					= PendingIntent.getBroadcast(this, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
				PendingIntent pendingDown 
					= PendingIntent.getBroadcast(this, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);
				
				views.setOnClickPendingIntent(R.id.scrollUp, pendingUp);
				views.setOnClickPendingIntent(R.id.scrollDown, pendingDown);
			    appWidgetManager.updateAppWidget(mAppWidgetId, views);
			    
			    Intent resultValue = new Intent();
			    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			    setResult(RESULT_OK, resultValue);
			    finish();
			}
			break;
		}
	}

}; //end of class
