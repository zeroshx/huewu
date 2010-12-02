package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class ShowQuickContact extends Activity{
	
	Uri lookupUri = null;
	boolean finish = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tv.setBackgroundColor(Color.TRANSPARENT);
		setContentView(tv);

		//get content uri from call log.

		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode("2222222"));
		Cursor c = managedQuery(uri, null, null, null, null);
		String lookup = null;
		while(c.moveToNext() == true){
			lookup = c.getString(c.getColumnIndex(PhoneLookup.LOOKUP_KEY));
		}
		c.close();

		if(lookup == null){
			Uri createUri = Uri.fromParts("tel", "2222222", null);
			Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, createUri);
            startActivity(intent);
		}else{
			lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookup);
			QuickContact.showQuickContact(this, tv,lookupUri, QuickContact.MODE_LARGE, null);
		}
		finish();
	}
}//end of class
