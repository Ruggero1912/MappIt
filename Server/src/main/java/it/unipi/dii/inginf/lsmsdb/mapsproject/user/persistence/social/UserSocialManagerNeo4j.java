package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import static org.neo4j.driver.Values.parameters;

public class UserSocialManagerNeo4j implements UserSocialManager{

    private static final Logger LOGGER = Logger.getLogger(UserSocialManagerNeo4j.class.getName());

    private static final String USER_ID_KEY = "id";
    private static final String USER_USERNAME_KEY = "username";

    private static final String USERLABEL = "User";     // TODO: retrieve the label from the Graph DB, maybe  can use CALL db.labels
    private static final String PLACELABEL = "Place";
    private static final String NEO4J_RELATION_USER_FAVOURITES_PLACE = "FAVOURITES";
    private static final String NEO4J_RELATION_USER_VISITED_PLACE = "VISITED";
    private static final String PLACE_NAME_KEY = "name";
    private static final String PLACE_ID_KEY = "id";

    private static final ArrayList<String> allowedRelationshipKinds = new ArrayList<>(Arrays.asList("favourite", "visited"));

    public UserSocialManagerNeo4j(){
    }

    @Override
    public User storeUser(User newUser) throws DatabaseConstraintViolation, Neo4jException {

        String id = newUser.getId();
        String username = newUser.getUsername();

        // empty object handling
        if (id == null) return null;
        if (username == null) return null;

        Neo4jConnection neo4jConnection = Neo4jConnection.getObj();

        try (Session session = neo4jConnection.getDriver().session()) {
            return session.writeTransaction((TransactionWork<User>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                params.put( "username", username );

                String query = "CREATE (u:"+ USERLABEL +" { "+ USER_ID_KEY +": $id, "+ USER_USERNAME_KEY +": $username})";

                tx.run( query, params);
                return newUser;
            });
        } catch (Neo4jException ne){
            System.out.println(ne.getMessage());
            if (ne.code().equals("Neo.ClientError.Schema.ConstraintValidationFailed")){
                throw new DatabaseConstraintViolation("A User node with passed id already exists");
            } else{
                throw ne;
            }
        }
    }

    @Override
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    public List<Place> retrieveFavouritePlaces(User user) {
        String id = user.getId();
        List<Place> favouritePlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            favouritePlaces = session.readTransaction((TransactionWork<List<Place>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                String query = "MATCH (u:"+ USERLABEL +")-[r:"+ NEO4J_RELATION_USER_FAVOURITES_PLACE +"]->(pl:"+ PLACELABEL +") WHERE u." + USER_ID_KEY +"= $id RETURN pl as FavouritePlace";
                Result result = tx.run(query,params);
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Place p = new Place(r.get(PLACE_ID_KEY).asString(), r.get(PLACE_NAME_KEY).asString());
                    places.add(p);
                }
                return places;
            });

            return favouritePlaces;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    public List<Place> retrieveVisitedPlaces(User user) {
        String id = user.getId();
        List<Place> visitedPlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            visitedPlaces = session.readTransaction((TransactionWork<List<Place>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                String query = "MATCH (u:"+ USERLABEL +")-[r:"+ NEO4J_RELATION_USER_VISITED_PLACE +"]->(pl:"+ PLACELABEL +") WHERE u." + USER_ID_KEY +"= $id RETURN pl as VisitedPlace";
                Result result = tx.run(query,params);
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value place=r.get("VisitedPlace");
                    Place p = new Place(place.get(PLACE_ID_KEY).asString(), place.get(PLACE_NAME_KEY).asString());
                    places.add(p);
                }
                return places;
            });

            return visitedPlaces;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
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
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')");
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
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')");
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
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')");
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
                tx.run( "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')");
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
