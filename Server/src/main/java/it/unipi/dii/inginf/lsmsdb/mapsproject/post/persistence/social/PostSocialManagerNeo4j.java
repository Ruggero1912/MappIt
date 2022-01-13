package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.neo4j.driver.Session;

import java.util.HashMap;
import java.util.Map;

public class PostSocialManagerNeo4j implements PostSocialManager{

    private static final int POST_DESCRIPTION_MAX_LENGTH = 75;

    @Override
    public boolean storePost(Post newPost) {
        if(newPost==null)
            return false;

        String postId = newPost.getId();
        String entireDescription=newPost.getDescription();
        String truncatedDescription = (entireDescription.length() > POST_DESCRIPTION_MAX_LENGTH) ? entireDescription.substring(0,POST_DESCRIPTION_MAX_LENGTH) : entireDescription;
        String title = newPost.getTitle();
        String thumbnail = newPost.getThumbnail();

        String authorId = newPost.getAuthorId();
        String placeId = newPost.getPlaceId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            return session.writeTransaction(tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "authorId", authorId );
                params.put( "placeId", placeId );
                params.put( "postId", postId );
                params.put( "title", title );
                params.put( "desc", truncatedDescription );
                params.put( "thumb", thumbnail );

                String query="MATCH (u:"+User.NEO_USER_LABEL+" {"+Post.NEO_KEY_ID+": $authorId}) "+
                            "MATCH (pl:"+ Place.NEO_PLACE_LABEL+" {"+Post.NEO_KEY_ID+": $placeId}) "+
                            "CREATE (p:"+Post.NEO_POST_LABEL+" {"+Post.NEO_KEY_ID+": $postId, "+Post.NEO_KEY_TITLE+": $title, "+
                            Post.NEO_KEY_THUMBNAIL+ ": $thumb, "+Post.NEO_KEY_DESC+": $desc})-[r:"+Post.NEO_RELATION_AUTHOR+"]->(u) "+
                            "MERGE (p)-[r2:"+Post.NEO_RELATION_LOCATION+"]->(pl)";

                tx.run(query, params);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean deleteAllPostsOfGivenUser(User u){
        if(u==null)
            return false;

        String userId = u.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            return session.writeTransaction(tx -> {
                String query = "MATCH (p:"+Post.NEO_POST_LABEL+")<-[r:AUTHOR]-(u:"+User.NEO_USER_LABEL+" WHERE u."+Post.NEO_KEY_ID+"='"+userId+"') " +
                                "DETACH DELETE p";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean likePost(User u, Post p) {
        String userId = u.getId();
        String postId = p.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+Post.NEO_KEY_ID+" = '"+userId+"') " +
                        "MATCH (p:"+Post.NEO_POST_LABEL+" WHERE p.id = '"+postId+"') " +
                        "MERGE (u)-[r:"+User.NEO_RELATION_LIKES+"]->(p) " +
                        " SET r.datetime = datetime()";

                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean unlikePost(User u, Post p) {
        String userId = u.getId();
        String postId = p.getId();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //if relationship doesn't exist the DELETE method leaves the node(s) unaffected.
            return session.writeTransaction(tx -> {
                String query = "MATCH (u:"+User.NEO_USER_LABEL+" WHERE u."+Post.NEO_KEY_ID+" = '"+userId+"') " +
                                "MATCH (p:"+Post.NEO_POST_LABEL+" WHERE p.id = '"+postId+"') " +
                                "MATCH (u)-[r:"+User.NEO_RELATION_LIKES+"]->(p) DELETE r";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }
}