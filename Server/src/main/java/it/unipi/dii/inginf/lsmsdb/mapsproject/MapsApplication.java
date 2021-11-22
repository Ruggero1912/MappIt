package it.unipi.dii.inginf.lsmsdb.mapsproject;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.GeoLocation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Marker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MapsProject", version = "1.0", description = "Simple API by TroiAIDE"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")

public class MapsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapsApplication.class, args);

		/*GeoLocation g = new GeoLocation(1, 134.2, -122.399, "via ambrosiana 23, livorno LI 57100");
		System.out.println(g);

		List<Place.PlaceTypes> pt = new ArrayList<>();
		pt.add(Place.PlaceTypes.COUNTRYSIDE);
		pt.add(Place.PlaceTypes.URBAN);
		List<String> aliases = new ArrayList<>();
		aliases.add("Trep");
		aliases.add("Treppo");
		Place p = new Place(1, "Tre Ponti", g, aliases, pt, "path");
		System.out.println(p);


		User u = new User(1, "Marco","Bianchi","User1", "pw1", "user1@test.com", "USER");
		System.out.println(u);
		//costruttore 1
		Marker m = new Marker(1, u, g);
		//costruttore 2
		Marker m2 = new Marker(1, u, p);
		System.out.println("\nmarker 2 costruito tramite un geoLocation: \n"+m);
		System.out.println("\nmarker 2 costruito tramite un Place: \n"+m2);*/
	}
}