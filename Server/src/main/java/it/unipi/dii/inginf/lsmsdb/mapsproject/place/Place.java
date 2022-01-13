package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import org.bson.Document;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.List;

public class Place {

    public static final String KEY_PLACE_COLLECTION = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "collectionName");
    public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "id");
    public static final String KEY_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "name");
    public static final String KEY_FITS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "fits");
    public static final String KEY_LOC = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "loc");
    public static final String KEY_IMAGE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "image");
    public static final String KEY_OSMID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "osmID");
    public static final String KEY_POSTS_ARRAY = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "postsArray");
    public static final String KEY_FAVOURITES = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "favouritesCounter");
    public static final String KEY_COORDINATES = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "coordinates");
    public static final String KEY_TYPE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.placeCollection, "type");

    public static final String NEO_PLACE_LABEL = PropertyPicker.getNodeLabel(PropertyPicker.placeEntity);
    public static final String NEO_KEY_ID = PropertyPicker.getNodePropertyKey(PropertyPicker.placeEntity, "id");
    public static final String NEO_KEY_NAME = PropertyPicker.getNodePropertyKey(PropertyPicker.placeEntity, "name");


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

    /**
     * We exploit this constructor to parse a Place object from a Neo4j Node
     * @param value
     */
    public Place(Value value){
        this._id = value.get(Place.NEO_KEY_ID).asString();
        this.name = value.get(Place.NEO_KEY_NAME).asString();
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