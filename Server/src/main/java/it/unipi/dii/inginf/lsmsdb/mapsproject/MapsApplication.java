package it.unipi.dii.inginf.lsmsdb.mapsproject;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MapsProject", version = "1.0", description = "Simple API by TroiAIDE"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")

public class MapsApplication {

	public static void main(String[] args) throws FileNotFoundException {
		//SpringApplication.run(MapsApplication.class, args);

		/*
		GeoLocation g = new GeoLocation(1, 134.2, -122.399, "via ambrosiana 23, livorno LI 57100");
		System.out.println(g);

		List<Place.PlaceType> pt = new ArrayList<>();
		pt.add(Place.PlaceType.COUNTRYSIDE);
		pt.add(Place.PlaceType.URBAN);
		List<String> aliases = new ArrayList<>();
		aliases.add("Trep");
		aliases.add("Treppo");
		System.out.println(aliases);
		Image img = new Image();
		Place p = new Place(1, "Tre Ponti", g, aliases, pt, img);
		System.out.println(p);


		User u = new User(1, "Marco","Bianchi","User1", "pw1", "user1@test.com", User.Role.MODERATOR);
		System.out.println(u);*/

		System.out.println(PropertyPicker.getProperty("matteo"));
	}
}