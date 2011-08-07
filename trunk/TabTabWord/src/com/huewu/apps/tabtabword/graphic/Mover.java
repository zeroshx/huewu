package com.huewu.apps.tabtabword.graphic;

import java.util.Random;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

class Mover implements IMove{

	static Random mRand = new Random();
	
	Rect mRegion = null;
	
	int mX = 0;
	int mVX = 0;

	int mY = 0;
	int mVY = 0;

	int mRotate = 0;
	int mVRotate = 0;
	
	float mZoom = 0;
	float mVZoom = 0;
	
	Matrix mMatrix = null;

	int mCenterX = 0;
	int mCenterY = 0;
	
	Mover(int centerX, int centerY, Rect region){
		mMatrix = new Matrix();
		
		mRegion = region;
		mCenterX = centerX;
		mCenterY = centerY;
//		mX = 0;//mRand.nextInt(region.right);
		mX = region.centerX();
		mVX = mRand.nextInt(13) - 6;

		mY = region.centerY();
		mVY = mRand.nextInt(13) - 6;
		
		mZoom = mRand.nextFloat() + 1.0f;
		mVZoom = (mRand.nextFloat() - 0.5f) / 100.0f;
		
		mRotate = mRand.nextInt(90) - 45;
		mVRotate = (mRand.nextInt(2) == 1) ? 1 : -1; 
				
		calculateMatrix();		
	}

	@Override
	public int getX() {
		return mX;
	}

	@Override
	public int getY() {
		return mY;
	}

	@Override
	public int getRotation() {
		return mRotate;
	}
	
	@Override
	public float getZoom() {
		return mZoom;
	}

	@Override
	public void move(float step) {
		if( mRegion.contains(mX + mCenterX, mRegion.centerY()) == false){
			mVX = -mVX;
		}
		
		if( mRegion.contains(mRegion.centerX(), mY + mCenterY) == false){
			mVY = -mVY;
		}
		
		if( mZoom < 0.5f || mZoom > 2.0f)
			mVZoom = -mVZoom;
		
		mX += mVX * step;
		mY += mVY * step;
		mRotate += mVRotate * step;
		mZoom += mVZoom * step;
		
		calculateMatrix();
	}

	@Override
	public Matrix getMatrix() {
		return new Matrix(mMatrix);
	}
	
	 
	private void calculateMatrix(){
		mMatrix.reset();
		mMatrix.postTranslate(-mCenterX, -mCenterY);
		mMatrix.postRotate(mRotate);
		mMatrix.postTranslate(mCenterX, mCenterY);
		mMatrix.postScale(mZoom, mZoom);
		mMatrix.postTranslate(mX, mY);
	}

}//end of class
