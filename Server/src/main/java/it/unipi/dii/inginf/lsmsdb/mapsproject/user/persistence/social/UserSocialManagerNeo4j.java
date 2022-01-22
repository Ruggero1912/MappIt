package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlacePreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserSocialManagerNeo4j implements UserSocialManager{

    private static final Logger LOGGER = Logger.getLogger(UserSocialManagerNeo4j.class.getName());

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
            LOGGER.log(Level.SEVERE, "Neo4j Error during deleting user from id: "+e.getMessage());
            return false;
        }
    }

    @Override
    public List<PlacePreview> retrieveFavouritePlaces(User user, int howMany) {
        String id = user.getId();
        List<PlacePreview> favouritePlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            favouritePlaces = session.readTransaction((TransactionWork<List<PlacePreview>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                params.put( "howMany", howMany );
                String query = "MATCH (u:"+ User.NEO_USER_LABEL +")-[r:"+ User.NEO_RELATION_FAVOURITES +"]->(pl:"+ Place.NEO_PLACE_LABEL +") WHERE u." + User.NEO_KEY_ID +"= $id RETURN pl as FavouritePlace LIMIT $howMany";
                Result result = tx.run(query,params);
                ArrayList<PlacePreview> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value place=r.get("FavouritePlace");
                    PlacePreview p = new PlacePreview(place);
                    places.add(p);
                }
                return places;
            });

            return favouritePlaces;
        }catch (Exception e){
            LOGGER.warning("Neo4j Error occurred during retrieveFavouritePlaces() method run");
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public List<PlacePreview> retrieveVisitedPlaces(User user, int howMany) {
        String id = user.getId();
        List<PlacePreview> visitedPlaces;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            visitedPlaces = session.readTransaction((TransactionWork<List<PlacePreview>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", id );
                params.put( "howMany", howMany );
                String query = "MATCH (u:"+ User.NEO_USER_LABEL +")-[r:"+ User.NEO_RELATION_VISITED +"]->(pl:"+ Place.NEO_PLACE_LABEL +") WHERE u." + User.NEO_KEY_ID +"= $id RETURN pl as VisitedPlace LIMIT $howMany";
                Result result = tx.run(query,params);
                ArrayList<PlacePreview> places = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value place=r.get("VisitedPlace");
                    PlacePreview p = new PlacePreview(place);
                    places.add(p);
                }
                return places;
            });

            return visitedPlaces;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve visited places of a user: "+e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Neo4j Error during storing favourite place of a user: "+e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Neo4j Error during deleting favourite place of a user: "+e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Neo4j Error during storing visited place of a user: "+e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Neo4j Error during adding follower of a user: "+e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Neo4j Error during deleting follower of a user: "+e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getSuggestedFollowersIds(User user, int maxHowMany) {
        Neo4jConnection neo4jConnection = Neo4jConnection.getObj();

        try (Session session = neo4jConnection.getDriver().session()) {
            return session.writeTransaction((TransactionWork<List<String>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "USER_ID", user.getId() );
                params.put("HOW_MANY", maxHowMany);
                String newLine = System.getProperty("line.separator");
                String query = String.join(newLine,
                "MATCH (u:"+ User.NEO_USER_LABEL +"{"+User.NEO_KEY_ID+":$USER_ID})-[:"+ User.NEO_RELATION_FOLLOWS +"]->(f:"+User.NEO_USER_LABEL+
                        ")-[:"+ User.NEO_RELATION_FOLLOWS +"]->(s:"+User.NEO_USER_LABEL+") ",
                "WHERE u."+User.NEO_KEY_ID+"<>s."+User.NEO_KEY_ID+" AND NOT((u)-[:"+ User.NEO_RELATION_FOLLOWS +"]->(s)) ",
                "WITH DISTINCT s AS suggestedUsers ",
                "MATCH (suggestedUsers)<-[r:"+ User.NEO_RELATION_FOLLOWS +"]-(:"+ User.NEO_USER_LABEL +") ",
                "WITH suggestedUsers.id AS id, suggestedUsers."+User.NEO_KEY_ID+" AS userId, COUNT(DISTINCT r) AS followers ",
                "ORDER BY followers DESC ",
                "LIMIT $HOW_MANY ",
                "RETURN id, userId, followers");

                Result res = tx.run(query, params);
                List<String> suggestedFollowersIds = new ArrayList<>();
                while(res.hasNext()){
                    Record r = res.next();
                    Value v = r.get("id");
                    String userIdToFollow = v.asString();
                    suggestedFollowersIds.add(userIdToFollow);
                }
                return suggestedFollowersIds;
            });
        } catch (Neo4jException ne){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve suggested followers ids: "+ne.getMessage());
            return null;
        }
    }


    @Override
    public List<PostPreview> getSuggestedPosts(User user, int maxHowMany) {
        Neo4jConnection neo4jConnection = Neo4jConnection.getObj();

        try (Session session = neo4jConnection.getDriver().session()) {
            return session.writeTransaction((TransactionWork<List<PostPreview>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "USER_ID", user.getId() );
                params.put("HOW_MANY", maxHowMany);
                String newLine = System.getProperty("line.separator");
                String query = String.join(newLine,
        "MATCH (u:"+ User.NEO_USER_LABEL +"{"+User.NEO_KEY_ID+":$USER_ID})-[:"+ User.NEO_RELATION_LIKES +"]->(p:"+ Post.NEO_POST_LABEL +")-[:"
                + Post.NEO_RELATION_LOCATION +"]->(pl:"+ Place.NEO_PLACE_LABEL +") ",
                "WITH DISTINCT pl AS places, COLLECT(p) AS likedPosts ",
                "MATCH (:"+ User.NEO_USER_LABEL +")-[l:"+ User.NEO_RELATION_LIKES +"]->(sp:"+ Post.NEO_POST_LABEL +" WHERE NOT(sp IN likedPosts))-[:"+ Post.NEO_RELATION_LOCATION +"]->(places) ",
                "MATCH (author:"+ User.NEO_USER_LABEL +")-[:"+ Post.NEO_RELATION_AUTHOR +"]->(sp) ",
                "WITH DISTINCT sp AS suggestedPosts, COUNT(l) AS likeReceived, author.username AS authorUsername, author.id AS authorId ",
                "ORDER BY likeReceived DESC ",
                "LIMIT $HOW_MANY ",
                "RETURN suggestedPosts, authorUsername, authorId, likeReceived ");

                Result res = tx.run(query, params);
                List<PostPreview> suggestedPosts = new ArrayList<>();
                while(res.hasNext()){
                    Record r = res.next();
                    Value postValue = r.get("suggestedPosts");
                    Value authorUsername = r.get("authorUsername");
                    Value authorId = r.get("authorId");
                    PostPreview postPreview = new PostPreview(postValue, authorId, authorUsername);
                    suggestedPosts.add(postPreview);
                }
                return suggestedPosts;
            });
        } catch (Neo4jException ne){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve suggested posts: "+ne.getMessage());
            return null;
        }
    }

    @Override
    public List<String> retrieveFollowers(String userId, int howMany) {
        List<String> followers;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            followers = session.readTransaction((TransactionWork<List<String>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", userId );
                params.put( "howMany", howMany );
                String query = "MATCH (followers:"+ User.NEO_USER_LABEL +")-[follows:"+ User.NEO_RELATION_FOLLOWS +
                        "]->(targetUser:"+ User.NEO_USER_LABEL +") WHERE targetUser." +
                        User.NEO_KEY_ID +"= $id RETURN followers.id as FollowersIds LIMIT $howMany";
                Result result = tx.run(query,params);
                ArrayList<String> ids = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    ids.add(r.get("FollowersIds").asString());
                }
                return ids;
            });

            return followers;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve followers of a user: "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> retrieveFollowedUsers(String userId, int howMany) {
        List<String> followedUsers;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            followedUsers = session.readTransaction((TransactionWork<List<String>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", userId );
                params.put( "howMany", howMany );
                String query = "MATCH (followedUser:"+ User.NEO_USER_LABEL +")<-[follows:"+ User.NEO_RELATION_FOLLOWS +
                        "]-(targetUser:"+ User.NEO_USER_LABEL +") WHERE targetUser." +
                        User.NEO_KEY_ID +"= $id RETURN followedUser.id as FollowedUsersIds LIMIT $howMany";
                Result result = tx.run(query,params);
                ArrayList<String> ids = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    ids.add(r.get("FollowedUsersIds").asString());
                }
                return ids;
            });

            return followedUsers;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve followed users: "+e.getMessage());
            return null;
        }
    }


    @Override
    public List<PostPreview> retrieveLikedPosts(String userId, int howMany) {
        List<PostPreview> likedPosts;

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            likedPosts = session.readTransaction((TransactionWork<List<PostPreview>>) tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "id", userId );
                params.put( "howMany", howMany );
                String query = "MATCH (targetUser:"+ User.NEO_USER_LABEL +" {"+User.NEO_KEY_ID+": $id})-[likes:"+ User.NEO_RELATION_LIKES
                        +"]->(post:"+ Post.NEO_POST_LABEL +")<-[a:"+Post.NEO_RELATION_AUTHOR+
                        "]-(author:"+User.NEO_USER_LABEL+") RETURN post AS likedPost, author.username AS authorUsername, author.id AS authorId LIMIT $howMany";
                Result result = tx.run(query,params);
                ArrayList<PostPreview> posts = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    Value postValue = r.get("likedPost");
                    Value authorId = r.get("authorId");
                    Value authorUsername = r.get("authorUsername");
                    PostPreview postPreview = new PostPreview(postValue, authorId, authorUsername);
                    posts.add(postPreview);
                }
                return posts;
            });

            return likedPosts;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Neo4j Error during retrieve liked posts of the specified user: "+e.getMessage());
            return null;
        }
    }
}
