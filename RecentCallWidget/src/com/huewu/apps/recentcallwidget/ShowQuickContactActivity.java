package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ShowQuickContactActivity extends Activity{
	
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
		String number = getIntent().getStringExtra(CallLog.Calls.NUMBER);
		if(number == null){
			finish();
			return;
		}

		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor c = managedQuery(uri, null, null, null, null);
		String lookup = null;
		long id = 0L;
		while(c.moveToNext() == true){
			lookup = c.getString(c.getColumnIndex(PhoneLookup.LOOKUP_KEY));
			id = c.getLong(c.getColumnIndex(PhoneLookup._ID));
		}
		c.close();

		if(lookup == null){
			Uri createUri = Uri.fromParts("tel", number, null);
			Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, createUri);
			//create a temp contact?
            startActivity(intent);
		}else{
			lookupUri = ContactsContract.Contacts.getLookupUri(id, lookup);
			QuickContact.showQuickContact(this, tv,lookupUri, QuickContact.MODE_LARGE, null);
		}
		finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
}//end of class
