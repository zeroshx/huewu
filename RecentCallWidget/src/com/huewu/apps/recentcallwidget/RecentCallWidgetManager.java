package com.huewu.apps.recentcallwidget;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CallLog;
import android.view.View;
import android.widget.RemoteViews;

/**
 * <p>
 * @file			WidgetManager.java
 * @version			1.0
 * @date 			Dec. 5, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * Helper class for creating widgets and for managing widget data as preferences.
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

public class RecentCallWidgetManager {

	final static int MODE_STILL_WIDGET = 1001;
	final static int MODE_UP_SCROLLING_WIDGET = 1002;
	final static int MODE_DOWN_SCROLLING_WIDGET = 1003;
	final static int MAX_CONTACTS_COUNT = 10;
	final static int DISPLAY_CONTACTS_COUNT = 3;
	final static int LIST_CONTACTS_COUNT = 5;	//include 2 padding(for animation) list.
	
	final static int SCROLL_UP = 2001;
	final static int SCROLL_DOWN = 2002;
	final static int SCROLL_NO = 2003;

	private Context mAppContext = null;
	private ContactsManager mContactsManager = null;
	private int[] mCallItemList = new int[]{R.id.call1, R.id.call2, R.id.call3, R.id.call4, R.id.call5};
	private String[] mCallIntentList = new String[]{RecentCallWidget.ACTION_CALL_1, RecentCallWidget.ACTION_CALL_2, RecentCallWidget.ACTION_CALL_3};
	private int mIndex = 0;

	public RecentCallWidgetManager(Context context){
		mAppContext = context.getApplicationContext();
		mContactsManager = new ContactsManager();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		//save preference?
	}

	public RemoteViews makeRecentCallWidget(Context context, int mode){

		RemoteViews views = null;
		String[] numbers = mContactsManager.getRecentContactNumbers(MAX_CONTACTS_COUNT);

		//#1.create a proper widget layout.
		switch(mode){
		case MODE_STILL_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_still);
			applyScroll(views, numbers, SCROLL_NO);
			break;
		case MODE_UP_SCROLLING_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_up);
			applyScroll(views, numbers, SCROLL_UP);
			break;
		case MODE_DOWN_SCROLLING_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_down);
			applyScroll(views, numbers, SCROLL_DOWN);
			break;
		}

		setCallListTexts(views, numbers);
		return views;
	}

	private void applyScroll(RemoteViews views, String[] numbers, int move) {

		switch(move){
		case SCROLL_NO:
			views.setViewVisibility(mCallItemList[0], View.INVISIBLE);	//hide		
			views.setViewVisibility(mCallItemList[4], View.INVISIBLE);	//hide		
			break;
		case SCROLL_UP:
			if(mIndex < numbers.length - 3)
				++mIndex;
			views.setTextViewText(mCallItemList[0], numbers[mIndex-1]);	//hide		
			break;
		case SCROLL_DOWN:
			if(mIndex > 0)
				--mIndex;
			views.setTextViewText(mCallItemList[4], numbers[mIndex+3]);	//hide		
			break;
		}

		if(mIndex != 0){
			//scroll down is possible.
			Intent down = new Intent(RecentCallWidget.ACTION_SCROLL_DOWN);
			PendingIntent pendingDown 
				= PendingIntent.getBroadcast(mAppContext, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollDown, pendingDown);
		}
		
		if(mIndex != numbers.length - 3){
			//scroll up is possible.
			Intent up = new Intent(RecentCallWidget.ACTION_SCROLL_UP);			
			PendingIntent pendingUp 
				= PendingIntent.getBroadcast(mAppContext, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollUp, pendingUp);
		}
	}

	private void setCallListTexts(RemoteViews views, String[] numbers) {
		if(views == null)
			return;

		int count = ( (numbers.length - mIndex) < DISPLAY_CONTACTS_COUNT ) ? (numbers.length - mIndex) : DISPLAY_CONTACTS_COUNT;

		int index = 0;
		for(index = 0; index <count; ++index){
			String number = numbers[mIndex + index];
			views.setTextViewText(mCallItemList[index + 1], number);	
			Intent intent = new Intent(mCallIntentList[index]);
			intent.putExtra(CallLog.Calls.NUMBER, number);
			PendingIntent pendingCall = PendingIntent.getActivity(mAppContext, 0, intent, 0);
			views.setOnClickPendingIntent(mCallItemList[index + 1], pendingCall);			
		}

		for(index = index; index < mCallItemList.length - 2; ++ index)
			views.setViewVisibility(mCallItemList[index + 1], View.INVISIBLE);	//hide
	}	

	public class ContactsManager {

		public String[] getRecentContactNumbers(int count){

			//#1. check phone call logs.
			List<String> numbers = new ArrayList<String>();
			Cursor c = mAppContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
			int index = c.getColumnIndex(CallLog.Calls.NUMBER);
			while(c.moveToNext() == true){
				numbers.add(c.getString(index));
			}
			c.close();

			count = ( count > numbers.size() ) ? numbers.size() : count;
			numbers = numbers.subList(0, count);

			String[] result = new String[numbers.size()];
			numbers.toArray(result);
			return result;
		}
	}//end of inner class	

}//end of class
