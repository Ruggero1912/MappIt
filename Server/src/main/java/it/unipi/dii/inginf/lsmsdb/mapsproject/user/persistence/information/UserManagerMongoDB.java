package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;


public class UserManagerMongoDB implements UserManager{

    private static final UserManagerMongoDB obj = new UserManagerMongoDB(); //TODO

    private static final String USERCOLLECTIONKEY = "user"; //PropertyPicker.getProperty(PropertyPicker.userCollectionName);

    private static final String IDKEY = "_id";
    private static final String USERNAMEKEY = "username";
    private static final String PASSWORDKEY = "password";
    private static final String EMAILKEY = "email";

    private MongoCollection userCollection;

    public UserManagerMongoDB(){
        userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
    }

    @Override
    public User getUserFromUsername(String username){
        Bson usernameFilter = Filters.eq(USERNAMEKEY, username);
        MongoCursor<Document> cursor = userCollection.find(usernameFilter).cursor();

        if(cursor.hasNext()){
            Document userDoc = cursor.next();
            User ret = new User(userDoc);
            //User ret = User.buildUser(userDoc);
            return ret;
        }
        else
            return null;
    }

    @Override
    public boolean checkDuplicateUsername(String username) {
        Bson usernameFilter = Filters.eq(USERNAMEKEY, username);
        MongoCursor<Document> cursor = userCollection.find(usernameFilter).cursor();
        return cursor.hasNext();
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        Bson emailFilter = Filters.eq(EMAILKEY, email);
        MongoCursor<Document> cursor = userCollection.find(emailFilter).cursor();
        return cursor.hasNext();
    }

    @Override
    public User storeUser(RegistrationUser newUser){
        // RegistrationUser.createDocument() also add a default role and default profile pic
        Document userDoc = newUser.createDocument();

        try{
            userCollection.insertOne(userDoc);
            //add inserted object id to the document
            String id = userDoc.getObjectId("_id").toString();
            userDoc.append("_id", id);
            return new User(userDoc);
        } catch(MongoException me){
            return null;
        }
    }

    @Override
    public User getUserFromId(String id){
        Bson idFilter = Filters.eq(IDKEY, new ObjectId(id));
        MongoCursor<Document> cursor = userCollection.find(idFilter).cursor();
        if(!cursor.hasNext()){
            return null;
        }
        else{
            Document userDoc = cursor.next();
            User ret = new User(userDoc);
            return ret;
        }
    }
}
