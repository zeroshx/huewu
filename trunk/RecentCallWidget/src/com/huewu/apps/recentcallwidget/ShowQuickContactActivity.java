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
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ShowQuickContactActivity extends Activity{
	
	Uri mLookupUri = null;
	View mView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new View(this);
		setContentView(mView);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus == false)
			return;
		
		//get content uri from call log.
		Uri data = getIntent().getData();
		if(data == null){
			finish();
			return;
		}
		
		try{
			QuickContact.showQuickContact(this, mView, data, QuickContact.MODE_LARGE, null);
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
