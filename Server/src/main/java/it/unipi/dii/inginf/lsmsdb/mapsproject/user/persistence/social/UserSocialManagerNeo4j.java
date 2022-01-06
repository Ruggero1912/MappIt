package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.Neo4jConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.logging.Logger;

import static org.neo4j.driver.Values.parameters;

public class UserSocialManagerNeo4j implements UserSocialManager{

    private static final Logger LOGGER = Logger.getLogger(UserSocialManagerNeo4j.class.getName());

    private static final String IDKEY = "id";
    private static final String USERNAMEKEY = "username";

    private static final String USERLABEL = "User";     // TODO: retrieve the label from the Graph DB, maybe with CALL db.labels

    public UserSocialManagerNeo4j(){
    }

    @Override
    public User storeUser(User newUser) {

        String id = newUser.getId();
        String username = newUser.getUsername();

        try (Session session = Neo4jConnection.getDriver().session()) {
            session.writeTransaction((TransactionWork<User>) tx -> {
                tx.run("CREATE (u:"+ USERLABEL +" { "+ IDKEY +": $id, "+ USERNAMEKEY +"username: $username })",
                        parameters(IDKEY, id, USERNAMEKEY, username));
                return newUser;
            });
        } catch (Neo4jException ne){
            return null;
        }
        return null;
    }
}
