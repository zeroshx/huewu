package com.huewu.apps.tabtabword.graphic;


import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

class MeaningTexture {
	
	private static Random mRandom = new Random();
	Bitmap mBitmap = null;
	Paint mPaint = null;
	
	MeaningTexture(String word, Rect mRegion) {
		
		int width = mRegion.right / 2;
		
		word = word.trim();
		mPaint = new Paint();
		mPaint.setTypeface(Typeface.SANS_SERIF);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setColor(0xffffffff);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(20.0f);
		Bitmap bitmap = Bitmap.createBitmap(width, 50, Config.ARGB_8888); 
		Canvas c = new Canvas(bitmap);
		c.drawARGB(150, 50, 50, 50);
		c.drawText(word, width / 2, 35, mPaint);
		mBitmap = bitmap;
		mPaint = new Paint();
		mPaint.setAlpha(255);
		mPaint.setAntiAlias(false);
	}
	
	public Bitmap getBitmap(){
		return mBitmap;
	}

	public Paint getPaint() {
		return mPaint;
	}
}//end of class
