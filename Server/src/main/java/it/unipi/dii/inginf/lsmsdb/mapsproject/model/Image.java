package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;

public class Image {

    private String path;
    private final String defaultPath = PropertyPicker.getProperty(PropertyPicker.defaultPicKey);

    public Image(){
        this.path = defaultPath;
    }

    public String getPath() {
        return path;
    }
    public void setPath( String path) { this.path = path; }
}
