package com.appquest.brudinne.treasurehunt;

import java.util.ArrayList;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

    public MyItemizedOverlay(Drawable pDefaultMarker) {
        super(pDefaultMarker);
        // TODO Auto-generated constructor stub
    }

    public void addItem(GeoPoint p, String title, String snippet){
        OverlayItem overlayItem = new OverlayItem(title, snippet, p);
        overlayItemList.add(overlayItem);
        populate();
    }

    public void deleteItem(OverlayItem overlayItem){
        overlayItemList.remove(overlayItem);
        populate();

    }

    @Override
    public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected OverlayItem createItem(int arg0) {
        // TODO Auto-generated method stub
        return overlayItemList.get(arg0);
    }

    @Override
    public int size() {
        return overlayItemList.size();
    }

}
