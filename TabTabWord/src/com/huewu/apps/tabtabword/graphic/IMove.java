package com.huewu.apps.tabtabword.graphic;

import android.graphics.Matrix;

public interface IMove {
	
	int getX();
	int getY();
	int getRotation();
	float getZoom();
	
	Matrix getMatrix();
	
	void move(float step);

}//end of interface
