package com.appquest.brudinne.drawpixel;

import android.graphics.Rect;

public class DrawingPixel {
    private Rect rect = null;
    private int color;

    public DrawingPixel(Rect rect, int color) {
        this.rect = rect;
        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public Rect getRect() {
        return this.rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    @Override
    public String toString(){
        return "";
    }
}
