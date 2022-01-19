from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)
import pymongo
from bson.objectid import ObjectId
from places.placeFactory import PlaceFactory
from utilities.utils import Utils
from posts.Post import Post
from utilities.neoConnectionManager import NeoConnectionManager

class PostFactory:

    LOGGER                          = Utils.start_logger("PostFactory")

    CONNECTION_STRING           = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = Utils.load_config("MONGO_DATABASE_NAME")
    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")

    POSTS_COLLECTION        = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][POSTS_COLLECTION_NAME]

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_POST_LABEL    = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_USER_LABEL    = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL    = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_RELATION_POST_PLACE = Utils.load_config("NEO4J_RELATION_POST_PLACE")
    NEO4J_RELATION_POST_USER = Utils.load_config("NEO4J_RELATION_POST_USER")

    neo_driver          = NeoConnectionManager.get_static_driver() #GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    def get_post_dict_by_id(post_id : str) -> dict:
        post_doc = PostFactory.POSTS_COLLECTION.find_one({Post.KEY_ID : ObjectId((str(post_id)))})
        return post_doc

    def update_likes_counter(post_id : str, num : int):
        """
        updates the likes counter of the given post (if the post_id corresponds to a post)
        - num should be the a relative number
        - adds num to the current value of the likes counter
        - :returns the modified_count 
        """
        #the '$inc' operator creates the field if it does not exists,
        # it increase the counter of the given 'num' quantity (can be positive or negative) 
        ret = PostFactory.POSTS_COLLECTION.update_one(filter={Post.KEY_ID : ObjectId(str(post_id))}, update={"$inc":{Post.KEY_LIKES_COUNTER : num}})
        #it has also to update the redundant field 'totalLikes' in the Place collection
        # retrieve the place id
        post_doc = PostFactory.get_post_dict_by_id(post_id)
        place_id = str ( post_doc.get(Post.KEY_PLACE, "") )
        PlaceFactory.increment_aggregated_likes_counter(place_id)     
        return ret.modified_count

    def get_random_ids(how_many : int = 10) -> list :
        """
        returns a list of random post_ids
        - returns list<str>
        """
        if how_many == 0:
            return []
        list_of_ids = []

        session = PostFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """
                MATCH (p:{post_label})
                RETURN p, rand() as r
                ORDER BY r
                LIMIT {how_many} 
                """.format(post_label=PostFactory.NEO4J_POST_LABEL, how_many=how_many)
        ret = session.run(query)
        result_set = ret.data()
        session.close()
        if len(result_set) == 0:
            PostFactory.LOGGER.warning("[!] empty result set for get_random_ids! specified how_many value: {how_many}".format(how_many=how_many))
            return []

        for element in result_set:
            list_of_ids.append(element['p']['id'])
        return list_of_ids
