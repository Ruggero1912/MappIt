package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;

import java.util.ArrayList;
import java.util.List;

public class Place {
    public enum PlaceType {WOODLAND, URBAN, MOUNTAIN, SEASIDE, MONUMENT, COUNTRYSIDE, ABANDONED}

    private int id;
    private String name;
    private List<String> alternativeNames;
    List<Coordinate> coordinates;
    private List<PlaceType> placeTypes;
    private String houseNumber;
    private String street;
    private String countryCode;
    private int postCode;
    private String county;      //province
    private String city;
    private String district;    //fraction of the province
    private Image image;

    public Place(int id, String name, List<Coordinate> coords, List<String> aliases, List<PlaceType> pTypes, Image img) {
        this.id = id;
        this.name = name;
        this.coordinates = new ArrayList<Coordinate>();
        for(Coordinate c : coords){
            this.coordinates.add(c);
        }
        this.alternativeNames = aliases;
        this.placeTypes = pTypes;
        this.image = img;
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

    public void setPlaceTypes(List<PlaceType> pTypes) {
        this.placeTypes = pTypes;
    }

    public List<PlaceType> getPlaceTypes() {
        return this.placeTypes;
    }

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public void addCoordinates(Coordinate c) {
        this.coordinates.add(c);
    }

    public String getAddress() {
        return street;
    }

    public void setAddress(String street) {
        this.street = street;
    }

    public Image getImagePath() {
        return image;
    }

    public void setImagePath(Image path) {
        this.image = path;
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
                    ret+="' ";
                    ret += "GeoLocation{ id=" + id;

                    for(Coordinate c : this.coordinates){
                        ret += c.toString();
                        ret += " / ";
                    }

                    ret+='}';

        return ret;
    }
}