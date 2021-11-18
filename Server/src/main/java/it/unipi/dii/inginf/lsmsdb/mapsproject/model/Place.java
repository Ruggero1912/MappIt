package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.util.List;

public class Place {
    public enum PlaceTypes {WOODLAND, URBAN, MOUNTAIN, SEASIDE, MONUMENT, COUNTRYSIDE, ABANDONED};

    private int id;
    public String name;
    public GeoLocation position;
    public List<String> alternativeNames;
    public List<PlaceTypes> placeTypes;
    public String imagePath;

    public Place(int id, String name, GeoLocation pos, List<String> aliases, List<PlaceTypes> pTypes, String imgPath) {
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

    public void setPlaceTypes(List<PlaceTypes> pTypes) {
        this.placeTypes = pTypes;
    }

    public List<PlaceTypes> getPlaceTypes() {
        return this.placeTypes;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    public GeoLocation getPosition(){
        return position;
    }

    public void setPosition(GeoLocation g){
        this.position = g;
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

                        for (PlaceTypes pt : placeTypes) {
                            ret += pt.toString() + ", ";
                        }
                    }
                    ret+="'";
        return ret;
    }
}