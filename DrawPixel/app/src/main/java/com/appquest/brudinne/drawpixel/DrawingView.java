package com.appquest.brudinne.drawpixel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Canvas handling
 */
public class DrawingView extends View {

    // grid
    private static final int GRID_SIZE = 13;
    private int pixelSizeX;
    private int pixelSizeY;
    private int maxX;
    private int maxY;

    // picture
    private ArrayList<DrawingPixel> pixelList           = new ArrayList();
    private ArrayList<ArrayList<DrawingPixel>> steps    = new ArrayList();

    // paints
    private Paint drawPaint = new Paint();
    private Paint linePaint = new Paint();
    private Paint initPaint = new Paint();

    // tools
    private boolean isErasing   = false;
    private int util            = 0;

    // create, draw and touch
    // ----------------------

    /**
     * @param context related activity
     * @param attrs
     */
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        drawPaint.setAntiAlias(true);

        initPaint.setAntiAlias(true);
        initPaint.setColor(0xFFFFFFFF);

        linePaint.setAntiAlias(true);
        linePaint.setColor(0xFF666666);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);

        saveStep();
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        maxX        = getWidth();
        maxY        = getHeight();
        pixelSizeX  = (int) Math.ceil((double) maxX / GRID_SIZE);
        pixelSizeY  = (int) Math.ceil((double) maxY / GRID_SIZE);

        if (pixelList == null || pixelList.isEmpty()) {
            drawBackground(-1);
        }
        drawBigPixel(canvas);

        // draw border
        for(DrawingPixel pixel: pixelList) {
            canvas.drawRect(pixel.getRect(), linePaint);
        }
        /*
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            canvas.drawRect(pixelList.get(i).getRect(), linePaint);
        }
        */
        canvas.drawLine(0, maxY, maxX, maxY, linePaint);
        canvas.drawLine(maxX, 0, maxX, maxY, linePaint);
    }

    /**
     * @param event touch event
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (util == 0) {
                    savePixelForDraw((int) touchX, (int) touchY);
                } else if (util == 1) {
                    drawBackground(drawPaint.getColor());
                } else {
                    changeColor(getPixelColor((int) touchX, (int) touchY), drawPaint.getColor());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (util == 0) {
                    if (touchX >= 0 && touchX < maxX && touchY >= 0 && touchY < maxY) {
                        savePixelForDraw((int) touchX, (int) touchY);
                        break;
                    }
                }
            case MotionEvent.ACTION_UP:
                saveStep();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    // for all utils
    // -------

    /**
     * get a empty picture
     */
    public void startNew() {
        pixelList   = null;
        steps       = new ArrayList();
        saveStep();
        invalidate();
    }

    /**
     * draw whole grid background in a color
     * @param color background color, if -1 = background gets white
     */
    private void drawBackground(int color) {
        if (color == -1) {
            color = Color.WHITE;
        }
        pixelList = new ArrayList();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                pixelList.add(new DrawingPixel(new Rect(i * pixelSizeX, j * pixelSizeY, (i + 1) * pixelSizeX - 1, (j + 1) * pixelSizeY - 1), color));
            }
        }
    }

    /**
     * draw all DrawingPixels
     * @param canvas
     */
    private void drawBigPixel(Canvas canvas) {
        Paint p = new Paint();
        for(DrawingPixel pixel: pixelList) {
            p.setColor(pixel.getColor());
            canvas.drawRect(pixel.getRect(), p);
        }
        /*
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            DrawingPixel bigPixel = pixelList.get(i);
            p.setColor(bigPixel.getColor());
            canvas.drawRect(bigPixel.getRect(), p);
        }
        */
    }

    // brush
    // -----

    /**
     * change color of DrawingPixel in which a touchEvent occurred
     * @param x touchEvent x
     * @param y touchEvent y
     */
    private void savePixelForDraw(int x, int y) {
        int fieldX  = (int) Math.floor(x / pixelSizeX);
        int fieldY  = (int) Math.floor(y / pixelSizeY);
        pixelList.get((fieldY + (fieldX * 13))).setColor(drawPaint.getColor());
    }

    // change
    // ------

    /**
     * get color of DrawingPixel in which a touchEvent occurred
     * @param x touchEvent x
     * @param y touchEvent y
     * @return color of touched DrawingPixel
     */
    private int getPixelColor(int x, int y) {
        int fieldX  = (int) Math.floor(x / pixelSizeX);
        int fieldY  = (int) Math.floor(y / pixelSizeY);
        return pixelList.get((fieldY + (fieldX * 13))).getColor();
    }

    /**
     * change color of all DrawingPixels with oldColor to newColor
     * @param oldColor
     * @param newColor
     */
    private void changeColor(int oldColor, int newColor) {
        if (oldColor != newColor) {
            for (DrawingPixel pixel : pixelList) {
                if (pixel.getColor() == oldColor) {
                    pixel.setColor(newColor);
                }
            }
        }
    }

    // undo
    // ----

    /**
     * save a step into the undo list
     */
    public void saveStep() {
        // max undo's
        if (steps.size() == 10) {
            steps.remove(0);
        }
        if (pixelList != null) {
            ArrayList<DrawingPixel> temp = new ArrayList();
            for (DrawingPixel pixel : pixelList) {
                temp.add(pixel.clone());
            }
            steps.add(temp);
        } else {
            steps.add(null);
        }
    }

    /**
     * undo the last step
     * max 10 steps possible
     * @return true if undo was possible, false if not possible
     */
    public boolean undo() {
        int lastIndex = steps.size() - 1;
        if (lastIndex >= 0) {
            steps.remove(lastIndex);
            if (lastIndex != 0) {
                lastIndex--;
                pixelList = steps.get(lastIndex);
                invalidate();
                return true;
            }
        }
        if (steps.isEmpty()) {
            saveStep();
        }
        return false;
    }

    // getter and setter
    // -----------------

    public void setErase(boolean isErase) {
        isErasing = isErase;
        if (isErasing) {
            drawPaint.setColor(Color.WHITE);
        }
    }

    public boolean getIsErasing() {
        return isErasing;
    }

    public void setColor(String color) {
        invalidate();
        drawPaint.setColor(Color.parseColor(color));
    }

    public int getPixelSizeX() {
        return pixelSizeX;
    }

    public int getPixelSizeY() {
        return pixelSizeY;
    }

    public void setUtil(int util) {
        this.util = util;
    }

    public ArrayList<DrawingPixel> getPixelList() {
        return pixelList;
    }
}
