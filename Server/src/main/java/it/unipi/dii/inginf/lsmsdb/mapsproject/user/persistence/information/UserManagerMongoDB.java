package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserManagerMongoDB implements UserManager{

    private static final Logger LOGGER = Logger.getLogger(UserManagerMongoDB.class.getName());

    private static final UserManagerMongoDB obj = new UserManagerMongoDB(); //TODO

    //private static final String USERCOLLECTIONKEY = "user"; //PropertyPicker.getProperty(PropertyPicker.userCollectionName);
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
            return ret;
        }
        else
            return null;
    }

    @Override
    public List<User> getAllUser(){
        List<User> users = new ArrayList<>();
        FindIterable<Document> iterable = userCollection.find();
        iterable.forEach(doc -> users.add(new User(doc)));
        return users;
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
            String id = userDoc.getObjectId(IDKEY).toString();
            userDoc.append(IDKEY, id);
            return new User(userDoc);
        } catch(Exception e){
            return null;
        }
    }

    @Override
    public User getUserFromId(String id){
        if(id.equals(""))
            return null;

        ObjectId objId;

        try{
           objId = new ObjectId(id);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        Bson idFilter = Filters.eq(IDKEY, objId);
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

    @Override
    public boolean deleteUserFromId(String id){
        if(id.equals(""))
            return false;

        ObjectId objId;

        try{
            objId = new ObjectId(id);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        Bson idFilter = Filters.eq(IDKEY, objId);
        DeleteResult ret = userCollection.deleteOne(idFilter);
        return ret.wasAcknowledged();
    }

    @Override
    public boolean changePassword(String id, String newPassword){
        String newEncryptedPassword = UserService.passwordEncryption(newPassword);
        Bson idFilter = Filters.eq(IDKEY, new ObjectId(id));
        UpdateResult res = userCollection.updateOne(idFilter, set("password", newEncryptedPassword));
        return res.wasAcknowledged();
    }
}
