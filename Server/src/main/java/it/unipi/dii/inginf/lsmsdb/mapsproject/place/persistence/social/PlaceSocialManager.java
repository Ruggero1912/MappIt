package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.util.List;

public interface PlaceSocialManager {

    /**
     * returns a list of Places to check out, based on the ones visited by followed users
     * @param user the User that asks for new places to check out
     * @param maxHowMany: the maximum number of Places instances to be returned
     * @return a list of Places (only a subset of information about each Place)
     */
    public List<Place> getSuggestedPlaces(User user, int maxHowMany);
}
