package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.util.List;

public class Place {
    public enum PlaceType {WOODLAND, URBAN, MOUNTAIN, SEASIDE, MONUMENT, COUNTRYSIDE, ABANDONED}

    private int id;
    private String name;
    private List<String> alternativeNames;
    private GeoLocation position;
    private List<PlaceType> placeTypes;
    private String imagePath;

    public Place(int id, String name, GeoLocation pos, List<String> aliases, List<PlaceType> pTypes, String imgPath) {
        this.id = id;
        this.name = name;
        this.position = pos;
        this.alternativeNames = aliases;
        this.placeTypes = pTypes;
        this.imagePath = imgPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAlternativeNames() {
        return this.alternativeNames;
    }

    public void setAlternativeNames(List<String> aliases) {
        this.alternativeNames = aliases;
    }

    public GeoLocation getPosition(){
        return position;
    }

    public void setPosition(GeoLocation g){
        this.position = g;
    }

    public void setPlaceTypes(List<PlaceType> pTypes) {
        this.placeTypes = pTypes;
    }

    public List<PlaceType> getPlaceTypes() {
        return this.placeTypes;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    @Override
    public String toString() {
        String ret = "Place{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", aliases='";
                    if(this.alternativeNames != null)
                        for (String an : this.alternativeNames) {
                            ret+=an+", ";
                        }
                    ret+="'";
                    if(this.placeTypes != null) {
                        ret += " place types='";

                        for (PlaceType pt : placeTypes) {
                            ret += pt.toString() + ", ";
                        }
                    }
                    ret+="'";
        return ret;
    }
}