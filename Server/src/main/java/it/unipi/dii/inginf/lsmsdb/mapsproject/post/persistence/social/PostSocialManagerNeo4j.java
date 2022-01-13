package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.neo4j.driver.Session;

import java.util.HashMap;
import java.util.Map;

public class PostSocialManagerNeo4j implements PostSocialManager{

    private static final int POST_DESCRIPTION_MAX_LENGTH = 75;

    public static final String POST_LABEL_NEO = PropertyPicker.getNodeLabel(PropertyPicker.postEntity);
    public static final String USER_LABEL_NEO = PropertyPicker.getNodeLabel(PropertyPicker.userEntity);
    public static final String PLACE_LABEL_NEO = PropertyPicker.getNodeLabel(PropertyPicker.placeEntity);

    public static final String KEY_ID_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "id");
    public static final String KEY_TITLE_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "title");
    public static final String KEY_DESCRIPTION_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "desc");
    public static final String KEY_THUMBNAIL_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "thumb");

    public static final String KEY_RELATIONSHIP_AUTHOR = PropertyPicker.getGraphRelationKey(PropertyPicker.authorRelationship);
    public static final String KEY_RELATIONSHIP_LOCATION = PropertyPicker.getGraphRelationKey(PropertyPicker.locationRelationship);
    public static final String KEY_RELATIONSHIP_LIKES = PropertyPicker.getGraphRelationKey(PropertyPicker.likesRelationship);


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

                String query="MATCH (u:"+USER_LABEL_NEO+" {"+KEY_ID_NEO+": $authorId}) "+
                            "MATCH (pl:"+PLACE_LABEL_NEO+" {"+KEY_ID_NEO+": $placeId}) "+
                            "CREATE (p:"+POST_LABEL_NEO+" {"+KEY_ID_NEO+": $postId, "+KEY_TITLE_NEO+": $title, "+
                            KEY_THUMBNAIL_NEO+ ": $thumb, "+KEY_DESCRIPTION_NEO+": $desc})-[r:"+KEY_RELATIONSHIP_AUTHOR+"]->(u) "+
                            "MERGE (p)-[r2:"+KEY_RELATIONSHIP_LOCATION+"]->(pl)";

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
                String query = "MATCH (p:"+POST_LABEL_NEO+")-[r:AUTHOR]->(u:"+USER_LABEL_NEO+" WHERE u."+KEY_ID_NEO+"="+userId+") " +
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
                String query = "MATCH (u:"+USER_LABEL_NEO+" WHERE u."+KEY_ID_NEO+" = '"+userId+"') " +
                        "MATCH (p:"+POST_LABEL_NEO+" WHERE p.id = '"+postId+"') " +
                        "MERGE (u)-[r:"+KEY_RELATIONSHIP_LIKES+"]->(p) " +
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
                String query = "MATCH (u:"+USER_LABEL_NEO+" WHERE u."+KEY_ID_NEO+" = '"+userId+"') " +
                                "MATCH (p:"+POST_LABEL_NEO+" WHERE p.id = '"+postId+"') " +
                                "MATCH (u)-[r:"+KEY_RELATIONSHIP_LIKES+"]->(p) DELETE r";
                tx.run(query);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }
}