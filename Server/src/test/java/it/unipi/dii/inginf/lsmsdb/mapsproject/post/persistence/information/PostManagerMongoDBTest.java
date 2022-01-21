package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class PostManagerMongoDBTest {
    @DisplayName("Test PostService.deletePost standard behaviour")
    @Test
    void GIVEN_postId_WHEN_standard_parameters_are_passed_THEN_deletes_post(){

        String json = "{\n" +
                "  \"authorUsername\": \"Micky Techology\",\n" +
                "  \"authorId\": \"61e567f53169df0c39dc8ac9\",\n" +
                "  \"placeId\": \"61d33ff1a1dc9d89ac02f817\",\n" +
                "  \"title\": \"post di prova\",\n" +
                "  \"date\": \"2022-01-19T19:01:28.006+00:00\",\n" +
                "  \"description\": \"descedescedsdedsc\",\n" +
                "  \"activity\": \"string\",\n" +
                "  \"tags\": [\n" +
                "    \"fenom\"\n" +
                "  ],\n" +
                "  \"videoId\": null,\n" +
                "  \"thumbnail\": \"\",\n" +
                "  \"pics\": [],\n" +
                "  \"_id\": \"61e86193ebbc440d6cecf9b4\"\n" +
                "}";

        BsonDocument bsonDocument = BsonDocument.parse(json);
        DocumentCodec codec = new DocumentCodec();
        DecoderContext decoderContext = DecoderContext.builder().build();
        Document document = codec.decode(new BsonDocumentReader(bsonDocument), decoderContext);
        Post post = Post.buildPost(document);

        System.out.println("Builded post obj: " + post.toString());

        try {
            //PostService.deletePost(post);
            PostManagerMongoDB pm = new PostManagerMongoDB();
            boolean ret = pm.deleteEmbeddedPosts(post);
            System.out.println("outcome " + ret);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @DisplayName("Test PostManagerMongoDB.getPostsPerYearAndActivity standard behaviour")
    @Test
    void GIVEN_getPostsPerYearAndActivity_WHEN_standard_parameters_are_passed_THEN_print_correct_values(){

        try {
            PostManagerMongoDB pm = new PostManagerMongoDB();
            pm.getPostsPerYearAndActivity(3);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
