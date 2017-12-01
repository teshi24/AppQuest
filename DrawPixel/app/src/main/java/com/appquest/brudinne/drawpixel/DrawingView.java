package com.appquest.brudinne.drawpixel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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

    private Path drawPath                   = new Path();
    private Paint drawPaint                 = new Paint();
    private Paint linePaint                 = new Paint();
    private Paint initPaint                 = new Paint();
    private boolean isErasing               = false;
    private ArrayList<DrawingPixel> pixelList = new ArrayList();

    //private ArrayList<ArrayList<Paint>> pixels;

    public ArrayList<DrawingPixel> getPixelList() {
        return pixelList;
    }

    public void setPixelList(ArrayList<DrawingPixel> pixelList) {
        this.pixelList = pixelList;
    }

    public ArrayList<ArrayList<Paint>> getPixels() {
      //  return pixels;
        return null;
    }

    public void setPixels(ArrayList<ArrayList<Paint>> pixels) {
        //this.pixels = pixels;
    }

    Context context;
    Canvas canvas;
    int stepSizeX;
    int stepSizeY;
    int maxX;
    int maxY;
    int pixelCanvas;
    int pixelCell;

    Color white = new Color();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        /*
        maxX = getWidth();
        maxY = getHeight();
        pixelCanvas = maxX;
        pixelCell = (pixelCanvas - (GRID_SIZE+1))/GRID_SIZE;

        stepSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        stepSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);
        */

        drawPaint.setAntiAlias(true);

        initPaint.setAntiAlias(true);
        initPaint.setColor(0xFFFFFFFF);

        linePaint.setAntiAlias(true);
        linePaint.setColor(0xFF666666);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);

        /*
        pixelList = new ArrayList();
        for(int i = 0; i<GRID_SIZE; i++){
            for(int j = 0; j<GRID_SIZE; j++){
                pixelList.add(new DrawingPixel(new Rect((i*stepSizeX), (j*stepSizeY), ((i + 1)*stepSizeX), ((j + 1)*stepSizeY)),Color.WHITE));
            }
        }
        */
         /*
        pixels = new ArrayList();
        for(int i = 0; i<GRID_SIZE; i++){
            ArrayList<Paint> pixelY = new ArrayList();
            for(int j = 0; j<GRID_SIZE; j++){
                pixelY.add(initPaint);
            }
            pixels.add(pixelY);
        }
        */
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;

        maxX = getWidth();
        maxY = getHeight();

        pixelCanvas = maxX;
        pixelCell = (pixelCanvas - (GRID_SIZE+1))/GRID_SIZE;

        stepSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        stepSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);

        if(pixelList == null || pixelList.isEmpty()){
            drawBackground(-1);
        }

        Bitmap bitmap = Bitmap.createBitmap(pixelCanvas,pixelCanvas,Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.canvas.drawBitmap(bitmap, new Matrix(), initPaint);


        // draw border
/*
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                canvas.drawRect(i * stepSizeX, j * stepSizeY, (i + 1) * stepSizeX, (j + 1) * stepSizeY, linePaint);
            }
        }
//*/
///*
        drawBigPixel(canvas);

        for(int i = 0; i < GRID_SIZE*GRID_SIZE; i++){
            canvas.drawRect(pixelList.get(i).getRect(),linePaint);
        }
//*/
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
                pixelList.add(new DrawingPixel(new Rect(i*stepSizeX, j*stepSizeY, (i + 1)*stepSizeX-1, (j + 1)*stepSizeY-1), color));
            }
        }
    }

    private void drawBigPixel(Canvas canvas){
        for(int i = 0; i<GRID_SIZE*GRID_SIZE; i++){
            DrawingPixel bigPixel = pixelList.get(i);
            Paint p = new Paint();
            p.setColor(bigPixel.getColor());
            canvas.drawRect(bigPixel.getRect(),p);
        }
        /*
        for(int i = 0; i<GRID_SIZE; i++){
            for(int j = 0; j<GRID_SIZE; j++){
                try{
                    Paint p;
                    if(pixels.get(i).get(j) != null) {
                        p = new Paint(pixels.get(i).get(j));
                    }else{
                        p = initPaint;
                    }
                    int x = i*stepSizeX;
                    int y = j*stepSizeY;
                    Rect rect = new Rect(x, y, x+stepSizeX-1, y+stepSizeY-1);
                    canvas.drawRect(rect, p);
                }catch (Exception ex){
                    Toast.makeText(context, "color error: " + pixels.get(i).get(j), Toast.LENGTH_SHORT).show();
                }
            }
        }
        */
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                if(isErasing){
                    drawPaint.setColor(Color.parseColor(""+R.color.white));
                }
                savePixelForDraw((int)touchX, (int)touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                //todo: besseres handling
                if(touchX < maxX && touchY < maxY && touchX >= 0 && touchY >= 0){
                    drawPath.lineTo(touchX, touchY);
                    savePixelForDraw((int)touchX, (int)touchY);
                    break;
                }
                event.setAction(MotionEvent.ACTION_UP);
            case MotionEvent.ACTION_UP:
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void savePixelForDraw(int x, int y){
        int fieldX = (int)Math.floor(x / stepSizeX);
        int fieldY = (int)Math.floor(y / stepSizeY);
        pixelList.get((fieldY+(fieldX*13))).setColor(drawPaint.getColor());

        //save touched pixel
        //Paint p = new Paint(drawPaint);
        //pixels.get(fieldX).set(fieldY, p);
    }

    private void drawPixel(int x, int y, Canvas canvas){
        int fieldX = (int)Math.floor(x / stepSizeX);
        int fieldY = (int)Math.floor(y / stepSizeY);

        int i = stepSizeX*fieldX;
        int j = stepSizeY*fieldY;
        Rect rect = new Rect(i, j, i+stepSizeX-1, j+stepSizeY-1);

        canvas.drawRect(rect, drawPaint);

        /*
        for(int i = stepSizeX * fieldX; i < stepSizeX * (fieldX+1); i++){
            for(int j = stepSizeY * fieldY; j < stepSizeY * (fieldY+1); j++) {
                //Rect rect = new Rect(i, j, i+stepSizeX-1, j+stepSizeY-1);
                //canvas.drawRect(rect, drawPaint);
                canvas.drawPoint(i,j,drawPaint);
                invalidate();
            }
        }
        */
    }

    public void startNew() {
        pixelList = null;
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
}
