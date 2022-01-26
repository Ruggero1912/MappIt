package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.neo4j.driver.Value;

public class UserPreview {

    public static final String NEO_USER_LABEL = PropertyPicker.getNodeLabel(PropertyPicker.userEntity);
    public static final String NEO_KEY_ID = PropertyPicker.getNodePropertyKey(PropertyPicker.userEntity, "id");
    public static final String NEO_KEY_USERNAME = PropertyPicker.getNodePropertyKey(PropertyPicker.userEntity, "username");

    private String id;
    private String username;

    /**
     * We exploit this constructor to parse a User Preview object from a Neo4j Node
     * @param userNode corresponds to the value gathered from the User Node in Neo4j
     */
    public UserPreview(Value userNode){
        this.id = userNode.get(NEO_KEY_ID).asString();
        this.username = userNode.get(NEO_KEY_USERNAME).asString();
    }
    public String getId(){ return this.id; }
    public String getUsername(){ return this.username;}
}
