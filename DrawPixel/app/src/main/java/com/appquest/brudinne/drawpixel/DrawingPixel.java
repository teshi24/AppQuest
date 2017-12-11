package com.appquest.brudinne.drawpixel;

import android.graphics.Rect;

public class DrawingPixel {
    private Rect rect = null;
    private int color;

    /**
     * @param rect from real pixels
     * @param color pixel color
     */
    public DrawingPixel(Rect rect, int color) {
        this.rect = rect;
        this.color = color;
    }

    /**
     * @return copy of the pixel
     */
    @Override
    protected DrawingPixel clone() {
        return new DrawingPixel(rect, color);
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
    public String toString() {
        return "";
    }
}
