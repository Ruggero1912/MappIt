package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;

public class Image {

    private String path;
    private static final String defaultPath = PropertyPicker.getProperty(PropertyPicker.defaultPicKey);

    public Image(){
        this.path = defaultPath;
    }

    public Image(String path){
        if(path == null || path.equals("")){
            this.path = defaultPath;
        }
        else
            this.path = path;
    }

    public String getPath() {
        return this.path;
    }
    public void setPath( String path) { this.path = path; }

    @Override
    public String toString(){
        return this.getPath();
    }

}
