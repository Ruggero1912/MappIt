package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Place {

    private String _id;
    private String name;
    private List<Coordinate> coordinates;
    private List<String> fits;
    private List<Post> posts;
    private Image image;

    public Place(String id, String name, List<Coordinate> coords, Image img) {
        this._id = id;
        this.name = name;
        this.coordinates = new ArrayList<Coordinate>();
        for(Coordinate c : coords){
            this.coordinates.add(c);
        }
        this.image = img;
    }

    public Place (Document doc){
        this._id = doc.get("_id").toString();
        this.name = doc.get("name").toString();
        /*
        this.coordinates = ?
        this.fits = ?
        this.posts = ?
        */
        this.image = new Image();
        this.image.setPath(doc.get("image").toString());
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

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public void addCoordinates(Coordinate c) {
        this.coordinates.add(c);
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

                    for(Coordinate c : this.coordinates){
                        ret += c.toString();
                        ret += " / ";
                    }

                    ret+='}';

        return ret;
    }
}