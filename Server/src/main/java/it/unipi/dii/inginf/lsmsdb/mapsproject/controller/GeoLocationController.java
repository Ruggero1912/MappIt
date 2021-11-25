package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.GeoLocation;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class GeoLocationController {

    List<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
    {
        geoLocations.add(new GeoLocation(1, 134.2, -122.399, "via ambrosiana"));
        geoLocations.add(new GeoLocation(2, 34.7, 230.22, "via ernesto rossi"));
    }

    @GetMapping(value = "/location/all", produces = "application/json")
    public List<GeoLocation> getLocations() {
        return geoLocations;
    }

}