package com.appquest.brudinne.drawpixel;

import android.graphics.Color;
import android.graphics.Rect;

public class DrawingPixel {
    private Rect rectOnCanvas = null;
    private Color color;

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public Rect getRectOnDrawingPitchCanvas() {
        return this.rectOnCanvas;
    }

    public void setRectOnDrawingPitchCanvas(Rect rectOnCanvas) {
        this.rectOnCanvas = rectOnCanvas;
    }
}
