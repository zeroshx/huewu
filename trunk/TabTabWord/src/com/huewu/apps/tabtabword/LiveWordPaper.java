/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huewu.apps.tabtabword;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.huewu.apps.tabtabword.graphic.Meaning;
import com.huewu.apps.tabtabword.graphic.Word;

/*
 * This animated wallpaper draws a rotating wireframe cube.
 */

public class LiveWordPaper extends WallpaperService {

	private final Handler mHandler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();
		WordLoader.loadWords(this);
//		WordLoader.dumpWordList();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new WordPaintEngine();
	}

	class WordPaintEngine extends Engine {

		//		private ArrayList<Word> mWords = null;
		private Bitmap mTile;
		private WordManager mWordManager = null;

		private final Runnable mDrawWords = new Runnable() {
			public void run() {
				drawFrame();
			}
		};
		private boolean mVisible;
		private float mOffset;
		private long mStartTime;
		private long mEndTime;
		private float mTouchX = -1;
		private float mTouchY = -1;

		WordPaintEngine() {
			// Create a Paint to draw the lines for our cube
			mTile = BitmapFactory.decodeResource(getResources(), R.drawable.back_tile);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			WallpaperManager manager = WallpaperManager.getInstance(LiveWordPaper.this);
			int w = manager.getDesiredMinimumWidth();
			int h = manager.getDesiredMinimumHeight();
			Rect region = new Rect(0,0,w,h);
			mWordManager = new WordManager(region, 5);
			mWordManager.generateWords();

			// By default we don't get touch events, so enable them.
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mDrawWords);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mDrawWords);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			// store the center of the surface, so we can draw the cube in the right spot
			//			mCenterX = width/2.0f;
			//			mCenterY = height/2.0f;
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			mStartTime = SystemClock.elapsedRealtime(); 			
			drawFrame();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mDrawWords);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xStep, float yStep, int xPixels, int yPixels) {
			mOffset = xPixels;
			//			Log.e("Offset", "Offset Changed: " + xOffset);
			//			Log.e("Offset", "Offset Changed: " + xPixels);
			drawFrame();
		}

		/*
		 * Store the position of the touch event so we can use it for drawing later
		 */
		@Override
		public void onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mTouchX = event.getX();
				mTouchY = event.getY();
			} 
			
			super.onTouchEvent(event);
		}
		
		void checkClick(){
			if(mTouchX == -1 || mTouchY == -1)
				return;
			
			mWordManager.checkTouch(mTouchX, mTouchY);
			
			mTouchX = -1;
			mTouchY = -1;
		}

		/*
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here. This example draws a wireframe cube.
		 */
		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
			
			checkClick();			

			long delta = 0;
			long currentTime = SystemClock.elapsedRealtime(); 
			delta = currentTime - mStartTime;

			if(delta < DELTA / 2){
				//do not draw

			}else{
				mStartTime = currentTime;

				Canvas c = null;
				try {


					c = holder.lockCanvas();
					if (c != null) {
						// draw something
						// drawCube(c);
						drawBackground(c);
						drawTouchPoint(c);
						for(Word w : mWordManager.getWordList()){
							w.setOffset(mOffset);
							w.draw(c, delta);
						}
						drawMeaning(c);
					}
				} finally {
					if (c != null) holder.unlockCanvasAndPost(c);
				}				

			}
			
			// Reschedule the next redraw
			mHandler.removeCallbacks(mDrawWords);
			if (mVisible) {
				//10frame per sec.

				delta = delta > DELTA ? 0 : DELTA - delta;
				mHandler.postDelayed(mDrawWords, DELTA);
			}
		}

		private final int DELTA = 40;

		private void drawMeaning(Canvas c) {
			Meaning m = mWordManager.getMeaning();
			m.draw(c, 0);
		}

		private void drawBackground(Canvas c) {
			Paint paint = new Paint();
			paint.setShader(new BitmapShader(mTile, TileMode.REPEAT, TileMode.REPEAT));
			c.drawPaint(paint);
		}

		/*
		 * Draw a circle around the current touch point, if any.
		 */
		void drawTouchPoint(Canvas c) {
			//            if (mTouchX >=0 && mTouchY >= 0) {
			//                c.drawCircle(mTouchX, mTouchY, 80, mPaint);
			//            }
		}

	}//end of inner class engine.


}//end of class