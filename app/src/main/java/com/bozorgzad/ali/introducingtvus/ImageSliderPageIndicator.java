package com.bozorgzad.ali.introducingtvus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

/*
 * Created by Ali_Dev on 7/8/2017.
 */

public class ImageSliderPageIndicator extends AppCompatImageView {

    private float screenWidthPixel;
    private float screenHeightPixel;
    private Paint fillPaint;
    private Paint strokePaint;
    private int count;
    private float CIRCLE_RADIUS;
    private float CIRCLE_SPACE;
    private float Y_POSITION;
    private float            offsetX;
    private int              currentPageIndex;
    private float            percent;

    public ImageSliderPageIndicator(Context context) {
        super(context);
        if(!isInEditMode()){
            initialize();
        }
    }


    public ImageSliderPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            initialize();
        }
    }


    public ImageSliderPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()){
            initialize();
        }
    }


    private void initialize() {
        screenWidthPixel = this.getResources().getDisplayMetrics().widthPixels;
        screenHeightPixel = this.getResources().getDisplayMetrics().heightPixels;

        CIRCLE_RADIUS       = screenWidthPixel * 0.02f;
        CIRCLE_SPACE        = screenWidthPixel * 0.02f;
        Y_POSITION        = screenHeightPixel * 0.01f;
        float STROKE_WIDTH        = screenWidthPixel * 0.0035f;
        int CIRCLE_STROKE_COLOR = Color.RED;
        int CIRCLE_FILL_COLOR   = Color.GREEN;

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(CIRCLE_FILL_COLOR);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(CIRCLE_STROKE_COLOR);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(STROKE_WIDTH);
    }


    public void setIndicatorsCount(int value) {
        count = value;
        computeIndicatorWidth();
    }


    public void setCurrentPage(int value) {
        currentPageIndex = value;
    }


    public void setPercent(float percent) {
        this.percent = percent;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < count; i++) {
            boolean canDrawFill = false;
            float yPosition = Y_POSITION;
            float radius = CIRCLE_RADIUS;

            if (i == currentPageIndex) {
                fillPaint.setAlpha((int) ((1.0f - percent) * 255));
                radius *= 1.3;
                canDrawFill = true;
            }

            if (percent > 0) {
                if (i == currentPageIndex + 1) {
                    fillPaint.setAlpha((int) (percent * 255));
                    canDrawFill = true;
                }
            }
            canvas.drawCircle(offsetX + i * (CIRCLE_RADIUS + CIRCLE_SPACE), yPosition, radius / 2.0f, strokePaint);

            if (canDrawFill) {
                canvas.drawCircle(offsetX + i * (CIRCLE_RADIUS + CIRCLE_SPACE), yPosition, radius / 2.0f, fillPaint);
            }
        }
    }

    private void computeIndicatorWidth() {
        float indicatorWidth = count * (CIRCLE_RADIUS + CIRCLE_SPACE);
        offsetX = (screenWidthPixel - indicatorWidth) / 2;
    }
}
