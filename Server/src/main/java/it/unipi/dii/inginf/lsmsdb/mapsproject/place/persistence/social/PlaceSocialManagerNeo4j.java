package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class PlaceSocialManagerNeo4j implements PlaceSocialManager{

    private static final Logger LOGGER = Logger.getLogger(PlaceSocialManagerNeo4j.class.getName());

    private static final String NEO_LABEL_PLACE = PropertyPicker.getNodePropertyKey(PropertyPicker.placeEntity, "label");
    private static final String NEO_LABEL_USER = PropertyPicker.getNodePropertyKey(PropertyPicker.userEntity, "label");
    private static final String NEO_RELATION_VISITED = PropertyPicker.getGraphRelationKey("visited");
    private static final String NEO_RELATION_FOLLOWS = PropertyPicker.getGraphRelationKey("follows");
    private static final String NEO_RELATION_FAVOURITES = PropertyPicker.getGraphRelationKey("favourites");


    @Override
    public List<Place> getSuggestedPlaces(User user, int maxHowMany) {
        //returns a list of Places to check out, based on the ones visited by followed users

        Neo4jConnection neo4jConnection = Neo4jConnection.getObj();

        try (Session session = neo4jConnection.getDriver().session()) {
            return session.writeTransaction((TransactionWork<List<Place>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "USER_ID", user.getId() );
                params.put("HOW_MANY", maxHowMany);
                String newLine = System.getProperty("line.separator");
                String query = String.join(newLine,
                        "MATCH (u:" + NEO_LABEL_USER + ") WHERE u." + User.NEO_KEY_ID + "=$USER_ID",
                        "MATCH (u)-[rFollows:" + NEO_RELATION_FOLLOWS + "]->(uFollowed:" + NEO_LABEL_USER + ")",
                        "MATCH (uFollowed)-[rVisited:" + NEO_RELATION_VISITED + "]->(pl:" + NEO_LABEL_PLACE + ")",
                        "MATCH (u)-[:" + NEO_RELATION_VISITED + "]->(excludedPl:Place)",
                        "MATCH (u)-[:" + NEO_RELATION_FAVOURITES + "]->(exclPl2:Place)",
                        "WITH",
                        "   pl,",
                        "   collect(excludedPl) AS excludedPlaces,",
                        "   collect(exclPl2) AS excludedPlaces2",
                        "WHERE",
                        "   NOT pl IN excludedPlaces",
                        "AND",
                        "NOT pl IN excludedPlaces2",
                        "RETURN pl",
                        "LIMIT $HOW_MANY");
                Result res = tx.run( query, params);
                List<Place> places = new ArrayList<>();
                while(res.hasNext()){
                    Record r = res.next();
                    Value v = r.get("pl");
                    Place p = new Place(v);
                    places.add(p);
                }
                return places;
            });
        } catch (Neo4jException ne){
            System.out.println(ne.getMessage());
            return null;
        }
    }

}
