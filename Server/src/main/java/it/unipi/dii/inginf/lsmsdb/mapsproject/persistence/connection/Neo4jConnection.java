package it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection;

import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.neo4j.driver.*;

import static org.neo4j.driver.Values.parameters;

public class Neo4jConnection implements AutoCloseable{
    private static final String Neo4jURI = PropertyPicker.getProperty(PropertyPicker.Neo4jURI);
    private static final String Neo4jUsername = PropertyPicker.getProperty(PropertyPicker.Neo4jUsername);
    private static final String Neo4jPassword = PropertyPicker.getProperty(PropertyPicker.Neo4jPassword);

    private static final Neo4jConnection obj = new Neo4jConnection();
    private static Driver driver;

    public Neo4jConnection() {
        driver = GraphDatabase.driver(Neo4jURI, AuthTokens.basic( Neo4jUsername, Neo4jPassword ));
    }

    public static Neo4jConnection getObj(){
        return obj;
    }

    public static Driver getDriver() {
        return driver;
    }

    /*public void neo4jFirstTest(final String message )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( tx ->
            {
                Result result = tx.run( "CREATE (a:Greeting) " +
                                "SET a.message = $message " +
                                "RETURN a.message + ', from node ' + id(a)",
                        parameters( "message", message ) );
                return result.single().get( 0 ).asString();
            } );
            System.out.println( greeting );
        }
    }*/

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
