from logging import Logger
import pymongo

from utilities.utils import Utils

class PlaceFactory:

    LOGGER          = Utils.start_logger("PlaceFactory")

    CONNECTION_STRING               = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME                   = Utils.load_config("MONGO_DATABASE_NAME")
    PLACE_COLLECTION_NAME           = Utils.load_config("COLLECTION_NAME_PLACES")
    
    PLACES_COLLECTION               = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][PLACE_COLLECTION_NAME]

    PLACE_ID_KEY                    = Utils.load_config("PLACE_ID_KEY")
    PLACE_FITS_KEY                  = Utils.load_config("PLACE_FITS_KEY")

    def load_place_by_id(place_id):
        """
        return the place doc associated with the given _id if any in Mongo (else return None)
        """
        place = PlaceFactory.PLACES_COLLECTION.find_one({PlaceFactory.PLACE_ID_KEY : place_id})
        return place

    def add_activity_to_fits(place_id, activity_name):
        """
        adds the specified activity to the fits for the given place
        return the number of modified documents
        """
        #we use $addToSet to add the element to the array only once, in order to prevent duplicates
        # ( we use this instead of $push )
        ret = PlaceFactory.PLACES_COLLECTION.update_one({PlaceFactory.PLACE_ID_KEY : place_id}, update={'$addToSet' : {PlaceFactory.PLACE_FITS_KEY : activity_name}})
        return ret.modified_count
