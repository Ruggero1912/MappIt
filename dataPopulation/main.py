from YTposts.YTPostFactory import YTPostFactory
from FlickrPosts.FlickrPostFactory import FlickrPostFactory
from users.userFactory import UserFactory
from places.placeFactory import PlaceFactory
from utilities.utils import *
from utilities.persistentEntitiesManager import PersistentEntitiesManager

import logging

def initilize_default_logger():
    default_logger = logging.getLogger()
    default_logger.setLevel(level=logging.CRITICAL)
    return default_logger

default_logger = initilize_default_logger()

def reset_posts_and_users():
    """ 
    deletes all the users and posts in both dbs, then generates new posts
    """
    PersistentEntitiesManager.delete_entity_kind(PersistentEntitiesManager.ENTITY_POSTS)
    PersistentEntitiesManager.delete_entity_kind(PersistentEntitiesManager.ENTITY_USERS)
    main()



def main(limit : int = 10):
    places = PlaceFactory.load_places(how_many=0)

    counter = 0

    PLACE_ID_KEY = "_id"


    for place in places:

        place_id = str(place[PLACE_ID_KEY])
        place_name = place[Utils.PLACE_NAME_KEY]
        (lon, lat) = Utils.load_coordinates(place)
        
        print("__________________________________________________")
        print("")
        print("calling YTPostFactory.posts_in_given_place for '{place_name}' ({lon} / {lat}) (lon / lat)".format(place_name=place_name, lon=lon,lat=lat))
        YTPostFactory.posts_in_given_place(place_name=place_name, place_lon=lon, place_lat=lat, place_id=place_id)
        print("__________________________________________________")
        print("")
        print("calling FlickrPostFactory.posts_in_given_place for '{place_name}' ({lon} / {lat}) (lon / lat) | place_id: {place_id}".format(place_name=place_name, lon=lon,lat=lat, place_id=place_id))
        FlickrPostFactory.posts_in_given_place(place_name=place_name, place_lon=lon, place_lat=lat, place_id=place_id)

        counter += 1
        
        if limit != -1 and counter > limit:
            break
    

def create_some_social_relations(how_many_users = 5):
    random_users = UserFactory.get_random_ids(how_many_users)
    for user_id in random_users:
        UserFactory.generate_social_relations_for_the_user(user_id=user_id)