package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

public class Marker {

    private int id;
    private double latitude;
    private double longitude;
    private User owner;
    private GeoLocation geoLocation;

    public Marker(int id, User u, GeoLocation pos) {
        this.id = id;
        this.owner = u;
        this.geoLocation = pos;
        this.latitude = pos.getLatitude();
        this.longitude = pos.getLongitude();
    }

    public Marker(int id, User u, Place p) {
        this.id = id;
        this.owner = u;
        this.geoLocation = p.getPosition();
        this.latitude = this.geoLocation.getLatitude();
        this.longitude = this.geoLocation.getLongitude();
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

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "id=" + id +
                ", owner_id='" + owner.getId() + '\'' +
                ", owner_name='" + owner.getName() + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
