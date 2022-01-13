from logging import Logger
import pymongo
from bson.objectid import ObjectId

from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

from utilities.utils import Utils

class PlaceFactory:

    LOGGER          = Utils.start_logger("PlaceFactory")

    CONNECTION_STRING               = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME                   = Utils.load_config("MONGO_DATABASE_NAME")
    PLACE_COLLECTION_NAME           = Utils.load_config("COLLECTION_NAME_PLACES")
    
    PLACES_COLLECTION               = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][PLACE_COLLECTION_NAME]

    PLACE_ID_KEY                    = Utils.load_config("PLACE_ID_KEY")
    PLACE_FITS_KEY                  = Utils.load_config("PLACE_FITS_KEY")
    PLACE_POST_ARRAY_KEY            = Utils.load_config("PLACE_POST_ARRAY_KEY")
    PLACE_FAVOURITES_COUNTER_KEY    = Utils.load_config("PLACE_FAVOURITES_COUNTER_KEY")

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_PLACE_LABEL    = Utils.load_config("NEO4J_PLACE_LABEL")

    neo_driver          = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

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
        ret = PlaceFactory.PLACES_COLLECTION.update_one({PlaceFactory.PLACE_ID_KEY : ObjectId(place_id)}, update={'$addToSet' : {PlaceFactory.PLACE_FITS_KEY : activity_name}})
        return ret.modified_count

    def add_post_id_to_post_array(place_id, post_id):
        """
        adds post_id related to a specific place into its post_array 
        """
        ret = PlaceFactory.PLACES_COLLECTION.update_one({PlaceFactory.PLACE_ID_KEY : ObjectId(place_id)}, update={'$addToSet' : {PlaceFactory.PLACE_POST_ARRAY_KEY : str(post_id)}})
        return ret.modified_count

    def update_favourites_counter(place_id : str, num : int):
        """
        updates the favourites counter of the given Place (if the place_id corresponds to a place)
        - num should be the a relative number
        - adds num to the current value of the favourites counter
        - :returns the modified_count 
        """
        #the '$inc' operator creates the field if it does not exists,
        # it increase the counter of the given 'num' quantity (can be positive or negative) 
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={PlaceFactory.PLACE_ID_KEY : str(place_id)}, update={"$inc":{PlaceFactory.PLACE_FAVOURITES_COUNTER_KEY : num}})
        return ret.modified_count

    def get_random_ids(how_many : int = 10) -> list :
        """
        returns a list of random post_ids
        - returns list<str>
        """
        if how_many == 0:
            return []
            
        list_of_ids = []

        session = PlaceFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """
                MATCH (p:{place_label})
                RETURN p, rand() as r
                ORDER BY r
                LIMIT {how_many} 
                """.format(place_label=PlaceFactory.NEO4J_PLACE_LABEL, how_many=how_many)
        ret = session.run(query)
        result_set = ret.data()
        session.close()
        if len(result_set) == 0:
            PlaceFactory.LOGGER.warning("[!] empty result set for get_random_ids! specified how_many value: {how_many}".format(how_many=how_many))
            return []

        for element in result_set:
            list_of_ids.append(element['p']['id'])
        return list_of_ids