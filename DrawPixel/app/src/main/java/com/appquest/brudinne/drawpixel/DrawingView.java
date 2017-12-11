package com.appquest.brudinne.drawpixel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Die DrawingView ist für die Darstellung und Verwaltung der Zeichenfläche
 * zuständig.
 */
public class DrawingView extends View {

    private static final int GRID_SIZE = 13;

    private Path drawPath                     = new Path();
    private Paint drawPaint                   = new Paint();
    private Paint linePaint                   = new Paint();
    private Paint initPaint                   = new Paint();
    private boolean isErasing                 = false;
    private ArrayList<DrawingPixel> pixelList = new ArrayList();
    private ArrayList<ArrayList<DrawingPixel>> steps = new ArrayList();

    private int util = 0;

    private Context context;
    private Canvas canvas;
    private int pixelSizeX;
    private int pixelSizeY;
    private int maxX;
    private int maxY;
    private int pixelCanvas;
    private int pixelCell;

    private Color white = new Color();

    public boolean undo(){
        int lastIndex = steps.size()-1;
        if(lastIndex >= 0) {
            steps.remove(lastIndex);
            if(lastIndex != 0){
                lastIndex--;

                pixelList = steps.get(lastIndex);
                //steps.remove(lastIndex);
                invalidate();
                return true;
            }
        }
        if(steps.isEmpty()){
            saveStep();
        }
        return false;
    }

    public void saveStep(){
        // max undo's
        if(steps.size() == 10){
            steps.remove(0);
        }
        if(steps == null){
            steps.add(null);
        }

        if(pixelList != null){
            //steps.add((ArrayList<DrawingPixel>)pixelList.clone());
            ArrayList<DrawingPixel> temp = new ArrayList();
            for(DrawingPixel pixel: pixelList){
                temp.add(pixel.clone());
            }
            steps.add(temp);
        }else{
            steps.add(null);
        }
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        drawPaint.setAntiAlias(true);

        initPaint.setAntiAlias(true);
        initPaint.setColor(0xFFFFFFFF);

        linePaint.setAntiAlias(true);
        linePaint.setColor(0xFF666666);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);
        saveStep();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;

        maxX = getWidth();
        maxY = getHeight();

        pixelCanvas = maxX;
        pixelCell = (pixelCanvas - (GRID_SIZE+1))/GRID_SIZE;

        pixelSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        pixelSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);

        if(pixelList == null || pixelList.isEmpty()){
            drawBackground(-1);
        }

        Bitmap bitmap = Bitmap.createBitmap(pixelCanvas,pixelCanvas,Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.canvas.drawBitmap(bitmap, new Matrix(), initPaint);


        drawBigPixel(canvas);

        // draw border
        for(int i = 0; i < GRID_SIZE*GRID_SIZE; i++){
            canvas.drawRect(pixelList.get(i).getRect(),linePaint);
        }
        // TODO: check if we can get maxX and max to something dividing through 13
        canvas.drawLine(0, maxY, maxX, maxY, linePaint);
        canvas.drawLine(maxX, 0, maxX, maxY, linePaint);

        // Zeichnet einen Pfad der dem Finger folgt
        // canvas.drawPath(drawPath, drawPaint);
    }

    private void drawBackground(int color){
        if(color == -1) {
            color = Color.WHITE;
        }
        pixelList = new ArrayList();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                pixelList.add(new DrawingPixel(new Rect(i* pixelSizeX, j* pixelSizeY, (i + 1)* pixelSizeX -1, (j + 1)* pixelSizeY -1), color));
            }
        }
    }

    private void drawBigPixel(Canvas canvas){
        Paint p = new Paint();
        for(int i = 0; i<GRID_SIZE*GRID_SIZE; i++){
            DrawingPixel bigPixel = pixelList.get(i);
            p.setColor(bigPixel.getColor());
            canvas.drawRect(bigPixel.getRect(),p);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //drawPath.moveTo(touchX, touchY);
                if(util == 0){
                    savePixelForDraw((int)touchX, (int)touchY);
                } else if(util == 1){
                    drawBackground(drawPaint.getColor());
                } else {
                    changeColor(getPixelColor((int) touchX, (int) touchY), drawPaint.getColor());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(util == 0) {
                    //todo: besseres handling
                    if (touchX < maxX && touchY < maxY && touchX >= 0 && touchY >= 0) {
                        //drawPath.lineTo(touchX, touchY);
                        savePixelForDraw((int) touchX, (int) touchY);
                        break;
                    }
                    event.setAction(MotionEvent.ACTION_UP);
                }
            case MotionEvent.ACTION_UP:
                //drawPath.reset();
                saveStep();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void changeColor(int oldColor, int newColor){
        if(oldColor != newColor) {
            for (DrawingPixel pixel : pixelList) {
                if (pixel.getColor() == oldColor) {
                    pixel.setColor(newColor);
                }
            }
        }
    }

    private int getPixelColor(int x, int y){
        int fieldX = (int)Math.floor(x / pixelSizeX);
        int fieldY = (int)Math.floor(y / pixelSizeY);
        return pixelList.get((fieldY+(fieldX*13))).getColor();
    }

    private void savePixelForDraw(int x, int y){
        int fieldX = (int)Math.floor(x / pixelSizeX);
        int fieldY = (int)Math.floor(y / pixelSizeY);
        pixelList.get((fieldY+(fieldX*13))).setColor(drawPaint.getColor());
    }

    public void startNew() {
        pixelList = null;
        steps = new ArrayList();
        saveStep();
        invalidate();
    }

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

    public int getUtil() {
        return util;
    }

    public void setUtil(int util) {
        this.util = util;
    }
    public ArrayList<DrawingPixel> getPixelList() {
        return pixelList;
    }

    public void setPixelList(ArrayList<DrawingPixel> pixelList) {
        this.pixelList = pixelList;
    }

    public ArrayList<ArrayList<DrawingPixel>> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<ArrayList<DrawingPixel>> steps) {
        this.steps = steps;
    }
}
