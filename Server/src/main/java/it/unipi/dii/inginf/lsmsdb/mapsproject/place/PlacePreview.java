package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import org.neo4j.driver.Value;

public class PlacePreview {

    private String _id;
    private String name;

    /**
     * We exploit this constructor to parse a Place Preview object from a Neo4j Node
     * @param valueFromPlaceNode corresponds to the value gathered from the Place Node in Neo4j
     */
    public PlacePreview(Value valueFromPlaceNode) {
        this._id =  valueFromPlaceNode.get(Place.NEO_KEY_ID).asString();
        this.name =  valueFromPlaceNode.get(Place.KEY_NAME).asString();
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }


}
