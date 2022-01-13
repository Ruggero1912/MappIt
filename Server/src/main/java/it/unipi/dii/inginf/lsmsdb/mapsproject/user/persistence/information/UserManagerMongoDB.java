package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Updates;
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

    private MongoCollection userCollection;

    public UserManagerMongoDB(){
        userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
    }

    @Override
    public User getUserFromUsername(String username){
        Bson usernameFilter = Filters.eq(User.KEY_USERNAME, username);
        MongoCursor<Document> cursor = userCollection.find(usernameFilter).cursor();

        if(cursor.hasNext()){
            Document userDoc = cursor.next();
            User ret = new User(userDoc);
            cursor.close();
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
        Bson usernameFilter = Filters.eq(User.KEY_USERNAME, username);
        MongoCursor<Document> cursor = userCollection.find(usernameFilter).cursor();
        boolean ret = cursor.hasNext();
        cursor.close();
        return ret;
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        Bson emailFilter = Filters.eq(User.KEY_EMAIL, email);
        MongoCursor<Document> cursor = userCollection.find(emailFilter).cursor();
        boolean ret = cursor.hasNext();
        cursor.close();
        return ret;
    }

    @Override
    public User storeUser(RegistrationUser newUser){

        // RegistrationUser.createDocument() also add a default role and default profile pic
        Document userDoc = newUser.createDocument();

        try{
            userCollection.insertOne(userDoc);
            //add inserted object id to the document
            String id = userDoc.getObjectId(User.KEY_ID).toString();
            userDoc.append(User.KEY_ID, id);
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

        Bson idFilter = Filters.eq(User.KEY_ID, objId);
        MongoCursor<Document> cursor = userCollection.find(idFilter).cursor();
        if(!cursor.hasNext()){
            cursor.close();
            return null;
        }
        else{
            Document userDoc = cursor.next();
            User ret = new User(userDoc);
            cursor.close();
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

        Bson idFilter = Filters.eq(User.KEY_ID, objId);
        DeleteResult ret = userCollection.deleteOne(idFilter);
        return ret.wasAcknowledged();
    }

    @Override
    public boolean changePassword(String id, String newPassword){
        if(id == "" || newPassword == "")
            return false;
        String newEncryptedPassword = UserService.passwordEncryption(newPassword);
        Bson idFilter = Filters.eq(User.KEY_ID, new ObjectId(id));
        UpdateResult res = userCollection.updateOne(idFilter, set("password", newEncryptedPassword));
        return res.wasAcknowledged();
    }

    @Override
    public boolean updateFollowersCounter(User user, int k) {
        if(user == null || k < -1 || k > 1  || k == 0){
            return false;
        }
        String userId = user.getId();

        Bson idFilter = Filters.eq(User.KEY_ID, new ObjectId(userId));
        UpdateResult res = userCollection.updateOne(idFilter, Updates.inc("followers", k));
        return res.wasAcknowledged();
    }
}
