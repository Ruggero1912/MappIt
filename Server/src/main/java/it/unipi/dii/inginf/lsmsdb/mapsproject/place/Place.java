package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Place {

    public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "id");
    public static final String KEY_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "name");
    public static final String KEY_FITS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "fits");
    public static final String KEY_LOC = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "loc");
    public static final String KEY_IMAGE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "image");
    public static final String KEY_OSMID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "osmID");
    public static final String KEY_POSTS_ARRAY = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "postsArray");
    public static final String KEY_FAVOURITES = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "favouritesCounter");
    public static final String KEY_COORDINATES = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "coordinates");


    private String _id;
    private String name;
    private Coordinate coordinates;
    private List<String> fits;
    private List<String> posts; // here we store the ids of the posts done by the user
    private Image image;
    private String osmId;
    private int favouritesCounter;

    public Place(String id, String placeName) {
        this._id=id;
        this.name=placeName;
    }

    public Place(String id, String name, Coordinate coords, Image img) {
        this._id = id;
        this.name = name;
        this.coordinates = coords;
        this.image = img;
    }

    public Place (Document doc){
        this._id = doc.get(KEY_ID).toString();
        this.name = doc.get(KEY_NAME).toString();
        this.fits = (List<String>) doc.get(KEY_FITS, List.class);   // TODO: test it (does this cast works properly)
        this.posts = (List<String>) doc.get(KEY_POSTS_ARRAY, List.class);
        Document loc = doc.get(KEY_LOC, Document.class);
        List<Double> coord = loc.getList(KEY_COORDINATES, Double.class);
        double lon = coord.get(0);
        double lat = coord.get(1);
        this.coordinates = new Coordinate(lat, lon);
        this.osmId = doc.getString(KEY_OSMID);
        this.favouritesCounter = doc.getInteger(KEY_FAVOURITES, 0);
        this.image = new Image();
        this.image.setPath(doc.getString(KEY_IMAGE));
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinate getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(Coordinate c) {
        this.coordinates = c;
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
                    "id=" + _id +
                    ", name='" + name + '\'' +
                    ", aliases='" +
                    ", Coordinates{ ";

                    if(this.coordinates!=null)
                        ret += coordinates.toString();

                    ret+='}';

        return ret;
    }
}