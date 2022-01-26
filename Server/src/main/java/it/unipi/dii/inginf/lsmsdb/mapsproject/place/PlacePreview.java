package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.neo4j.driver.Value;

public class PlacePreview {

    public final static String NEO_KEY_TIME_VISIT = "datetime";

    private String _id;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timeVisit;

    /**
     * We exploit this constructor to parse a Place Preview object from a Neo4j Node
     * @param valueFromPlaceNode corresponds to the value gathered from the Place Node in Neo4j
     */
    public PlacePreview(Value valueFromPlaceNode) {
        this._id =  valueFromPlaceNode.get(Place.NEO_KEY_ID).asString();
        this.name =  valueFromPlaceNode.get(Place.KEY_NAME).asString();
    }

    /**
     * We exploit this constructor to parse a Place Preview object from a Neo4j Node
     * @param valueFromPlaceNode corresponds to the value gathered from the Place Node in Neo4j
     * @param visitInfos the Neo4j infos about a visit done by a given user to that place
     */
    public PlacePreview(Value valueFromPlaceNode, Value visitInfos) {
        this._id =  valueFromPlaceNode.get(Place.NEO_KEY_ID).asString();
        this.name =  valueFromPlaceNode.get(Place.KEY_NAME).asString();
        this.timeVisit = visitInfos.get(PlacePreview.NEO_KEY_TIME_VISIT).toString();
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getTimeVisit() { return this.timeVisit;}


}
