package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.DatabaseException;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.neo4j.driver.Values.parameters;

public class UserSocialManagerNeo4j implements UserSocialManager{

    private static final Logger LOGGER = Logger.getLogger(UserSocialManagerNeo4j.class.getName());

    private static final String USER_ID_KEY = "id";
    private static final String USER_USERNAME_KEY = "username";

    private static final String USERLABEL = "User";
    private static final String PLACELABEL = "Place";
    private static final String NEO4J_RELATION_USER_FOLLOWS_USER = "FOLLOWS";
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
    public boolean deleteUserFromId(String userId) {
        if(userId.equals(""))
            return false;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Detach deletes the node and all its relationships
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"') " +
                        "DETACH DELETE u";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
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
                    Value place=r.get("FavouritePlace");
                    Place p = new Place(place.get(PLACE_ID_KEY).asString(), place.get(PLACE_NAME_KEY).asString());
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

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Merge method will handle the duplicate relationship case
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"') " +
                                "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"') " +
                                "MERGE (u)-[r:"+NEO4J_RELATION_USER_FAVOURITES_PLACE+"]->(p) " +
                                " SET r.datetime = datetime()";

                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean deleteFavouritePlace(User user, Place place) {
        String userId = user.getId();
        String placeId = place.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //if relationship doesn't exist the DELETE method leaves the node(s) unaffected.
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"') " +
                                "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"') " +
                                "MATCH (u)-[r:"+NEO4J_RELATION_USER_FAVOURITES_PLACE+"]->(p) DELETE r";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean storeNewVisitedPlace(User user, Place place, LocalDateTime timestampVisit) {
        String userId = user.getId();
        String placeId = place.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Merge method will handle the duplicate relationship case
            return session.writeTransaction(tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "datetime", timestampVisit );
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')" +
                                "MATCH (p:"+PLACELABEL+" WHERE p.id = '"+placeId+"')" +
                                "MERGE (u)-[r:"+NEO4J_RELATION_USER_VISITED_PLACE+" {datetime: $datetime}]->(p)";
                tx.run(query, params);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean storeNewFollower(User user, User userToFollow, LocalDateTime timestampFollow) {
        String userId = user.getId();
        String userToFollowId = userToFollow.getId();

        try( Session session = Neo4jConnection.getDriver().session())
        {
            //Merge method will handle the duplicate relationship case
            return session.writeTransaction(tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "datetime", timestampFollow);
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"')" +
                                "MATCH (uToFollow:"+USERLABEL+" WHERE uToFollow."+USER_ID_KEY+" = '"+userToFollowId+"')" +
                                "MERGE (u)-[r:"+NEO4J_RELATION_USER_FOLLOWS_USER+" {datetime: $datetime}]->(uToFollow)";
                tx.run(query, params);
                return true;
            });
        } catch ( Exception e){
            return false;
        }
    }

    @Override
    public LocalDateTime deleteFollower(User user, User userToUnfollow) {
        String userId = user.getId();
        String userToUnfollowId = userToUnfollow.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //if relationship doesn't exist the DELETE method leaves the node(s) unaffected.
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+USERLABEL+" WHERE u."+USER_ID_KEY+" = '"+userId+"') " +
                                "MATCH (uToUnfollow:"+USERLABEL+" WHERE uToUnfollow."+USER_ID_KEY+" = '"+userToUnfollowId+"') " +
                                "MATCH (u)-[r:"+NEO4J_RELATION_USER_FOLLOWS_USER+"]->(uToUnfollow) "+
                                "WITH r.datetime as timestamp "+
                                "DELETE r "+
                                "RETURN timestamp";
                Result result = tx.run(query);
                LocalDateTime timestamp = null;
                if(result.hasNext())
                {
                    Record r = result.next();
                    Value value = r.get("timestamp");
                    timestamp = value.asLocalDateTime();
                }
                return timestamp;
            });
        }catch (Exception e){
            return null;
        }
    }
}
