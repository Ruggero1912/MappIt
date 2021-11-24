package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.GeoLocation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class GeoLocationController {

    List<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
    {
        geoLocations.add(new GeoLocation(1, 134.2, -122.399, "via ambrosiana"));
        geoLocations.add(new GeoLocation(2, 34.7, 230.22, "via ernesto rossi"));
    }

    @RequestMapping(value = "/location/all", method = RequestMethod.GET, produces = "application/json")
    public List<GeoLocation> getLocations() {
        return geoLocations;
    }

}