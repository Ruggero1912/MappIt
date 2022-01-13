package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
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

                String query = "CREATE (u:"+ User.NEO_USER_LABEL +" { "+ User.NEO_KEY_ID +": $id, "+ User.NEO_KEY_USERNAME +": $username})";

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
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"') " +
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
                String query = "MATCH (u:"+ User.NEO_USER_LABEL +")-[r:"+ User.NEO_RELATION_FAVOURITES +"]->(pl:"+ Place.NEO_PLACE_LABEL +") WHERE u." + User.NEO_KEY_ID +"= $id RETURN pl as FavouritePlace";
                Result result = tx.run(query,params);
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value place=r.get("FavouritePlace");
                    Place p = new Place(place);
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
                String query = "MATCH (u:"+ User.NEO_USER_LABEL +")-[r:"+ User.NEO_RELATION_VISITED +"]->(pl:"+ Place.NEO_PLACE_LABEL +") WHERE u." + User.NEO_KEY_ID +"= $id RETURN pl as VisitedPlace";
                Result result = tx.run(query,params);
                ArrayList<Place> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value place=r.get("VisitedPlace");
                    Place p = new Place(place);
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
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"') " +
                                "MATCH (p:"+Place.NEO_PLACE_LABEL+" WHERE p.id = '"+placeId+"') " +
                                "MERGE (u)-[r:"+User.NEO_RELATION_FAVOURITES+"]->(p) " +
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
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"') " +
                                "MATCH (p:"+Place.NEO_PLACE_LABEL+" WHERE p.id = '"+placeId+"') " +
                                "MATCH (u)-[r:"+User.NEO_RELATION_FAVOURITES+"]->(p) DELETE r";
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
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"')" +
                                "MATCH (p:"+Place.NEO_PLACE_LABEL+" WHERE p.id = '"+placeId+"')" +
                                "MERGE (u)-[r:"+User.NEO_RELATION_VISITED+" {datetime: $datetime}]->(p)";
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
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"')" +
                                "MATCH (uToFollow:"+User.NEO_USER_LABEL+" WHERE uToFollow."+User.NEO_KEY_ID+" = '"+userToFollowId+"')" +
                                "MERGE (u)-[r:"+User.NEO_RELATION_FOLLOWS+"]->(uToFollow) "+
                                "SET r.datetime = datetime()";
                tx.run(query, params);
                return true;
            });
        } catch ( Exception e){
            return false;
        }
    }

    @Override
    public boolean deleteFollower(User user, User userToUnfollow) {
        String userId = user.getId();
        String userToUnfollowId = userToUnfollow.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+User.NEO_KEY_ID+" = '"+userId+"') " +
                                "MATCH (uToUnfollow:"+User.NEO_USER_LABEL+" WHERE uToUnfollow."+User.NEO_KEY_ID+" = '"+userToUnfollowId+"') " +
                                "MATCH (u)-[r:"+User.NEO_RELATION_FOLLOWS+"]->(uToUnfollow) DELETE r";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public List<Post> retrieveAllPosts(User user) {
        String id = user.getId();
        List<Post> userPosts;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            userPosts = session.readTransaction((TransactionWork<List<Post>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                String query = "MATCH (u:"+ User.NEO_USER_LABEL +")-[r:"+ Post.NEO_RELATION_AUTHOR +"]->(p:"+ Post.NEO_POST_LABEL +") WHERE u." + User.NEO_KEY_ID +"= $id RETURN p as Post";
                Result result = tx.run(query,params);
                ArrayList<Post> posts = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value post=r.get("Post");
                    Post p = new Post(post);
                    posts.add(p);
                }
                return posts;
            });

            return userPosts;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
