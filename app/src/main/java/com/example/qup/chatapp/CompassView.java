package com.example.qup.chatapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 나침반을 표시하기 위한 클래스
 */
public class CompassView extends View {
	private Drawable mCompass;
	private float mAzimuth = 0;
	private int PADDING = 2;

	public CompassView(Context ctx) {
		super(ctx);

		this.mCompass = ctx.getResources().getDrawable(R.drawable.arrow_n);
	}

	protected void onDraw(Canvas canvas) {
		canvas.save();

		canvas.rotate(360 - mAzimuth, PADDING + mCompass.getMinimumWidth()
				/ 2, PADDING + mCompass.getMinimumHeight() / 2);	//각도 계산 하여 회전
		mCompass.setBounds(PADDING, PADDING, PADDING
				+ mCompass.getMinimumWidth(), PADDING
				+ mCompass.getMinimumHeight());	//회전후 그림을 가로,세로 길이 폭으로 조정한다

		mCompass.draw(canvas);	//설정한 대로 그린다
		canvas.restore();

		super.onDraw(canvas);
	}

	public void setAzimuth(float aAzimuth) {
		mAzimuth = aAzimuth;
	}
}