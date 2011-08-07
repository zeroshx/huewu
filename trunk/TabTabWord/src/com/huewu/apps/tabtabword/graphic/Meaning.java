package com.huewu.apps.tabtabword.graphic;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

public class Meaning implements IDraw{
	
	MeaningTexture mTexture = null;
	int mY = 0;
	
	public Meaning(String meaning, Rect mRegion){
		mTexture = new MeaningTexture(meaning, mRegion);
		 mY = mRegion.centerY() - 25;
	}

	@Override
	public void draw(Canvas c, long delta) {
		c.drawBitmap(mTexture.getBitmap(), 0, mY, mTexture.getPaint());
	}

}; //end of class
