package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import org.neo4j.driver.Session;

public class PostSocialManagerNeo4j implements PostSocialManager{

    @Override
    public Post storePost(Post newPost) {
        if(newPost==null)
            return null;

        String postId = newPost.getId();
        String authorUsername = newPost.getAuthorUsername();

        try ( Session session = Neo4jConnection.getDriver().session() )
        {
            //Merge method will handle the duplicate relationship case
            return session.writeTransaction(tx -> {
                String query = "";

                tx.run(query);
                return newPost;
            });
        }catch (Exception e){
            return null;
        }
    }
}
