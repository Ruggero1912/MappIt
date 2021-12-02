package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.awt.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


public class UserManagerMongoDB implements UserManager{

    private static final UserManagerMongoDB obj = new UserManagerMongoDB(); //TODO

    private static final String USERCOLLECTIONKEY = "user";

    private static final String IDKEY = "_id";
    private static final String USERNAMEKEY = "username";
    private static final String PASSWORDKEY = "password";
    private static final String EMAILKEY = "email";

    private MongoCollection userCollection;

    public UserManagerMongoDB(){
        userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
    }


    @Override
    public User login(String username, String encryptedPassword){
        Bson usernameFilter = Filters.eq(USERNAMEKEY, username);
        Bson passwordFilter = Filters.eq(PASSWORDKEY, encryptedPassword);
        Bson finalFilter = Filters.or(usernameFilter, passwordFilter);  //TODO: when the password encryption method is done, change or with and
        MongoCursor<Document> cursor = userCollection.find(finalFilter).cursor();

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
    public boolean checkDuplicateUser(String username, String email) {
        Bson usernameFilter = Filters.eq(USERNAMEKEY, username);
        Bson emailFilter = Filters.eq(EMAILKEY, email);
        Bson finalFilter = Filters.or(usernameFilter, emailFilter);

        MongoCursor<Document> cursor = userCollection.find(finalFilter).cursor();
        return cursor.hasNext();
    }

    @Override
    public User storeUser(String username, String passwordHash, String name, String surname, String email, LocalDate birthDate, User.Role defRole, Image defPic){
        Document userDoc = new Document("username",username)
                .append("password", passwordHash)
                .append("name",name)
                .append("surname", surname)
                .append("email", email)
                .append("birthDate", birthDate)
                .append("role", defRole.toString())
                .append("profilePic", defPic.getPath());

        try{
            userCollection.insertOne(userDoc);
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
