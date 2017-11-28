package com.appquest.brudinne.drawpixel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

/**
 * Die DrawingView ist für die Darstellung und Verwaltung der Zeichenfläche
 * zuständig.
 */
public class DrawingView extends View {

    private static final int GRID_SIZE = 13;

    private Path drawPath                   = new Path();
    private Paint drawPaint                 = new Paint();
    private Paint linePaint                 = new Paint();
    private boolean isErasing               = false;
    private HashMap<Float, Float> positions = new HashMap<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        linePaint.setColor(0xFF666666);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int maxX = canvas.getWidth();
        final int maxY = canvas.getHeight();

        final int stepSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        final int stepSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);

        // TODO Zeichne das Gitter
        // Draw horizontal lines
        for (int i = 1; i < GRID_SIZE; i++) {
            canvas.drawLine(i * stepSizeX, 0, i * stepSizeX, maxY, linePaint);
        }
        // Draw vertical lines
        for (int i = 1; i < GRID_SIZE; i++) {
            canvas.drawLine(0, i * stepSizeY, maxX, i * stepSizeY, linePaint);
        }
        // draw border
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                //if (cellChecked[i][j]) {

                    canvas.drawRect(i * stepSizeX, j * stepSizeY,
                            (i + 1) * stepSizeX, (j + 1) * stepSizeY,
                            linePaint);
                //}
            }
        }
        // Zeichnet einen Pfad der dem Finger folgt
        canvas.drawPath(drawPath, drawPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                // save touched pixels
                positions.put(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                // Save touched pixels
                positions.put(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                for(Float x : positions.keySet()){
                    Float y = positions.get(x);
                    if(!isErasing){
                        // TODO: add chosen color to big pixels
                    } else {
                        // TODO: Delete Color in big pixels
                    }
                }
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void startNew() {
        // TODO Gitter löschen
        invalidate();
    }

    public void setErase(boolean isErase) {
        isErasing = isErase;
        if (isErasing) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    public boolean getIsErasing() {
        return isErasing;
    }

    public void setColor(String color) {
        invalidate();
        drawPaint.setColor(Color.parseColor(color));
    }

/*    public Class Position {
        private Float posX;
        private Float posY;

        public Position(Float posX, Float posY){
            this.posX = posX;
        }
    }*/
}
