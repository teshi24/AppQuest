package ch.appquest.brudinne.memory;

import android.graphics.Bitmap;

public class PictureCard extends Card {
    private Bitmap picture;
    private String description;

    public PictureCard(Bitmap picture, String description){
        this.picture     = picture;
        this.description = description;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }
}
