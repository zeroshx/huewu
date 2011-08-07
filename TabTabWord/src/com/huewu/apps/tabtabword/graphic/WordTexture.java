package com.huewu.apps.tabtabword.graphic;


import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;

class WordTexture {
	
	private static Random mRandom = new Random();
	Bitmap mBitmap = null;
	Paint mPaint = null;
	Rect mRect = null;
	
	WordTexture(String word) {
		
		word = word.trim();
		Paint paint = new Paint();
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(0);
		paint.setTextSize(50.0f);
		paint.setShader(
				new LinearGradient(0, 0, 400, 50, 
						Color.argb(255, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)),
						Color.argb(255, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)), 
						Shader.TileMode.MIRROR));
		paint.setShadowLayer(3.0f, 1.0f, 1.0f, Color.argb(100, 0, 0, 0));
		
		FontMetrics metric = paint.getFontMetrics();
		
		Log.e("FONT", "AS: " + metric.ascent);
		Log.e("FONT", "BT: " + metric.bottom);
		Log.e("FONT", "TP: " + metric.top);
		
		float width = paint.measureText(word);
		Bitmap bitmap = Bitmap.createBitmap((int) width, (int) (metric.bottom - metric.top), Config.ARGB_8888); 
		Canvas c = new Canvas(bitmap);
		c.drawARGB(255, 255, 255, 255);
		c.drawText(word, width/2, -metric.ascent, paint);
		mBitmap = bitmap;

		
		mRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		mPaint = new Paint();	//paint for drawing created word texture.
		mPaint.setAntiAlias(true);
	}
	
	public Rect getRect(){
		return new Rect(mRect);
	}
	
	public Bitmap getBitmap(){
		return mBitmap;
	}

	public Paint getPaint() {
		// TODO Auto-generated method stub
		return mPaint;
	}
}//end of class
