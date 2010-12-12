package com.huewu.apps.recentcontactswidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.widget.RemoteViews;

/**
 * <p>
 * @file			RecentContactstManager.java
 * @version			1.1
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
	final static int MAX_CONTACTS_COUNT = 10;	//max number of list, which can browse by recent contacts widget.
	final static int DISPLAY_CONTACTS_COUNT = 3;	//number of list, which an user actually see and select.
	final static int LIST_CONTACTS_COUNT = 5;	//DISPLAY_CONTACTS_COUNT + padding(for animation effect) list.
	
	final static int SCROLL_UP = 2001;
	final static int SCROLL_DOWN = 2002;
	final static int SCROLL_NO = 2003;

	/**
	 * public constructor.
	 * @param context
	 */
	public RecentContactsManager(Context context){
		mAppContext = context.getApplicationContext();
		mContactsManager = new ContactsManager();
		
		//load current list index. default value is 0.
		SharedPreferences preference = mAppContext.getSharedPreferences(WIDGET_PREFERENCE, 0);
		mIndex = preference.getInt(CURRENT_INDEX, 0);
	}

	/**
	 * create a recent call widget remote view.
	 * still widget - MODE_STILL_WIDGET (non-moving widget)
	 * scroll up widget - MODE_UP_SCROLLING_WIDGET - (scroll up animation widget)
	 * scroll down widget - MODE_DOWN_SCROLLING_WIDGET - (scroll down animation widget)
	 * @param context
	 * @param mode one of values : MODE_STILL_WIDGET / MODE_UP_SCROLLING_WIDGET / MODE_DOWN_SCROLLING_WIDGET
	 * @return created remote view.
	 */
	public RemoteViews makeRecentCallWidget(Context context, int mode){

		RemoteViews views = null;
		//#1. get recent call logs.
		 SimpleContact[] contacts = mContactsManager.getRecentContactDisplayName(MAX_CONTACTS_COUNT);
		 
		if(contacts.length - DISPLAY_CONTACTS_COUNT < mIndex)	//too few contacts to scroll in the list.
			mIndex = 0;	//reset index.

		//#3. build a proper remote view.
		switch(mode){
		case MODE_STILL_WIDGET:
			views = new RemoteViews(context.getPackageName(), R.layout.call_list_still);
			//#2. check is there any change?
			if(contacts.length > 0){
				if(contacts[0].mLastContactTime == mLastContacted){
					//no change.
					applyScroll(views, contacts, SCROLL_NO);
				}else{
					//new item is added. reset index.
					mIndex = 0;
					mLastContacted = contacts[0].mLastContactTime;
				}
			}			
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
	
	/**
	 * Android (at least in the nexus1) doesn't mark the contact as contacted when receiving sms, so do it manually.
	 * #1.convert pdusObj to SmsMessage.
	 * #2.mark the senders' contact(if it contains in the contacts) as contacted.
	 * @param pdusObj
	 */
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
		
		//scroll animation works as below.
		//#1.In the parent LinearLayout, there are five buttons which have exactly same height( !!using layout_weight attribute. )
		//#2.3 buttons are displaying buttons, the others are padding buttons.
		//#3.when we scroll up the list. ++ or -- starting index, and set text value.
		//#4.so, at the padding button, the text value of original index's button is cloned.
		//#5.start layout animation, which move up or down all buttons by 20% (cloned text is showing, so user can't notice).
		//#6.animation is playing and all the buttons position is moving to original position by y-axis. (so, the scrolling animation is showing!)

		switch(move){
		case SCROLL_NO:
			//do nothing.
			break;
		case SCROLL_UP:
			if(mIndex < contacts.length - DISPLAY_CONTACTS_COUNT){
				++mIndex;
				try{
					//clone text value.
					views.setTextViewText(mCallItemList[0], contacts[mIndex-1].mDisplayName);	//hide		
				}catch(Exception e){
				}				
			}
			break;
		case SCROLL_DOWN:
			if(mIndex > 0){
				--mIndex;
				try{
					//clone text value.
					views.setTextViewText(mCallItemList[4], contacts[mIndex+3].mDisplayName);	//hide		
				}catch(Exception e){
				}
			}
			break;
		}

		if(mIndex > 0){
			//scroll down is possible. set pending intent to the scroll button.
			Intent down = new Intent(RecentContactsWidget.ACTION_SCROLL_DOWN);
			PendingIntent pendingDown 
				= PendingIntent.getBroadcast(mAppContext, 0, down, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollUp, pendingDown);
		}
		
		if(mIndex < contacts.length - 3){
			//scroll up is possible. set pending intent to the scroll button.
			Intent up = new Intent(RecentContactsWidget.ACTION_SCROLL_UP);			
			PendingIntent pendingUp 
				= PendingIntent.getBroadcast(mAppContext, 0, up, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.scrollDown, pendingUp);
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

		//mIndex is starting index of displayed contact item list.
		int count = ( (contacts.length - mIndex) < DISPLAY_CONTACTS_COUNT ) ? (contacts.length - mIndex) : DISPLAY_CONTACTS_COUNT;

		int index = 0;
		for(index = 0; index <count; ++index){
			String name = contacts[mIndex + index].mDisplayName;
			Uri uri = contacts[mIndex + index].mUri;
			views.setTextViewText(mCallItemList[index + 1], name);
			//pending intent which has the same action name can't copied. so use different action name for each button.
			Intent intent = new Intent(mCallIntentList[index]);
			intent.setData(uri);
			intent.putExtra(ContactsContract.Contacts.DISPLAY_NAME, name);
			PendingIntent pendingCall = PendingIntent.getActivity(mAppContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(mCallItemList[index + 1], pendingCall);			
		}
	}
	
	/**
	 * simple data call which represents one specific ContactsContract.
	 */
	class SimpleContact {
		String mDisplayName;
		Uri mUri;
		long mLastContactTime;
		public SimpleContact(String name, Uri uri, long time){
			mDisplayName = name;
			mUri = uri;
			mLastContactTime = time;
		}
	}

	/**
	 * inner class which deals with ContactsContract Provider.
	 */
	class ContactsManager {

		SimpleContact[] getRecentContactDisplayName(int count){
			
			Cursor c = mAppContext.getContentResolver().query(
					ContactsContract.Contacts.CONTENT_URI, 
					new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, 
							ContactsContract.Contacts.LAST_TIME_CONTACTED, ContactsContract.Contacts.LOOKUP_KEY}, 
					null, 
					null, 
					ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC");	//sort by LAST_TIME_CONTACTED.

			if(c == null){
				//error happen.
				return new SimpleContact[0];
			}

			int ct = 0;
			//maximum list size is given integer count.
			int listSize = (count < c.getCount()) ? count : c.getCount();

			SimpleContact[] result = new SimpleContact[listSize];
			while(c.moveToNext() == true){
				//Log.i("RecentCallWidget", "Name:" + c.getString(idx1) + " Value: " + c.getString(idx2));
				long id = c.getLong(0);
				String name = c.getString(1);
				long time = c.getLong(2);
				String lookup = c.getString(3);
				Uri uri = ContactsContract.Contacts.getLookupUri(id, lookup);
				SimpleContact contact = new SimpleContact(name, uri, time);
				result[ct] = contact;
				++ct;
				if(ct == listSize)	//list is full.
					break;
			}
			c.close();

			return result;
		}

		void markContacted(String originatingAddress) {
			//#1. select ContactsContract information by using phone number.
			Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(originatingAddress));
			Cursor c = mAppContext.getContentResolver().query(
					uri, null, null, null, null);

			if(c == null)	//error happen
				return;
			
			while(c.moveToNext() == true){
				//only if the originatingAddress is stored in the contacts.
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
	private long mLastContacted = -1;

}//end of class
