package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

public class Coordinate {

    private double latitude;
    private double longitude;

    public Coordinate(double lat, double lon){
        this.latitude = lat;
        this.longitude = lon;
    }

    public void setLatitude(double lat){
        this.latitude = lat;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return " Coordinate: { lat: " + this.latitude + " / lon: " + this.longitude + "} ";
    }
}
