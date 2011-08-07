package com.huewu.apps.tabtabword.graphic;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class Word implements IDraw, ITouch {
	
	WordTexture mTexture = null;
	Mover mMover = null;
	String mWord = "";
	boolean mHit = false;
	
	public Word(String word, Rect region){
		mWord = word;
		mTexture = new WordTexture(word);
		mMover = new Mover(mTexture.getRect().centerX(), mTexture.getRect().centerY(), region);
	}
	
	public String getContent(){
		return mWord;
	}
	
	private float mOffset = 0f;
	public void setOffset(float offset){
		mOffset = offset;
	}
	
	public boolean isVisible(){
		return (mHit == false);
	}

	@Override
	public void draw(Canvas c, long delta) {
		
		if(mHit == true)
			return; 
		
		float step = delta / 40.0f;
		
		Matrix matrix = mMover.getMatrix();
		matrix.postTranslate(mOffset, 0);
		c.drawBitmap(mTexture.getBitmap(), matrix, mTexture.getPaint());
		mMover.move(step);
	}

	public boolean contains(float mTouchX, float mTouchY) {
		int x = mMover.getX();
		int y = mMover.getY();
		float zoom = mMover.getZoom();
		
		Rect r = mTexture.getRect();
		r.set(0, 0, (int)(r.right * zoom), (int)(r.bottom * zoom));
		r.offset(x, y);
		
		mTouchX -= mOffset;
		
//		Log.e("WORD", "Rect: " + mWord + ":" + r.toShortString());
//		Log.e("WORD", "Touch: " + "X:" + mTouchX + " Y:" + mTouchY);
		
		if(	r.contains((int)mTouchX, (int)mTouchY) ){
			mHit = true;
		}
		//Log.e("WORD", "HIT: " + mWord);
		return mHit;
	}

}//end of class
