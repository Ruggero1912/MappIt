package it.unipi.dii.inginf.lsmsdb.mapsproject;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseUnavailableException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.FlickrPost;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.YtPost;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MapsProject", version = "1.0", description = "Simple API by TroiAIDE"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")

public class MapsApplication {

	public static void main(String[] args) throws FileNotFoundException {
		SpringApplication.run(MapsApplication.class, args);



		/*
		//NEW CLASS COORDINATE TEST

		Coordinate c = new Coordinate(23.4,567.5);
		Coordinate c2 = new Coordinate(32.3,99.9);

		List<Coordinate> coords = new ArrayList<Coordinate>();
		coords.add(c);
		coords.add(c2);

		Place p = new Place(1, "nome_posto", coords, null, null,null);
		System.out.println(p);
		*/



		/*
		//NEO4J CONNECTION TEST
		try ( Neo4jConnection greeter = new Neo4jConnection() )
		{
			greeter.neo4jFirstTest( "hello, world" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/


		/*
		//TEST FOR NEW YOUTUBE POST
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date d = new Date(System.currentTimeMillis());
		YtPost newPost = new YtPost("mongo_id", "author", "authorID", "TEST_NEW_YOUTUBE_POST", d, "descr", null, null, null, null,"videoid", "thumb", Arrays.asList("trekking","aerialphotography"));
		YtPost inserted_post = PostService.createNewYtPost(newPost);
		System.out.println(inserted_post);
		*/

		/*
		//TEST FOR NEW FLICKR POST
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date d = new Date(System.currentTimeMillis());
		FlickrPost newPost = new FlickrPost("mongo_id", "author", "authorID", "TEST_NEW_FLICKR_POST", d, "descr", Arrays.asList("img_link_1","img_link_2"), Arrays.asList("trekking","aerialphotography"));
		FlickrPost inserted_post = PostService.createNewFlickrPost(newPost);
		System.out.println(inserted_post);
		*/

	}
}