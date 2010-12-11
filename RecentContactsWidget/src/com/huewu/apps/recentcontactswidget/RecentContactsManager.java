package com.huewu.apps.recentcontactswidget;

import java.util.ArrayList;
import java.util.List;

import com.huewu.apps.recentcontactswidget.R;
import com.huewu.apps.recentcontactswidget.RecentContactsManager.SimpleContact;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * <p>
 * @file			RecentCallWidgetManager.java
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

public class RecentContactsManager {

	final static int MODE_STILL_WIDGET = 1001;
	final static int MODE_UP_SCROLLING_WIDGET = 1002;
	final static int MODE_DOWN_SCROLLING_WIDGET = 1003;
	final static int MAX_CONTACTS_COUNT = 10;
	final static int DISPLAY_CONTACTS_COUNT = 3;
	final static int LIST_CONTACTS_COUNT = 5;	//include 2 padding(for animation) list.
	
	final static int SCROLL_UP = 2001;
	final static int SCROLL_DOWN = 2002;
	final static int SCROLL_NO = 2003;

	public RecentContactsManager(Context context){
		mAppContext = context.getApplicationContext();
		mContactsManager = new ContactsManager();
		
		//load current index.
		SharedPreferences preference = mAppContext.getSharedPreferences(WIDGET_PREFERENCE, 0);
		mIndex = preference.getInt(CURRENT_INDEX, 0);
	}

