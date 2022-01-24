package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class PostManagerMongoDB implements PostManager {

    private static final Logger LOGGER = Logger.getLogger(PostManagerMongoDB.class.getName());
    private static final int DEFAULT_MAXIMUM_QUANTITY = 50;

    private MongoCollection postCollection;

    public PostManagerMongoDB(){
        this.postCollection = MongoConnection.getCollection(MongoConnection.Collections.POSTS.toString());
    }
    @Override
    public Post storePost(Post newPost){
        if(newPost == null)
            return null;
        Post res = storeInPostCollection(newPost);
        if(res != null)
            storeEmbeddedPosts(res);
        return res;
    }

    private Post storeInPostCollection(Post newPost) {
        Document postDoc = newPost.createDocument();
        LOGGER.info("Going to store the following post document: " + newPost.toString());
        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId(Post.KEY_ID).toString();
            postDoc.append(Post.KEY_ID, id);
            return new Post(postDoc);
        } catch(MongoException me){
            return null;
        }
    }

    private PostPreview storeEmbeddedPosts(Post newPost) {

        ObjectId placeObjId;
        ObjectId authorObjId;
        try{
            placeObjId = new ObjectId(newPost.getPlaceId());
            authorObjId = new ObjectId(newPost.getAuthorId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        PostPreview newPostPreview = new PostPreview(newPost);
        Document postPreviewDoc = newPostPreview.createDocument();

        try{
            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);
            Bson userUpdate = Updates.push(User.KEY_PUBLISHED_POSTS, postPreviewDoc);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateOne(userFilter, userUpdate);

            Bson placeFilter = Filters.eq(Place.KEY_ID, placeObjId);
            Bson placeUpdate = Updates.push(Place.KEY_POSTS_ARRAY, postPreviewDoc);
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateOne(placeFilter, placeUpdate);

            return newPostPreview;
        } catch(MongoException me){
            LOGGER.severe("Mongo store embedded documents error");
            me.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deletePost(Post postToDelete){
        if(postToDelete == null)
            return  false;
        boolean deletedEmbeddedDocs = false;
        boolean deletedFromPostCollection = deletePostInPostCollection(postToDelete);
        if(deletedFromPostCollection)
            deletedEmbeddedDocs = deleteEmbeddedPosts(postToDelete);
        return deletedEmbeddedDocs && deletedFromPostCollection;
    }

    private boolean deletePostInPostCollection(Post postToDelete){
        if(postToDelete==null)
            return false;

        ObjectId objId;
        String postId = postToDelete.getId();

        try{
            objId = new ObjectId(postId);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        Bson idFilter = Filters.eq(Post.KEY_ID, objId);
        DeleteResult ret = postCollection.deleteOne(idFilter);
        return ret.wasAcknowledged();
    }

    public boolean deleteEmbeddedPosts(Post postToDelete) {
        ObjectId postObjId;
        ObjectId placeObjId;
        ObjectId authorObjId;
        try{
            postObjId = new ObjectId(postToDelete.getId());
            placeObjId = new ObjectId(postToDelete.getPlaceId());
            authorObjId = new ObjectId(postToDelete.getAuthorId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        PostPreview postPreviewToDelete = new PostPreview(postToDelete);
        Document postPreviewToDeleteDoc = postPreviewToDelete.createDocument();

        try{
            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);
            Bson userUpdate = Updates.pull(User.KEY_PUBLISHED_POSTS, postPreviewToDeleteDoc);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateOne(userFilter, userUpdate);

            Bson placeFilter = Filters.eq(Place.KEY_ID, placeObjId);
            Bson placeUpdate = Updates.pull(Place.KEY_POSTS_ARRAY, postPreviewToDeleteDoc);
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateOne(placeFilter, placeUpdate);
            return true;
        } catch(MongoException me){
            return false;
        }
    }

    @Override
    public boolean deletePostsOfGivenUser(User user) {
        if (user == null)
            return false;
        boolean deletedFromPostCollection = false;
        boolean deletedEmbeddedDocs = deleteEmbeddedPostsOfGivenUser(user);
        if(deletedEmbeddedDocs) {
            deletedFromPostCollection = deletePostsOfGivenUserInPostCollection(user);
            if(deletedFromPostCollection == false){
                LOGGER.severe("[!] deletedFromPostCollection is False, the posts of the user '" + user.getId() + "' were not eliminated from post collection");
            }
        }else{
            LOGGER.severe("[!] deletedEmbeddedDocs is False, the posts of the user '" + user.getId() + "' were not eliminated!");
            return false;
        }
        return (deletedEmbeddedDocs && deletedFromPostCollection);
    }
    private boolean deletePostsOfGivenUserInPostCollection(User user) {
        if(user==null) {
            LOGGER.warning("'deletePostsOfGivenUserInPostCollection': received an empty user obj");
            return false;
        }

        Bson authorIdFilter = Filters.eq(Post.KEY_AUTHOR_ID, user.getId());
        DeleteResult ret = postCollection.deleteMany(authorIdFilter);
        LOGGER.info("Deleted " + ret.getDeletedCount() + " documents from Post collection for the user " + user.getId() );
        return ret.wasAcknowledged();
    }

    private boolean deleteEmbeddedPostsOfGivenUser(User user) {
        ObjectId authorObjId;
        try{
            authorObjId = new ObjectId(user.getId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        List<Post> usersPosts = retrieveAllPostsFromUser(user);

        if(usersPosts.isEmpty()){
            LOGGER.info("The specified user '" + user.getId() + "' has no published posts. Skipping...");
            return true;
        }

        //we obtain the ids of the places in which the given user has done a post
        List<ObjectId> placesIds = new ArrayList<>();
        for(Post post : usersPosts){
            if( ! ObjectId.isValid(post.getId())){
                LOGGER.warning("deleteEmbeddedPostsOfGivenUser: the place id '" + post.getPlaceId() + "' of the post '" + post.getId() + "' of the given user " + user.getId() + " is not a valid ObjectId");
                return false;
            }
            ObjectId placeId = new ObjectId(post.getPlaceId());
            //avoid duplicates
            if( ! placesIds.contains(placeId)) {
                placesIds.add(placeId);
            }
        }

        try{
            Bson placesIdsFilter = Filters.in(Place.KEY_ID, placesIds);
            Bson placeUpdater = Updates.pull(Place.KEY_POSTS_ARRAY, new Document(PostPreview.KEY_AUTHOR_USERNAME, user.getUsername()));
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateMany(placesIdsFilter, placeUpdater);

            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);
            //Bson userUpdate = Updates.pull(User.KEY_PUBLISHED_POSTS, new Document(Post.KEY_AUTHOR_ID, authorObjId));
            // equivalent to dropping the attribute...
            Bson userUpdate = Updates.unset(User.KEY_PUBLISHED_POSTS);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateMany(userFilter, userUpdate);

            return true;
        } catch(MongoException me){
            LOGGER.severe("'deleteEmbeddedPostsOfGivenUser': MongoException catched!");
            me.printStackTrace();
            return false;
        }
    }


    @Override
    public List<Post> retrieveAllPostsFromUser(User user) {
        if(user == null){
            LOGGER.warning("'retrieveAllPostsFromUser': received empty user object");
            return null;
        }
        List<Post> postsOfGivenUser = new ArrayList<>();
        Bson authorFilter = Filters.eq(Post.KEY_AUTHOR_ID, user.getId());
        MongoCursor<Document> cur = postCollection.find(authorFilter).cursor();
        while(cur.hasNext()){
            Post post = new Post(cur.next());
            postsOfGivenUser.add(post);
        }
        return postsOfGivenUser;
    }

    @Override
    public Post getPostFromId(String id) {
        if(id.equals(""))
            return null;

        ObjectId objId;

        try{
            objId = new ObjectId(id);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        Bson idFilter = Filters.eq(Post.KEY_ID, objId);
        MongoCursor<Document> cursor = postCollection.find(idFilter).cursor();
        if(!cursor.hasNext()){
            cursor.close();
            return null;
        }
        else{
            Document postDoc = cursor.next();
            Post ret = new Post(postDoc);
            cursor.close();
            return ret;
        }
    }


    @Override
    public boolean updateLikesCounter(Post post, int k) {
        if(post == null || k < -1 || k > 1  || k == 0){
            return false;
        }
        String postId = post.getId();

        Bson idFilter = Filters.eq(Post.KEY_ID, new ObjectId(postId));
        UpdateResult res = postCollection.updateOne(idFilter, Updates.inc("likes", k));
        return res.wasAcknowledged();
    }

    /**
     * It uses the $in Mongo Operator. The results will have the specified activity in the fits array
     * @param activityName: the name of the activity that should be in the fits of the returned Places or "any"
     *                     NOTE that the activityName should be validated in advance
     * @return a Bson object representing the activity filter, null if the filter is not applicable
     */
    private Bson ActivityFilter(String activityName){
        if(activityName == null || activityName.equals(PostService.noActivityFilterKey)){
            return new BasicDBObject();
        }
        BasicDBObject filter=new BasicDBObject();
        filter.put(Post.KEY_ACTIVITY, activityName);
        return filter;
    }

    private Bson FromDateToDateFilter(Date fromDate, Date toDate){
        BasicDBObject filter = new BasicDBObject();
        filter.put("postDate", BasicDBObjectBuilder.start("$gte", fromDate).add("$lte", toDate).get());
        return filter;
    }

    /**
     * use this method to query the Post Collection
     * @param activityAndTimePeriodFilters : a Bson object representing the union of the 2 filters by activity and by startDate-endDate
     * @param sort : a Bson object representing the order by criteria to be used for the result set
     * @param maxQuantity : the maximum quantity of Posts to be returned
     * @return null if empty set, else a List of Posts respecting the given filters
     */
    private List<Post> queryPostCollection(Bson activityAndTimePeriodFilters, Bson sort, int maxQuantity){
        if(maxQuantity <= 0){
            maxQuantity = DEFAULT_MAXIMUM_QUANTITY;
        }
        List<Post> posts = new ArrayList<>();
        FindIterable<Document> iterable = postCollection.find(activityAndTimePeriodFilters).sort(sort).limit(maxQuantity);
        iterable.forEach(doc -> posts.add(new Post(doc)));
        return posts;
    }

    @Override
    public List<Post> getPopularPosts(Date fromDate, Date toDate, String activityName, int maxQuantity) {
        Bson activityFilter = ActivityFilter(activityName);
        Bson timePeriodFilter = FromDateToDateFilter(fromDate, toDate);
        return this.queryPostCollection(Filters.and(activityFilter, timePeriodFilter), Sorts.descending(Post.KEY_LIKES), maxQuantity);
    }

    @Override
    public List<Document> getPostsPerYearAndActivity(int maxQuantity) {
        MongoCollection<Document> postColl = MongoConnection.getCollection(MongoConnection.Collections.POSTS.toString());

        Document firstGroup = new Document("$group",
                new Document("_id",
                        new Document("year", new Document("$year","$postDate"))
                                .append("activity", "$activity"))
                        .append("postPublished", new Document("$sum", 1)));

        Document sort = new Document("$sort", new Document("_id.year", -1).append("postPublished", -1));
        //Document project = new Document("$project", new Document("year", "$_id.year").append("activity", "$_id.activity").append("postsPublished", "$postPublished"));
        Document limit = new Document("$limit", maxQuantity);

        List<Document> results = new ArrayList<>();

        try{
            List<Document> pipeline = Arrays.asList(firstGroup, sort, limit); //project
            AggregateIterable<Document> cursor = postColl.aggregate(pipeline);
            for(Document doc : cursor) { results.add(doc); }
        } catch (MongoException ex){
            ex.printStackTrace();
            LOGGER.severe("Error: MongoDB Analytic about aggregated values on posts failed");
        }

        return results;
    }

    @Override
    public List<Post> retrievePlacesFromName(String postTitleSuffix, int howMany) {
        if(postTitleSuffix.equals("") || postTitleSuffix==null)
            return null;

        List<Post> matchingPosts = new ArrayList<>();
        Pattern regex = Pattern.compile(postTitleSuffix, Pattern.CASE_INSENSITIVE);
        Bson suffixFilter = Filters.eq(Post.KEY_TITLE, regex);
        MongoCursor<Document> cursor = postCollection.find(suffixFilter).limit(howMany).cursor();
        if(!cursor.hasNext()){
            cursor.close();
            return null;
        }
        else{
            while(cursor.hasNext()){
                Document postDoc = cursor.next();
                Post post = new Post(postDoc);
                matchingPosts.add(post);
            }
            cursor.close();
            return matchingPosts;
        }
    }
}
