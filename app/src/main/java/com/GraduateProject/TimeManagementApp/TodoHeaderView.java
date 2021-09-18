package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by guojun on 2015/10/20.
 */
public class TodoHeaderView extends View {
    private final Context mContext;
    private Calendar mToday;
    private Calendar mFirstVisibleDay;
    private Calendar mSelectedDay;
    private static Calendar insertDate;

    private GestureDetectorCompat mGestureDetector;
    private Scroller mStickyScroller;
    private DateTimeInterpreter mDateTimeInterpreter;

    private final PointF mCurrentOrigin = new PointF(0f, 0f);
    private final PointF mLastOrigin = new PointF(0F, 0F);
    private Paint mHeaderBackgroundPaint;
    private Paint mHeaderWeekLabelPaint;
    private Paint mHeaderDayLabelNormalPaint;
    private Paint mHeaderDayLabelTodayPaint;

    private Paint mHeaderFocusTextPaint;
    private Paint mHeaderFocusBackgroungPaint;
    private Paint mHeaderFocusSameDayTextPaint;
    private Paint mHeaderFocusSameDayBackgroundPaint;
    private float mDistanceX = 0;

    // Attributes and their default values.
    private int mHeaderHeight;
    private int mHeaderWidthPerDay;
    private int mFirstDayOfWeek = Calendar.SUNDAY;
    private int mHeaderRowGap = 30;
    private int mHeaderPaddingLeft = 0;
    private int mHeaderPaddingRight = 0;
    private int mHeaderPaddingTop = 30;
    private int mHeaderPaddingBottom = 30;
    private int mHeaderWeekLabelTextSize = 24;
    private int mHeaderWeekLabelTextColor = Color.BLACK;
    private int mHeaderDayLabelTextSize = 24;
    private int mHeaderDayLabelNormalTextColor = Color.BLACK;
    private final int mHeaderDayLabelTodayTextColor;
    private int mHeaderBackgroundColor = Color.WHITE;
    private int mHeaderFocusTextColor = Color.YELLOW;
    private int mHeaderFocusBackgroundColor = Color.BLUE;
    private int mHeaderFocusSameDayTextColor = Color.BLACK;
    private int mHeaderFocusSameDayBackgroundColor = Color.GREEN;
    private int mHeaderWeekLabelHeight;
    private int mHeaderDayLabelWidth;
    private int mHeaderDayLabelHeight;

    private boolean mIsFirstDraw = true;
    private boolean mAreHeaderScrolling = false;

