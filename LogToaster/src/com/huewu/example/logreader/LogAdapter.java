package com.huewu.example.logreader;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.huewu.apps.logtoaster.R;

/**
 * <p>
 * @file			LogAdapter.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 * <br>
 * Extend ArrayAdater. Convert log string to log_listitem layout.
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

public class LogAdapter extends ArrayAdapter<String>{
	LayoutInflater mInflater = null;

	public LogAdapter(Context context) {
		super(context, R.layout.log_listitem);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View resultView = null;
		if(convertView != null)
			resultView = convertView;
		else{
			resultView = mInflater.inflate(R.layout.log_listitem, null);
			resultView.setTag(R.id.tag, resultView.findViewById(R.id.tag));
			resultView.setTag(R.id.priority, resultView.findViewById(R.id.priority));
			resultView.setTag(R.id.log, resultView.findViewById(R.id.log));
		}
		
		String rawLog = getItem(position);
		TextView tag = (TextView) resultView.getTag(R.id.tag);
		TextView priority = (TextView) resultView.getTag(R.id.priority);
		TextView log = (TextView) resultView.getTag(R.id.log);
		log.setPadding(30, 0, 0, 0);
		
		String[] tokens = LogUtils.parseTokens(rawLog);

		if(tokens.length < 3){
			priority.setText("");
			tag.setText("");
			priority.setBackgroundColor(Color.BLACK);
			tag.setBackgroundColor(Color.BLACK);
			log.setText(rawLog);
		}else{
			switch(tokens[0].charAt(0)){
			case 'V':
				priority.setBackgroundColor(Color.GRAY);
				tag.setBackgroundColor(Color.GRAY);
				break;
			case 'D':
				priority.setBackgroundColor(Color.argb(255, 40, 40, 200));
				tag.setBackgroundColor(Color.argb(255, 40, 40, 200));
				break;
			case 'I':
				priority.setBackgroundColor(Color.argb(255, 40, 200, 40));
				tag.setBackgroundColor(Color.argb(255, 40, 200, 40));
				break;
			case 'W':
				priority.setBackgroundColor(Color.argb(255, 200, 120, 0));
				tag.setBackgroundColor(Color.argb(255, 200, 120, 0));
				break;
			case 'E':
				priority.setBackgroundColor(Color.argb(255, 200, 40, 0));
				tag.setBackgroundColor(Color.argb(255, 200, 40, 0));
				break;
			}
			
			priority.setText(tokens[0]);
			tag.setText(tokens[1]);
			log.setText(tokens[2]);
		}

		return resultView;
	}
}//end of inner class