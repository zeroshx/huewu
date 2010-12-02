package com.huewu.apps.recentcallwidget;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.widget.TextView;

public class ShowQuickContact extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		setContentView(tv);

		Uri uri = Uri.parse("content://com.android.contacts/contacts/lookup/2508i1/7");
		QuickContact.showQuickContact(this, tv,uri, QuickContact.MODE_LARGE, null);
	}
}//end of class
