package ch.appquest.brudinne.memory;

import android.graphics.Bitmap;

public class PictureCard extends Card {
    private Bitmap picture;
    private String filepath;
    private String filename;
    private String description;

    public PictureCard(Bitmap picture, String description, String filepath, String filename){
        this.picture     = picture;
        this.description = description;
        this.filepath    = filepath;
        this.filename    = filename;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getDescription() {
        return description;
    }

    public String getFilepath() { return filepath; }

    public String getFilename() { return filename; }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setFilepath(String filepath) { this.filepath = filepath; }

    public void setFilename(String filename) { this.filename = filename; }
}
