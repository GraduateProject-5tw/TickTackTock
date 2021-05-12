package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface ;
import android.util.AttributeSet;
import android.view.View;

public class RingProgressBar extends View{
    /**
     * 畫筆對象的引用
     */
    private Paint paint;

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
     * 當前進度
     */
    private  int progress;
    /**
     * 是否顯示中間的進度
     */
    private  boolean textIsDisplayable;

    /**
     * 進度的風格，實心或者空心
     */
    private  int style;

    /**
     * 進度開始的角度數
     */
    private  int startAngle;

    private  int backColor;



    public  static  final  int STROKE = 0 ;
    public  static  final  int FILL = 1 ;

    public RingProgressBar(Context context) {
        this (context, null );
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        this (context, attrs, 0 );
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);

        paint = new Paint();


        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RingProgressBar);

        //獲取自定義屬性和默認值，第一個參數是從用戶屬性中得到的設置，如果用戶沒有設置，那麼就用默認的屬性，即：第二個參數
        //圓環的顏色
        roundColor = mTypedArray .getColor(R.styleable.RingProgressBar_roundColor, Color.GRAY);
        //圓環進度條的顏色
        roundProgressColor = mTypedArray.getColor(R.styleable.RingProgressBar_roundProgressColor, Color.YELLOW);
        //文字的顏色
        //textColor = mTypedArray.getColor( R.styleable.RingProgressBar_textColor, Color.GREEN);
        //文字的大小
        //textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_textSize, 15 );
        //圓環的寬度
        roundWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_roundWidth, 15 );
        //最大進度
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_max, 25*10/6 );
        //是否顯示中間的進度
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RingProgressBar_textIsDisplayable, false );
        //進度的風格，實心或者空心
        style = mTypedArray.getInt(R.styleable.RingProgressBar_style, 0 );
        //進度開始的角度數
        startAngle = mTypedArray.getInt(R.styleable.RingProgressBar_startAngle, 0 );
        //圓形顏色
        backColor = mTypedArray.getColor(R.styleable.RingProgressBar_backColor, 0 );
        mTypedArray.recycle();
    }

    @Override
    protected  void onDraw(Canvas canvas) {
        super .onDraw(canvas);

        /**
         * 畫最外層的大圓環
         */
        int centre = getWidth()/2; //獲取圓心的x坐標
        int radius = ( int ) (centre - roundWidth/2); //圓環的半徑
        paint.setColor(roundColor); //設置圓環的顏色
        paint.setStyle(Paint.Style.STROKE); //設置空心
        paint.setStrokeWidth(roundWidth); //設置圓環的寬度
        paint.setAntiAlias( true );   //消除鋸齒
        canvas.drawCircle(centre, centre, radius, paint); //畫出圓環

        // Log.e("log", centre + "");
        if (backColor != 0 ) {
            paint.setAntiAlias( true );
            paint.setColor(backColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(centre, centre, radius, paint);
        }

        /**
         * 畫進度百分比
         */
        paint.setStrokeWidth( 0 );
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD); //設置字體
        int percent = ( int )((( float )progress / ( float )max) * 100);   //中間的進度百分比，先轉換成float在進行除法運算，不然都為0
        float textWidth = paint.measureText(percent + "%");    //測量字體寬度，我們需要根據字體的寬度設置在圓環中間

        if (textIsDisplayable && percent != 0 && style == STROKE){
            canvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize/2, paint); //畫出進度百分比
        }


        /**
         * 畫圓弧，畫圓環的進度
         */
        //設置進度是實心還是空心
        paint.setStrokeWidth(roundWidth); //設置圓環的寬度
        paint.setColor(roundProgressColor);   //設置進度的顏色
        RectF oval = new RectF(centre - radius, centre - radius , centre
                + radius, centre + radius);   //用於定義的圓弧的形狀和大小的界限

        switch (style) {
            case STROKE:{
                paint.setStyle(Paint.Style.STROKE);

                /*第二個參數是進度開始的角度，-90表示從12點方向開始走進度，如果是0表示從三點鐘方向走進度，依次類推
                 *public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)
                    oval :指定圓弧的外輪廓矩形區域。
                    startAngle: 圓弧起始角度，單位為度。
                    sweepAngle: 圓弧掃過的角度，順時針方向，單位為度。
                    useCenter: 如果為True時，在繪製圓弧時將圓心包括在內，通常用來繪製扇形。
                    paint: 繪製圓弧的畫板屬性，如顏色，是否填充等
                 *
                */
                canvas.drawArc(oval, startAngle, 360 * progress / max, false , paint);   //根據進度畫圓弧
                break ;
            }
            case FILL:{
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress !=0 )
                    canvas.drawArc(oval, startAngle, 360 * progress / max, true , paint);   //根據進度畫圓弧
                break ;
            }
        }


    }

    public  synchronized  int getMax() {
        return max;
    }

    /**
     * 設置進度的最大值
     * @param max
     */
    public  synchronized  void setMax( int max) {
        if (max < 0 ){
            throw  new IllegalArgumentException("max not less than 0" );
        }
        this .max = max;
    }

    /**
     * 獲取進度.需要同步
     * @return
     */
    public  synchronized  int getProgress() {
        return progress;
    }

    /**
     * 設置進度，此為線程安全控件，由於考慮多線的問題，需要同步
     * 刷新界面調用postInvalidate()能在非UI線程刷新
     * @param progress
     */
    public  synchronized  void setProgress( int progress) {
        if (progress < 0 ){
            throw  new IllegalArgumentException("progress not less than 0" );
        }
        if (progress > max){
            progress = max;
        }
        if (progress <= max){
            this .progress = progress;
            postInvalidate();
        }
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

    public  int getTextColor() {
        return textColor;
    }

    public  void setTextColor( int textColor) {
        this .textColor = textColor;
    }

    public  float getTextSize() {
        return textSize;
    }

    public  void setTextSize( float textSize) {
        this .textSize = textSize;
    }

    public  float getRoundWidth() {
        return roundWidth;
    }

    public  void setRoundWidth( float roundWidth) {
        this .roundWidth = roundWidth;
    }


}
