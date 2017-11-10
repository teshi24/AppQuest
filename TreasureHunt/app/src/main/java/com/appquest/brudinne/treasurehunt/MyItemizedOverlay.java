package com.appquest.brudinne.treasurehunt;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;


public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    public MyItemizedOverlay(Drawable pDefaultMarker) {
        super(pDefaultMarker);
    }

    @Override
    protected OverlayItem createItem(int i) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
        return false;
    }
}
