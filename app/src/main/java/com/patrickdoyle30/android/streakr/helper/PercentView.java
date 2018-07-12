package com.patrickdoyle30.android.streakr.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.patrickdoyle30.android.streakr.R;

public class PercentView extends View {

    //View to display in goalsHabitsFeatureActivity that will display a pie chart that displays the
    //percentage of the goal/habit that is complete

    public PercentView(Context context) {
        super(context);
        init();
    }

    public PercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PercentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.colorAccent));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        bgpaint = new Paint();
        bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIcons));
        bgpaint.setAntiAlias(true);
        bgpaint.setStyle(Paint.Style.FILL);
        rect = new RectF();
    }

    Paint paint;
    Paint bgpaint;
    RectF rect;
    float percentage = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw background circle anyway
        int left = 0;
        int width = getWidth();
        int top = 0;
        rect.set(left, top, left + width, top + width);
        canvas.drawArc(rect, -90, 360, true, bgpaint);
        if (percentage != 0) {
            canvas.drawArc(rect, -90, (360 * percentage), true, paint);
        }
    }

    public void setPercentage(float percentage) {

        //Set the percentage on the view to fill up the pie chart appropriately
        this.percentage = percentage / 100;
        invalidate();
    }

    public void setColor(int theme) {
        //Set the color of the percent View based on whatever theme the user selected
        if (theme == R.style.PinkAppTheme) {
            paint.setColor(getContext().getResources().getColor(R.color.colorAccentPink));
            bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIconsPink));
        } else if (theme == R.style.BlueAppTheme) {
            paint.setColor(getContext().getResources().getColor(R.color.colorAccentBlue));
            bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIconsBlue));
        } else if (theme == R.style.RedAppTheme) {
            paint.setColor(getContext().getResources().getColor(R.color.colorAccentRed));
            bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIconsRed));
        } else if (theme == R.style.BlackAppTheme) {
            paint.setColor(getContext().getResources().getColor(R.color.colorAccentBlack));
            bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIconsBlack));
        }else {
            paint.setColor(getContext().getResources().getColor(R.color.colorAccent));
            bgpaint.setColor(getContext().getResources().getColor(R.color.colorTextAndIcons));
        }
    }
}