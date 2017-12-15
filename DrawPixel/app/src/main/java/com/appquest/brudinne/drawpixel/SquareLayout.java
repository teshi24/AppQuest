package com.appquest.brudinne.drawpixel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

// http://stackoverflow.com/questions/2948212/android-layout-with-sqare-buttons

/**
 * to achieve the grid layout
 */
public class SquareLayout extends LinearLayout {
    private int mScale = 1;

    /**
     * @param context related activity
     */
    public SquareLayout(Context context) {
        super(context);
    }

    /**
     * @param context related activity
     * @param attrs
     */
    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getWidth();
        getHeight();
        int width   = MeasureSpec.getSize(widthMeasureSpec);
        int height  = MeasureSpec.getSize(heightMeasureSpec);

        int scale = (int) (mScale * height + 0.5);

        if (width > scale) {
            width = scale;
        } else {
            height = (int) (width / mScale + 0.5);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}
