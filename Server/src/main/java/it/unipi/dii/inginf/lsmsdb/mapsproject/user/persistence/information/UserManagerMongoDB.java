package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.List;


public class UserManagerMongoDB implements UserManager{

    private static final UserManagerMongoDB obj = new UserManagerMongoDB(); //TODO

    private static final String USERCOLLECTIONKEY = "user";

    private static final String USERNAMEKEY = "username";
    private static final String PASSWORDKEY = "password";

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
}
