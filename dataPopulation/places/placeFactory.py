import pymongo
from bson.objectid import ObjectId
from datetime import datetime
from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)
from utilities.neoConnectionManager import NeoConnectionManager

from utilities.utils import Utils
from posts.Post import Post
from places.place import Place

class PlaceFactory:

    LOGGER          = Utils.start_logger("PlaceFactory")

    CONNECTION_STRING               = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME                   = Utils.load_config("MONGO_DATABASE_NAME")
    PLACE_COLLECTION_NAME           = Utils.load_config("COLLECTION_NAME_PLACES")
    
    PLACES_COLLECTION               = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][PLACE_COLLECTION_NAME]

    PLACE_ID_KEY                    = Utils.load_config("PLACE_ID_KEY")
    PLACE_NAME_KEY                  = Utils.load_config("PLACE_NAME_KEY")
    PLACE_FITS_KEY                  = Utils.load_config("PLACE_FITS_KEY")
    PLACE_POST_ARRAY_IDS_KEY        = Utils.load_config("PLACE_POST_IDS_ARRAY_KEY")
    PLACE_POST_ARRAY_KEY            = Utils.load_config("PLACE_POST_ARRAY_KEY")
    PLACE_FAVOURITES_COUNTER_KEY    = Utils.load_config("PLACE_FAVOURITES_COUNTER_KEY")
    PLACE_LAST_YT_SEARCH_KEY        = Utils.load_config("PLACE_LAST_YT_SEARCH_KEY")
    PLACE_LAST_FLICKR_SEARCH_KEY    = Utils.load_config("PLACE_LAST_FLICKR_SEARCH_KEY")
    PLACE_TOTAL_LIKES_COUNTER_KEY   = Utils.load_config("PLACE_TOTAL_LIKES_COUNTER_KEY")

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_PLACE_LABEL    = Utils.load_config("NEO4J_PLACE_LABEL")

    neo_driver = NeoConnectionManager.get_static_obj()
    #GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    def load_place_by_id(place_id):
        """
        return the place doc associated with the given _id if any in Mongo (else return None)
        """
        place = PlaceFactory.PLACES_COLLECTION.find_one({PlaceFactory.PLACE_ID_KEY : place_id})
        return place

    def store_place(place_obj : Place) -> str:
        """
        stores a given place object in MongoDB
        :returns the given place_obj with the id set to the id of the stored document
        """
        inserted_id = PlaceFactory.__store_in_mongo(place_obj)
        place_obj.set_id(inserted_id)
        #it has to store the place in Neo4J too!
        PlaceFactory.__store_in_neo(place_obj)
        return place_obj

    def __store_in_mongo(place_obj : Place) -> str:
        """
        returns the _id of the stored document
        """
        ret = PlaceFactory.PLACES_COLLECTION.insert_one(place_obj.get_dict())
        return str(ret.inserted_id)

    def __store_in_neo(place_obj : Place):
        session = PlaceFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        place_id = place_obj.get_id()
        place_name = place_obj.get_name()
        ret = session.run(f"MERGE (a:{PlaceFactory.NEO4J_PLACE_LABEL} {{id: $id, name: $name}})", {"id": place_id, "name": place_name})
        result_summary = ret.consume()
        session.close()
        return result_summary.counters.nodes_created

    def is_place_already_present(place_obj : Place):
        """
        returns True if a Place with:
        - the same osm_id 
        - or
        - the same loc attribute
        - or
        - the same name and a position in a radius of 0.5 km
        is already present in the Places collection, else False
        """
        radius_in_km = 0.5
        radius_in_degrees = radius_in_km / 111.12
        lon, lat = place_obj.get_center()
        #if the given place does not have proper coordinates, returns that the place is already present to avoid the insertion of spare elements
        if lon is False:    return True
        fil = {
            "$or" : [
            {Place.KEY_OSM_ID   : place_obj.get_osm_id()},
            #{Place.KEY_LOC      : place_obj.get_loc()   },
            {"$and" : [
                { Place.KEY_NAME        : place_obj.get_name()  },
                #{"$geoWithin": {"$center"  : [[lon, lat], radius_in_degrees]}}
                {Place.KEY_LOC : {"$geoWithin"    : {"$center" : [[lon, lat], radius_in_degrees]}} }
                ]
            }
            ]
        }
        cur = PlaceFactory.PLACES_COLLECTION.find(filter=fil)
        if len(list(cur)):
            return True
        else:
            return False

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
        ret = PlaceFactory.PLACES_COLLECTION.update_one({PlaceFactory.PLACE_ID_KEY : ObjectId(place_id)}, update={'$addToSet' : {PlaceFactory.PLACE_POST_ARRAY_IDS_KEY : str(post_id)}})
        return ret.modified_count

    def add_post_preview_to_post_array(place_id : str, post_obj : Post):
        """
        adds a nested document "Post Preview" which contains a reduced sets of the attributes of the object Post to the document of the place which has the given place_id
        """
        ret = PlaceFactory.PLACES_COLLECTION.update_one({PlaceFactory.PLACE_ID_KEY : ObjectId(str(place_id))}, update={'$addToSet' : {PlaceFactory.PLACE_POST_ARRAY_KEY : post_obj.get_post_preview_dict()}})
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
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={PlaceFactory.PLACE_ID_KEY : ObjectId(str(place_id))}, update={"$inc":{PlaceFactory.PLACE_FAVOURITES_COUNTER_KEY : num}})
        return ret.modified_count

    def set_favourites_counter(place_id : str, value : int):
        """
        sets the favourites counter attribute to the given value
        - :param num should be a positive number
        - :returns the modified_count 
        """
        if(value < 0):
            PlaceFactory.LOGGER.warning(f"[x] 'set_favourites_counter': received a negative number for the favouritesCounter! num: {value} ")
            return 0
        if(value == 0):
            PlaceFactory.LOGGER.debug(f"[-] 'set_favourites_counter': received 0 as favouritesCounter for the place_id: {place_id} ")
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={Place.KEY_ID : ObjectId(str(place_id))}, update={"$set":{Place.KEY_FAVOURITES_COUNTER : value}})
        return ret.modified_count

    def increment_aggregated_likes_counter(place_id : str):
        """
        updates the redundant aggregated field 'totalLikes' which indicates the total of likes received by all the posts done in the specified place
        """
        num = 1
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={PlaceFactory.PLACE_ID_KEY : ObjectId(str(place_id))}, update={"$inc":{PlaceFactory.PLACE_TOTAL_LIKES_COUNTER_KEY : num}})
        return ret.modified_count

    def set_total_likes_counter(place_id : str, value : int):
        """
        sets the total likes counter attribute to the given value
        - :param num should be a positive number
        - :returns the modified_count 
        """
        if(value < 0):
            PlaceFactory.LOGGER.warning(f"[x] 'set_total_likes_counter': received a negative number for the likesCounter! num: {value} ")
            return 0
        if(value == 0):
            PlaceFactory.LOGGER.debug(f"[-] 'set_total_likes_counter': received 0 as likesCounter for the place_id: {place_id} ")
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={Place.KEY_ID : ObjectId(str(place_id))}, update={"$set":{Place.KEY_TOTAL_LIKES_COUNTER : value}})
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

    def load_places(how_many : int = 10, random = False):
        """
        returns a list of dicts from the Place Mongo collection
        - :param how_many returns the specified number of places [[ if set to 0, returns all the places, if random is False ]]
        - :param random if set to True, gives random result
        """
        if random == True:
            #db.mycoll.aggregate([{ $sample: { size: how_many } }])
            if how_many <= 0:
                how_many = 10
            cur = PlaceFactory.PLACES_COLLECTION.aggregate([{"$sample" : {"size" : how_many}}])
        else:
            cur = PlaceFactory.PLACES_COLLECTION.find()
        return list(cur)

    def load_places_for_yt_search(how_many : int = 10):
        """
        returns a list of dicts from the place mongo collection ordered by the value of the timestamp field 'lastYTsearch'
        - returns as firsts the ones without the field or with an empty value, then ordered by increasing timestamp
        - :param how_many returns the specified number of places, returns all if set to 0
        """
        cur = PlaceFactory.PLACES_COLLECTION.find().sort( PlaceFactory.PLACE_LAST_YT_SEARCH_KEY, pymongo.ASCENDING ).limit(how_many)
        return list(cur)

    def load_places_for_flickr_search(how_many : int = 10):
        """
        returns a list of dicts from the place mongo collection ordered by the value of the timestamp field 'lastFlickrSearch'
        - :returns as firsts the ones without the field or with an empty value, then ordered by increasing timestamp
        - :param how_many returns the specified number of places, returns all if set to 0
        """
        cur = PlaceFactory.PLACES_COLLECTION.find().sort( PlaceFactory.PLACE_LAST_FLICKR_SEARCH_KEY, pymongo.ASCENDING ).limit(how_many)
        return list(cur)

    def update_last_yt_search(place_id : str):
        """
        updates the field 'lastYTsearch' of the place document whose id is place_id to now(), if the document with the given id exists
        """
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={"_id" : ObjectId(str(place_id))}, update={"$set" : {PlaceFactory.PLACE_LAST_YT_SEARCH_KEY : datetime.now()}})
        return ret.modified_count

    def update_last_flickr_search(place_id : str):
        """
        updates the field 'lastFlickrSearch' of the place document whose id is place_id to now(), if the document with the given id exists
        """
        ret = PlaceFactory.PLACES_COLLECTION.update_one(filter={"_id" : ObjectId(str(place_id))}, update={"$set" : {PlaceFactory.PLACE_LAST_FLICKR_SEARCH_KEY : datetime.now()} })
        return ret.modified_count
