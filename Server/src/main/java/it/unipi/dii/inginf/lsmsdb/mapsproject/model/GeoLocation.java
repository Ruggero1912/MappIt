package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

public class GeoLocation {

    private int id;
    private double latitude;
    private double longitude;
    // maybe we could use a Map to distinguish fields inside the address
    private String address;

    public GeoLocation(int id, double lat, double lon, String addr) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lon;
        this.address = addr;
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
        return address;
    }

    public void setAddress(String addr) {
        this.address = addr;
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
