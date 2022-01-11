package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import org.neo4j.driver.Session;

import java.util.HashMap;
import java.util.Map;

public class PostSocialManagerNeo4j implements PostSocialManager{

    private static final int POST_DESCRIPTION_MAX_LENGTH = 75;

    public static final String POST_LABEL_NEO = PropertyPicker.getNodeLabel(PropertyPicker.postEntity);
    public static final String USER_LABEL_NEO = PropertyPicker.getNodeLabel(PropertyPicker.userEntity);

    public static final String KEY_ID_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "id");
    public static final String KEY_TITLE_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "title");
    public static final String KEY_DESCRIPTION_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "desc");
    public static final String KEY_THUMBNAIL_NEO = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "thumb");

    public static final String KEY_RELATIONSHIP_AUTHOR = PropertyPicker.getGraphRelationKey(PropertyPicker.authorRelationship);


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

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            return session.writeTransaction(tx -> {
                Map<String,Object> params = new HashMap<>();
                params.put( "authorId", authorId );
                params.put( "postId", postId );
                params.put( "title", title );
                params.put( "desc", truncatedDescription );
                params.put( "thumb", thumbnail );

                String query="MATCH (u:"+USER_LABEL_NEO+" {"+KEY_ID_NEO+": $authorId}) "+
                            "CREATE (p:"+POST_LABEL_NEO+" {"+KEY_ID_NEO+": $postId, "+KEY_TITLE_NEO+": $title, "+
                            KEY_THUMBNAIL_NEO+ ": $thumb, "+KEY_DESCRIPTION_NEO+": $desc})-[r:"+KEY_RELATIONSHIP_AUTHOR+"]->(u)";

                tx.run(query, params);
                return true;
            });
        }catch (Exception e){
            return false;
        }
    }
}
