package com.huewu.apps.recentcontactswidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.view.View;

/**
 * <p>
 * @file			ShowQuickContactActivity.java
 * @version			1.1
 * @date 			Dec. 5, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * Transparent Activity. Only for launch QuickContact.
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

public class ShowQuickContactActivity extends Activity{
	
	Uri mLookupUri = null;
	View mView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_quick_contact);
		mView = findViewById(R.id.view1);
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
			//i don't want to any exception or error annoy user.
		}catch(Error e){
			//i don't want to any exception or error annoy user.
		}
		finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
}//end of class
