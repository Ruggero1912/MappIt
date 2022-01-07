package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.neo4j.driver.Values.parameters;

public class UserSocialManagerNeo4j implements UserSocialManager{

    private static final Logger LOGGER = Logger.getLogger(UserSocialManagerNeo4j.class.getName());

    private static final String IDKEY = "id";
    private static final String USERNAMEKEY = "username";

    private static final String USERLABEL = "User";     // TODO: retrieve the label from the Graph DB, maybe with CALL db.labels
    private static final String PLACELABEL = "Place";
    private static final String NEO4J_RELATION_USER_FAVOURITES_PLACE = "FAVOURITES";
    private static final String NEO4J_RELATION_USER_VISITED_PLACE = "VISITED";
    private static final String PLACE_NAME_KEY = "name";
    private static final String PLACE_ID_KEY = "id";

    private static final ArrayList<String> allowedRelationshipKinds = new ArrayList<>(Arrays.asList("favourite", "visited"));

    public UserSocialManagerNeo4j(){
    }

    @Override
    public User storeUser(User newUser) {

        String id = newUser.getId();
        String username = newUser.getUsername();

        Neo4jConnection neo4jConnection = Neo4jConnection.getObj();

        try (Session session = neo4jConnection.getDriver().session()) {
            session.writeTransaction((TransactionWork<User>) tx -> {

                tx.run("CREATE (u:User { id: '61d3428101336eeafcb438e7', username: 'abc' })");
                //tx.run("CREATE (u:"+ USERLABEL +" { "+ IDKEY +": '$id', "+ USERNAMEKEY +": '$username' })", parameters(IDKEY, id, USERNAMEKEY, username));
                return newUser;
            });
        } catch (Neo4jException ne){
            return null;
        }
        return null;
    }

    @Override
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    public List<Place> retrieveFavouritePlaces(User user) {
        String id = user.getId();
        List<Place> favouritePlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            favouritePlaces = session.readTransaction((TransactionWork<List<Place>>) tx -> {
                Result result = tx.run( "MATCH (u:"+ USERLABEL +")-[r:"+ NEO4J_RELATION_USER_FAVOURITES_PLACE +"]->(pl:"+ PLACELABEL +") WHERE u." + IDKEY +": $id RETURN pl",
                        parameters(IDKEY, id) );
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Place p = new Place(r.get(PLACE_ID_KEY).asString(), r.get(PLACE_NAME_KEY).asString());
                    places.add(p);
                }
                return places;
            });
        }catch (Exception e){
            favouritePlaces = null;
        }

        return favouritePlaces;
    }

    @Override
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    public List<Place> retrieveVisitedPlaces(User user) {
        String id = user.getId();
        List<Place> visitedPlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            visitedPlaces = session.readTransaction((TransactionWork<List<Place>>) tx -> {
                Result result = tx.run( "MATCH (u:"+ USERLABEL +")-[r:"+ NEO4J_RELATION_USER_VISITED_PLACE +"]->(pl:"+ PLACELABEL +") WHERE u." + IDKEY +": $id RETURN pl",
                        parameters(IDKEY, id) );
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Place p = new Place(r.get(PLACE_ID_KEY).asString(), r.get(PLACE_NAME_KEY).asString());
                    places.add(p);
                }
                return places;
            });
        }catch (Exception e){
            visitedPlaces = null;
        }

        return visitedPlaces;
    }

    @Override
    public boolean storeNewFavouritePlace(User user, Place place) {
        String userId = user.getId();
        String placeId = place.getId();
        LocalDateTime now = LocalDateTime.now();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Merge method will handle the duplicate relationship case
            session.writeTransaction(tx -> {
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+IDKEY+" = '"+userId+"')");
                tx.run( "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"')");
                tx.run( "MERGE (u)-[:"+NEO4J_RELATION_USER_FAVOURITES_PLACE+" {datetime: "+now+"}]->(p)");
                return true;
            });
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteFavouritePlace(User user, Place place) {
        String userId = user.getId();
        String placeId = place.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //if relationship doesn't exist the DELETE method leaves the node(s) unaffected.
            session.writeTransaction(tx -> {
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+IDKEY+" = '"+userId+"')");
                tx.run( "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"')");
                tx.run( "MATCH (u)-[r:"+NEO4J_RELATION_USER_FAVOURITES_PLACE+"]->(p) DELETE r");
                return true;
            });
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public boolean storeNewVisitedPlace(User user, Place place, LocalDateTime timestampVisit) {
        String userId = user.getId();
        String placeId = place.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Merge method will handle the duplicate relationship case
            session.writeTransaction(tx -> {
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+IDKEY+" = '"+userId+"')");
                tx.run( "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"')");
                tx.run( "MERGE (u)-[:"+NEO4J_RELATION_USER_VISITED_PLACE+" {datetime: "+timestampVisit+"}]->(p)");
                return true;
            });
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public boolean checkAlreadyExistingRelationship(User user, Place place, String relationshipKind) {
        String userId = user.getId();
        String placeId = place.getId();
        if(!allowedRelationshipKinds.contains(relationshipKind)){
            return false;
        }

        try ( Session session = Neo4jConnection.getDriver().session() ) {
            session.readTransaction((TransactionWork<Boolean>) tx -> {
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+IDKEY+" = '"+userId+"')");
                tx.run( "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"')");

                String query="";
                if(relationshipKind.equals(allowedRelationshipKinds.get(0))){
                    query = "MATCH (u)-[:"+NEO4J_RELATION_USER_FAVOURITES_PLACE+"]->(p)";
                }
                else if(relationshipKind.equals(allowedRelationshipKinds.get(1))){
                    query = "MATCH (u)-[:"+NEO4J_RELATION_USER_VISITED_PLACE+"]->(p)";
                }

                Result result = tx.run(query);
                if(result.hasNext()){
                    return true;
                }
                else
                    return false;
            });
        }
        return false;
    }
}
