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
		Uri data = getIntent().getData();
		if(data == null){
			finish();
			return;
		}
		
		try{
			QuickContact.showQuickContact(this, tv, data, QuickContact.MODE_LARGE, null);
		}catch(Exception e){
		}
		finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
}//end of class
