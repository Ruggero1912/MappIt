package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

public class GeoLocation {

    private int id;
    private double latitude;
    private double longitude;
    private String houseNumber;
    private String street;
    private String countryCode;
    private int postCode;

    private String county;      //province
    private String city;
    private String district;    //fraction of the province


    public GeoLocation(int id, double lat, double lon, String street) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lon;
        this.street = street;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double l) {
        this.latitude = l;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double l) {
        this.longitude = l;
    }

    public String getAddress() {
        return street;
    }

    public void setAddress(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "GeoLocation{" +
                "id=" + id +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
