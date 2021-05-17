package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RingProgressBar extends View{

    private float mWidth = 0;

    /** 指針與邊框的距離 */
    private int mPadding = 0;

    /** 其他用來計算指針位置的變數 */
    private int mRadius = 0;
    private boolean isInit = false;  //一旦時鐘被建立會變成true

    /**
     * 圓環的顏色
     */
    private  int roundColor;

    /**
     * 圓環進度的顏色
     */
    private  int roundProgressColor;

    /**
     * 中間進度百分比的字符串的顏色
     */
    private  int textColor;

    /**
     * 中間進度百分比的字符串的字體
     */
    private  float textSize;

    /**
     * 圓環的寬度
     */
    private  float roundWidth;

    /**
     * 最大進度
     */
    private  int max;

    /**
     * 進度開始的角度數
     */
    private  int endAngle, newAngle;
    private  int backColor;
    private int time;
    private int minute;
    private boolean isNewProgress = false;
    private final TypedArray mTypedArray;

    public RingProgressBar(Context context) {
        this (context, null );
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        this (context, attrs, 0 );
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        //獲取自定義屬性和默認值，第一個參數是從用戶屬性中得到的設置，如果用戶沒有設置，那麼就用默認的屬性，即：第二個參數
        mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RingProgressBar);
    }

    @Override
    protected  void onDraw(Canvas canvas) {
        super .onDraw(canvas);

        //若progressbar未被初始化過
        if (!isInit) {

            float mHeight = getHeight();//取得畫布的長
            mWidth = getWidth();  //取得畫布的寬
            int mNumeralSpacing = 0; //數字與邊框的距離
            mPadding = mNumeralSpacing + 50;
            int minAttr = (int) Math.min(mHeight, mWidth); //取得直徑
            mRadius = minAttr / 2 - mPadding; //取得指針最長的半徑

            /**根據實際時間，取得分針位置*/
            int minn = minute - 15;
            /** 分針錨點(最終位置) */
            int stangle = minn * 6 + (time / 10000);

            //圓環的顏色
            roundColor = mTypedArray .getColor(R.styleable.RingProgressBar_roundColor, Color.GRAY);
            //圓環進度條的顏色
            roundProgressColor = mTypedArray.getColor(R.styleable.RingProgressBar_roundProgressColor, Color.YELLOW);
            //圓環的寬度
            roundWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_roundWidth, 40 );
            //最大進度
            int maxx = (int) time * 10 / 6;
            max = mTypedArray.getInteger(R.styleable.RingProgressBar_max, maxx);
            //進度開始的角度數
            endAngle = mTypedArray.getInt(R.styleable.RingProgressBar_startAngle, stangle);
            //圓形顏色
            backColor = mTypedArray.getColor(R.styleable.RingProgressBar_backColor, 0 );

            isInit = true; //代表時鐘建立完成
        }

        @SuppressLint("DrawAllocation") Paint paint = new Paint();

        int centre = getWidth()/2; //獲取圓心的x坐標
        int radius = ( int ) (centre - roundWidth/2); //圓環的半徑
        paint.setColor(roundColor); //設置圓環的顏色
        paint.setStyle(Paint.Style.STROKE); //設置空心
        paint.setStrokeWidth(roundWidth); //設置圓環的寬度
        paint.setAntiAlias( true );   //消除鋸齒
        canvas.drawCircle(centre, centre, radius, paint); //畫出圓環

        if (backColor != 0 ) {
            paint.setAntiAlias( true );
            paint.setColor(backColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(centre, centre, radius, paint);
        }


        paint.setStrokeWidth(roundWidth); //設置圓環的寬度
        paint.setColor(roundProgressColor);   //設置進度的顏色

        //用於定義的圓弧的形狀和大小的界限
        @SuppressLint("DrawAllocation") RectF rectF = new RectF(mWidth/2-(mRadius + mPadding - 10)+10, mWidth/2-(mRadius + mPadding - 10)+10, mWidth/2+(mRadius + mPadding - 10)-10, mWidth/2+(mRadius + mPadding - 10)-10);

        paint.setStyle(Paint.Style.STROKE);

        /*oval :指定圓弧的外輪廓矩形區域。
          endAngle: 圓弧終點角度，單位為度。
          sweepAngle: 圓弧從endAngle往回掃過的角度，加"-"順時針移動，單位為度。
          useCenter: 如果為True時，在繪製圓弧時將圓心包括在內，通常用來繪製扇形。
          paint: 繪製圓弧的畫板屬性，如顏色，是否填充等
        *
        */
        //根據進度畫圓弧
        if(!isNewProgress)
            canvas.drawArc(rectF, endAngle, -time/10000, true, paint);
        else
            canvas.drawArc(rectF, newAngle, -time/10000, true, paint);
    }

    public  synchronized  int getMax() {
        return max;
    }

    /**
     * 設置進度的最大值
     */
    public  synchronized  void setMax( int max) {
        if (max < 0 ){
            throw  new IllegalArgumentException("max not less than 0" );
        }
        this.max = max;
    }


    /**
     * 設置進度，此為線程安全控件，由於考慮多線的問題，需要同步
     * 刷新界面調用postInvalidate()能在非UI線程刷新
     */
    public  synchronized  void setProgress(int startTime, int leftTime) {
        this.time = leftTime;
        this.newAngle = (startTime - 15)* 6 + (time / 10000);
        postInvalidate();
    }

    public  synchronized  void setProgress(int leftTime) {
        this.time = leftTime;
        postInvalidate();
    }

    public  int getCircleColor() {
        return roundColor;
    }

    public  void setCircleColor( int CircleColor) {
        this .roundColor = CircleColor;
    }

    public  int getCircleProgressColor() {
        return roundProgressColor;
    }

    public  void setCircleProgressColor( int CircleProgressColor) {
        this .roundProgressColor = CircleProgressColor;
    }

    public  int getTextColor() { return textColor; }

    public  void setTextColor( int textColor) {
        this .textColor = textColor;
    }

    public  float getTextSize() {
        return textSize;
    }

    public  void setTextSize( float textSize) {
        this .textSize = textSize;
    }

    public  float getRoundWidth() { return roundWidth; }

    public  void setRoundWidth( float roundWidth) {
        this .roundWidth = roundWidth;
    }

    public  void setMinute(int minute) { this.minute = minute; }

    public  int getMinute() { return minute; }

    public void setTime(int time){this.time = time;}

    public int getTime(){return time;}

    public void setIsNewProgress(boolean init){ this.isNewProgress = init;}

    public boolean getIsNewProgress(){ return isNewProgress;}
}