    //    InterFace
    private ScrollListener mScrollListener;
    private DateSelectedChangeListener mDateSelectedChangeListener;


    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
//            mStickyScroller.forceFinished(true);
            mLastOrigin.x = mCurrentOrigin.x;
            mLastOrigin.y = mCurrentOrigin.y;
//            Log.d(TAG, "onDown         mCurrentOrigin.x" + mCurrentOrigin.x % mHeaderWidthPerDay);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mAreHeaderScrolling = true;
            int mXScrollingSpeed = 1;
            mDistanceX = distanceX * mXScrollingSpeed;
            ViewCompat.postInvalidateOnAnimation(TodoHeaderView.this);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Calendar newSelectedDay = getDateFromPoint(e.getX());
            if (null != newSelectedDay && mDateSelectedChangeListener != null) {
                mDateSelectedChangeListener.onDateSelectedChange(mSelectedDay, newSelectedDay);
                setSelectedAddDay(newSelectedDay);
            }
            assert newSelectedDay != null;
            mSelectedDay = (Calendar) newSelectedDay.clone();
            ViewCompat.postInvalidateOnAnimation(TodoHeaderView.this);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    };


    public TodoHeaderView(Context context) {
        this(context, null);
    }

    public TodoHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TodoHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Hold references.
        mContext = context;

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekHeaderView, 0, 0);
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekHeaderView_firstDayOfWeek2, mFirstDayOfWeek);
            mHeaderRowGap = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerRowGap, mHeaderRowGap);
            mHeaderPaddingLeft = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerPaddingLeft, mHeaderPaddingLeft);
            mHeaderPaddingRight = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerPaddingRight, mHeaderPaddingRight);
            mHeaderPaddingTop = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerPaddingTop, mHeaderPaddingTop);
            mHeaderPaddingBottom = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerPaddingBottom, mHeaderPaddingBottom);
            mHeaderDayLabelNormalTextColor = a.getColor(R.styleable.WeekHeaderView_headerDayLabelNormalTextColor, mHeaderDayLabelNormalTextColor);
            mHeaderDayLabelTodayTextColor = a.getColor(R.styleable.WeekHeaderView_headerDayLabelTodayTextColor, mHeaderDayLabelNormalTextColor);
            mHeaderWeekLabelTextColor = a.getColor(R.styleable.WeekHeaderView_headerWeekLabelTextColor, mHeaderWeekLabelTextColor);
            mHeaderBackgroundColor = a.getColor(R.styleable.WeekHeaderView_headerBackgroundColor, mHeaderBackgroundColor);
            mHeaderFocusTextColor = a.getColor(R.styleable.WeekHeaderView_headerFocusTextColor, mHeaderFocusTextColor);
            mHeaderFocusBackgroundColor = a.getColor(R.styleable.WeekHeaderView_headerFocusBackgroundColor, mHeaderFocusBackgroundColor);
            mHeaderFocusSameDayTextColor = a.getColor(R.styleable.WeekHeaderView_headerFocusSameDayTextColor, mHeaderFocusSameDayTextColor);
            mHeaderFocusSameDayBackgroundColor = a.getColor(R.styleable.WeekHeaderView_headerFocusSameDayBackgroundColor, mHeaderFocusSameDayBackgroundColor);
            mHeaderWeekLabelTextSize = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerWeekLabelTextSize, mHeaderWeekLabelTextSize);
            mHeaderDayLabelTextSize = a.getDimensionPixelSize(R.styleable.WeekHeaderView_headerDayLabelTextSize, mHeaderDayLabelTextSize);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        //Get the date today
        mToday = Calendar.getInstance();
        mToday.set(Calendar.HOUR_OF_DAY, 0);
        mToday.set(Calendar.MINUTE, 0);
        mToday.set(Calendar.SECOND, 0);

        mSelectedDay = (Calendar) mToday.clone();

        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mStickyScroller = new Scroller(mContext);

        //prepare paint
        mHeaderBackgroundPaint = new Paint();
        mHeaderBackgroundPaint.setColor(mHeaderBackgroundColor);

        mHeaderFocusSameDayBackgroundPaint = new Paint();
        mHeaderFocusSameDayBackgroundPaint.setColor(mHeaderFocusSameDayBackgroundColor);
        mHeaderFocusSameDayBackgroundPaint.setAntiAlias(true);

        mHeaderFocusBackgroungPaint = new Paint();
        mHeaderFocusBackgroungPaint.setColor(mHeaderFocusBackgroundColor);
        mHeaderFocusBackgroungPaint.setStyle(Paint.Style.STROKE);
        mHeaderFocusBackgroungPaint.setAntiAlias(true);

        mHeaderWeekLabelPaint = new Paint();
        mHeaderWeekLabelPaint.setColor(mHeaderWeekLabelTextColor);
        mHeaderWeekLabelPaint.setTextSize(mHeaderWeekLabelTextSize);
        Rect rect = new Rect();
        mHeaderWeekLabelPaint.getTextBounds("日", 0, "日".length(), rect);
        mHeaderWeekLabelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderWeekLabelPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderWeekLabelHeight = rect.height();

        mHeaderDayLabelNormalPaint = new Paint();
        mHeaderDayLabelNormalPaint.setColor(mHeaderDayLabelNormalTextColor);
        mHeaderDayLabelNormalPaint.setTextSize(mHeaderDayLabelTextSize);
        mHeaderDayLabelNormalPaint.getTextBounds("日", 0, "日".length(), rect);
        mHeaderDayLabelNormalPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderDayLabelNormalPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderDayLabelHeight = rect.height();
        mHeaderDayLabelWidth = rect.width();

        mHeaderDayLabelTodayPaint = new Paint();
        mHeaderDayLabelTodayPaint.setColor(mHeaderDayLabelTodayTextColor);
        mHeaderDayLabelTodayPaint.setTextSize(mHeaderDayLabelTextSize);
        mHeaderDayLabelTodayPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderDayLabelTodayPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderFocusTextPaint = new Paint();
        mHeaderFocusTextPaint.setColor(mHeaderFocusTextColor);
        mHeaderFocusTextPaint.setTextSize(mHeaderDayLabelTextSize);
        mHeaderFocusTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderFocusTextPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderFocusSameDayTextPaint = new Paint();
        mHeaderFocusSameDayTextPaint.setColor(mHeaderFocusSameDayTextColor);
        mHeaderFocusSameDayTextPaint.setTextSize(mHeaderDayLabelTextSize);
        mHeaderFocusSameDayTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderFocusSameDayTextPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderHeight = mHeaderPaddingBottom + mHeaderPaddingTop + mHeaderRowGap + mHeaderWeekLabelHeight + mHeaderDayLabelWidth;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, mHeaderHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHeader(canvas);
    }

    //Draw week label and day label.
    private void drawHeader(Canvas canvas) {
        mHeaderWidthPerDay = getWidth() - (mHeaderPaddingLeft + mHeaderPaddingRight);
        mHeaderWidthPerDay = mHeaderWidthPerDay / 7;

        if (mIsFirstDraw) {
            mIsFirstDraw = false;
            mSelectedDay = (Calendar) mToday.clone();
            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (mToday.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                int difference = (7 + (mToday.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)) % 7;
                mCurrentOrigin.x += (mHeaderWidthPerDay) * difference;
            }
        }
        if (mAreHeaderScrolling) {
            mCurrentOrigin.x -= mDistanceX;
        }
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mHeaderWidthPerDay)));
//        Log.d(TAG,"leftDaysWithGaps=="+leftDaysWithGaps);
        float startFromPixel = mCurrentOrigin.x + mHeaderWidthPerDay * leftDaysWithGaps;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) mToday.clone();
        day.add(Calendar.HOUR, 6);

        // Iterate through each day.
        Calendar oldFirstVisibleDay = mFirstVisibleDay;
        mFirstVisibleDay = (Calendar) mToday.clone();
        mFirstVisibleDay.add(Calendar.DATE, leftDaysWithGaps);
        Calendar mLastVisibleDay = (Calendar) mFirstVisibleDay.clone();
        mLastVisibleDay.add(Calendar.DATE, 6);
        if (!mFirstVisibleDay.equals(oldFirstVisibleDay) && mScrollListener != null) {
            mScrollListener.onFirstVisibleDayChanged(mFirstVisibleDay, oldFirstVisibleDay);
        }
        // Draw the header background.
        canvas.drawRect(0, 0, getWidth(), mHeaderHeight, mHeaderBackgroundPaint);

        mFirstDayOfWeek = (mFirstDayOfWeek > Calendar.SATURDAY || mFirstDayOfWeek < Calendar.SUNDAY) ? Calendar.SUNDAY : mFirstDayOfWeek;
        int dayOfWeek;
        // Draw the week labels;
        for (int i = mFirstDayOfWeek; i < mFirstDayOfWeek + 7; i++) {
            dayOfWeek = i % 7;
            String weekLabel = getDateTimeInterpreter().interpretWeek(dayOfWeek == 0 ? 7 : dayOfWeek);
            if (weekLabel == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            }
            canvas.drawText(weekLabel, mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (i - mFirstDayOfWeek), mHeaderWeekLabelHeight / 2 + mHeaderPaddingTop, mHeaderWeekLabelPaint);

        }
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + 7 + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) mToday.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean isToday = isSameDay(day, mToday);
            boolean selectedDay = isSameDay(day, mSelectedDay);
            // Draw the day labels.
            String dayLabel = getDateTimeInterpreter().interpretDate(day);

            if (dayLabel == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            }
            if (selectedDay) {
                if (isToday) {
                    canvas.drawCircle(startFromPixel + mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (dayNumber - leftDaysWithGaps - 1),
                            (mHeaderDayLabelHeight / 2 + mHeaderRowGap + mHeaderWeekLabelHeight),
                            mHeaderDayLabelWidth / 2 + (Math.min(mHeaderRowGap, mHeaderPaddingBottom)), mHeaderFocusSameDayBackgroundPaint);
                    canvas.drawText(dayLabel, startFromPixel + mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (dayNumber - leftDaysWithGaps - 1),
                            (mHeaderDayLabelHeight + mHeaderRowGap + mHeaderWeekLabelHeight), mHeaderFocusSameDayTextPaint);
                } else {
                    canvas.drawCircle(startFromPixel + mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (dayNumber - leftDaysWithGaps - 1),
                            (mHeaderDayLabelHeight / 2 + mHeaderRowGap + mHeaderWeekLabelHeight),
                            mHeaderDayLabelWidth / 2 + (Math.min(mHeaderRowGap, mHeaderPaddingBottom)), mHeaderFocusBackgroungPaint);
                    canvas.drawText(dayLabel, startFromPixel + mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (dayNumber - leftDaysWithGaps - 1),
                            (mHeaderDayLabelHeight + mHeaderRowGap + mHeaderWeekLabelHeight), mHeaderFocusTextPaint);
                }
            } else {
                canvas.drawText(dayLabel, startFromPixel + mHeaderPaddingLeft + mHeaderWidthPerDay / 2 + (mHeaderWidthPerDay) * (dayNumber - leftDaysWithGaps - 1),
                        (mHeaderDayLabelHeight + mHeaderRowGap + mHeaderWeekLabelHeight), isToday ? mHeaderDayLabelTodayPaint : mHeaderDayLabelNormalPaint);
            }
        }
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getDateFromPoint(float x) {
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mHeaderWidthPerDay)));
        float startPixel = mCurrentOrigin.x + (mHeaderWidthPerDay) * leftDaysWithGaps;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + 7 + 1;
             dayNumber++) {
            float start = startPixel;
            if (mHeaderWidthPerDay + startPixel - start > 0
                    && x > start && x < startPixel + mHeaderWidthPerDay) {
                Calendar day = (Calendar) mToday.clone();
                day.add(Calendar.DATE, dayNumber - 1);
                return day;
            }
            startPixel += mHeaderWidthPerDay;
        }
        return null;
    }


    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                final String[] weekLabels = {"日", "一", "二", "三", "四", "五", "六"};

                @Override
                public String interpretDate(Calendar date) {
                    SimpleDateFormat format = new SimpleDateFormat("dd", Locale.getDefault());
                    return format.format(date.getTime());
                }

                @Override
                public String interpretTime(int hour) {
                    return null;
                }

                @Override
                public String interpretWeek(int dayofweek) {
                    if (dayofweek > 7 || dayofweek < 1) {
                        return null;
                    }
                    return weekLabels[dayofweek - 1];
                }
            };
        }
        return mDateTimeInterpreter;
    }

    public Calendar getSelectedDay() {
        setSelectedAddDay(mSelectedDay);
        return mSelectedDay;
    }

    public void setSelectedAddDay(Calendar newdate) {
        insertDate = newdate;
    }

    public static Calendar getSelectedAddDay() {
        return insertDate;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mStickyScroller.computeScrollOffset()) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mAreHeaderScrolling = false;
            mDistanceX = 0;
            float dx = mLastOrigin.x - mCurrentOrigin.x;
            if (Math.abs(dx) < mHeaderWidthPerDay) {
                mCurrentOrigin.x = mLastOrigin.x;
                mCurrentOrigin.y = mLastOrigin.y;
            } else {
                int scrollDx = (int) (dx > 0 ? (-mHeaderWidthPerDay * 7 + dx) : (mHeaderWidthPerDay * 7 + dx));
                mSelectedDay.add(Calendar.DATE, dx > 0 ? 7 : -7);
                mStickyScroller.startScroll((int) mCurrentOrigin.x, 0, scrollDx, 0);
            }
            ViewCompat.postInvalidateOnAnimation(TodoHeaderView.this);
            return mGestureDetector.onTouchEvent(event);
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mStickyScroller.computeScrollOffset()) {
            mCurrentOrigin.x = mStickyScroller.getCurrX();
            ViewCompat.postInvalidateOnAnimation(this);
       }
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    public ScrollListener getScrollListener() {
        return mScrollListener;
    }

    public void setMonthChangeListener(MonthChangeListener monthChangeListener) {
    }

    public void setScrollListener(ScrollListener mScrollListener) {
        this.mScrollListener = mScrollListener;
        invalidate();
    }


    public void setDateSelectedChangeListener(DateSelectedChangeListener dateSelectedChangeListener) {
        this.mDateSelectedChangeListener = dateSelectedChangeListener;
    }

    public void goToDate(Calendar newSelectedDay) {
        TodayToDoListActivity.getTask(newSelectedDay);
    }


    ////////////////////////////////////////////////////////////////
    //
    //       InterFace
    //
    ///////////////////////////////////////////////////////////////
    public interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         * <p/>
         * (this will also be called during the first draw of the WeekHeaderView)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);
    }

    public interface DateSelectedChangeListener {
        void onDateSelectedChange(Calendar oldSelectedDay, Calendar newSelectedDay);
    }

    public interface MonthChangeListener {
        void onMonthChange(Calendar day);
    }

    /**
     * Checks if two times are on the same day.
     *
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    private boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

}