	/**
	 * create a recent call widget remote view.
	 * still widget - (not move widget)
	 * scroll up widget - (scroll up animation widget)
	 * scroll down widget - (scroll down animation widget)
	 * @param context
	 * @param mode one of values : MODE_STILL_WIDGET / MODE_UP_SCROLLING_WIDGET / MODE_DOWN_SCROLLING_WIDGET
	 * @return created remote view.
	 */
	public RemoteViews makeRecentCallWidget(Context context, int mode){

		RemoteViews views = null;
		//#1. get recent call logs.
		 SimpleContact[] contacts = mContactsManager.getRecentContactDisplayName(MAX_CONTACTS_COUNT);
		
		if(contacts.length - DISPLAY_CONTACTS_COUNT < mIndex)
			mIndex = 0;	//reset index.

		//#2. build a proper remote view.
		switch(mode){
		case MODE_STILL_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_still);
			applyScroll(views, contacts, SCROLL_NO);
			break;
		case MODE_UP_SCROLLING_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_up);
			applyScroll(views, contacts, SCROLL_UP);
			break;
		case MODE_DOWN_SCROLLING_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_down);
			applyScroll(views, contacts, SCROLL_DOWN);
			break;
		}

		//#3. set texts.
		setCallListTexts(views, contacts);
		return views;
	}
	
	public void markContacted(Object[] pdusObj) {
		if(pdusObj == null)
			return;
		
		for(Object pdu : pdusObj){
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
			if(sms != null)
				mContactsManager.markContacted(sms.getOriginatingAddress());
		}
	}

	private void applyScroll(RemoteViews views, SimpleContact[] contacts, int move) {

		switch(move){
		case SCROLL_NO:
//			views.setViewVisibility(mCallItemList[0], View.INVISIBLE);	//hide		
//			views.setViewVisibility(mCallItemList[4], View.INVISIBLE);	//hide		
			break;
		case SCROLL_UP:
			if(mIndex < contacts.length - 3){
				++mIndex;
				try{
					views.setTextViewText(mCallItemList[0], contacts[mIndex-1].mDisplayName);	//hide		
				}catch(Exception e){
				}				
			}
			break;
		case SCROLL_DOWN:
			if(mIndex > 0){
				--mIndex;
				try{
					views.setTextViewText(mCallItemList[4], contacts[mIndex+3].mDisplayName);	//hide		
				}catch(Exception e){
				}
			}
			break;
		}

		if(mIndex > 0){
			//scroll down is possible.
			Intent down = new Intent(RecentContactsWidget.ACTION_SCROLL_DOWN);
			PendingIntent pendingDown 
				= PendingIntent.getBroadcast(mAppContext, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollDown, pendingDown);
		}
		
		if(mIndex < contacts.length - 3){
			//scroll up is possible.
			Intent up = new Intent(RecentContactsWidget.ACTION_SCROLL_UP);			
			PendingIntent pendingUp 
				= PendingIntent.getBroadcast(mAppContext, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollUp, pendingUp);
		}
		
		//save current index.
		SharedPreferences preference = mAppContext.getSharedPreferences(WIDGET_PREFERENCE, 0);
		Editor editor = preference.edit();
		editor.putInt(CURRENT_INDEX, mIndex);
		editor.commit();
	}

	private void setCallListTexts(RemoteViews views, SimpleContact[] contacts) {
		if(views == null)
			return;

		int count = ( (contacts.length - mIndex) < DISPLAY_CONTACTS_COUNT ) ? (contacts.length - mIndex) : DISPLAY_CONTACTS_COUNT;

		int index = 0;
		for(index = 0; index <count; ++index){
			String name = contacts[mIndex + index].mDisplayName;
			Uri uri = contacts[mIndex + index].mUri;
			views.setTextViewText(mCallItemList[index + 1], name);	
			Intent intent = new Intent(mCallIntentList[index]);
			intent.setData(uri);
			intent.putExtra(ContactsContract.Contacts.DISPLAY_NAME, name);
			PendingIntent pendingCall = PendingIntent.getActivity(mAppContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(mCallItemList[index + 1], pendingCall);			
		}

//		for(index = index; index < mCallItemList.length - 2; ++ index){
//			views.setViewVisibility(mCallItemList[index + 1], View.INVISIBLE);	//hide
//		}
	}
	
	
	class SimpleContact {
		String mDisplayName;
		Uri mUri;
		public SimpleContact(String name, Uri uri){
			mDisplayName = name;
			mUri = uri;
		}
	}

	class ContactsManager {

		public SimpleContact[] getRecentContactDisplayName(int count){
			
			Cursor c = mAppContext.getContentResolver().query(
					ContactsContract.Contacts.CONTENT_URI, 
					new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.LOOKUP_KEY}, 
					null, 
					null, 
					ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC");

			if(c == null){
				return new SimpleContact[0];
			}

			int ct = 0;
			int listSize = (count < c.getCount()) ? count : c.getCount();

			SimpleContact[] result = new SimpleContact[listSize];
			while(c.moveToNext() == true){
				//Log.i("RecentCallWidget", "Name:" + c.getString(idx1) + " Value: " + c.getString(idx2));
				long id = c.getLong(0);
				String name = c.getString(1);
				String lookup = c.getString(2);
				Uri uri = ContactsContract.Contacts.getLookupUri(id, lookup);
				SimpleContact contact = new SimpleContact(name, uri);
				result[ct] = contact;
				++ct;
				if(ct == listSize)	//fill list.
					break;
			}
			c.close();

			return result;
		}

		public void markContacted(String originatingAddress) {
			Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(originatingAddress));
			Cursor c = mAppContext.getContentResolver().query(
					uri, null, null, null, null);
			while(c.moveToNext() == true){
				long id = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
				ContactsContract.Contacts.markAsContacted(mAppContext.getContentResolver(), id);
			}
			c.close();
		}
	}//end of inner class	

	private static String WIDGET_PREFERENCE = "setting";
	private static String CURRENT_INDEX = "index";
	
	
	private Context mAppContext = null;
	private ContactsManager mContactsManager = null;
	private int[] mCallItemList = new int[]{R.id.call1, R.id.call2, R.id.call3, R.id.call4, R.id.call5};
	private String[] mCallIntentList = new String[]{RecentContactsWidget.ACTION_CALL_1, RecentContactsWidget.ACTION_CALL_2, RecentContactsWidget.ACTION_CALL_3};
	private int mIndex = 0;

}//end of class
