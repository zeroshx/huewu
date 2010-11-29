package com.huewu.example.logreader;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huewu.apps.logtoaster.R;

/**
 * <p>
 * @file			LogView.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 *
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

public class LogView extends TextView{
	
	public LogView(Context context){
		super(context);
	}

	public LogView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//automatically align to bottom. (last log should be placed at bottom.)
		
		int offset = getLineCount() * getLineHeight();
		int height = getHeight();		
		int padding = getTotalPaddingTop();
		
		if(padding != height - offset)
			setPadding(10, height-offset, 10, 0);

		super.onDraw(canvas);
	}
}//end of class
