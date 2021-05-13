package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Calendar;

public class AnalogClockStyle extends View {

    /** 時鐘呈現的長與寬 */
    private float mHeight, mWidth = 0;

    /** 時鐘上的數字 */
    private final int[] mClockHours = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    /** 指針與邊框的距離 */
    private int mPadding = 0;

    /** 從圓周，分針與十針的截去長度*/
    private int mHandTruncation, mHourHandTruncation = 0;

    /** 其他用來計算指針位置的變數 */
    private int mRadius = 0;
    private Paint mPaint;
    private final Rect mRect = new Rect();
    private boolean isInit;  //一旦時鐘被建立會變成true

    /**建構子*/
    public AnalogClockStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnalogClockStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**開始繪製時鐘*/
    @Override
    protected void onDraw(Canvas canvas) {
        /**若時鐘還沒被建立*/
        if (!isInit) {
            mPaint = new Paint(); //建立畫筆
            mHeight = getHeight();//取得畫布的長
            mWidth = getWidth();  //取得畫布的寬
            int mNumeralSpacing = 0; //數字與邊框的距離
            mPadding = mNumeralSpacing + 50;
            int minAttr = (int) Math.min(mHeight, mWidth); //取得直徑
            mRadius = minAttr / 2 - mPadding; //取得指針最長的半徑

            /** 定義分針時針的長度*/
            mHandTruncation = minAttr / 20;
            mHourHandTruncation = minAttr / 10;

            isInit = true; //代表時鐘建立完成
        }

        canvas.drawColor(0); //畫布顏色>>透明

        /**時鐘邊框*/
        mPaint.reset();
        mPaint.setColor(-1);
        mPaint.setStyle(Paint.Style.FILL); //邊線
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius + mPadding - 10, mPaint);
        mPaint.reset();
        mPaint.setColor(Color.parseColor("#A9A9A9"));
        mPaint.setStyle(Paint.Style.STROKE); //邊線
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius + mPadding - 10, mPaint);

        /**時鐘圓心*/
        mPaint.setStyle(Paint.Style.FILL);  //填滿
        canvas.drawCircle(mWidth / 2, mHeight / 2, 12, mPaint);  // the 03 clock hands will be rotated from this center point.

        /**設置數字大小*/
        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        mPaint.setTextSize(fontSize);  // set font size (optional)

        /**在邊框畫上數字，迴圈12次*/
        for (int hour : mClockHours) {
            String tmp = String.valueOf(hour);
            mPaint.getTextBounds(tmp, 0, tmp.length(), mRect);  // for circle-wise bounding

            // 透過sin cos找出數字應擺的位置
            double angle = Math.PI / 6 * (hour - 3);
            int x = (int) (mWidth / 2 + Math.cos(angle) * mRadius - mRect.width() / 2);
            int y = (int) (mHeight / 2 + Math.sin(angle) * mRadius + mRect.height() / 2);

            //將數字畫到該位置上
            canvas.drawText(String.valueOf(hour), x, y, mPaint);
        }

        /**根據實際時間，繪製時針位置*/
        Calendar calendar = Calendar.getInstance();  //取得現在時間
        float hour = calendar.get(Calendar.HOUR_OF_DAY); //取得時間中的小時
        hour = hour > 12 ? hour - 12 : hour;  //將24小時制改成12小時制，若hour>12則-12否則維持hour

        /**呼叫drawHandLine在具體角度方向繪製時針與分針*/
        drawHandLine(canvas, (hour + (double)calendar.get(Calendar.MINUTE) / 60) * 5f, true); //畫時針
        drawHandLine(canvas, calendar.get(Calendar.MINUTE), false); //畫分針

        /**每隔500毫秒檢查一次時間*/
        postInvalidateDelayed(500);
        invalidate();
    }

    /**實作指針的繪製*/
    private void drawHandLine(Canvas canvas, double moment, boolean isHour) {
        double angle = Math.PI * moment / 30 - Math.PI / 2; //找到角度
        int handRadius = isHour ? mRadius - mHandTruncation - mHourHandTruncation : mRadius - mHandTruncation; //設定指針長度
        canvas.drawLine(mWidth / 2, mHeight / 2, (float) (mWidth / 2 + Math.cos(angle) * handRadius), (float) (mHeight / 2 + Math.sin(angle) * handRadius), mPaint);
    }
}