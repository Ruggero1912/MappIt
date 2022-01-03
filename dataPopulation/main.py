from YTposts.YTPostFactory import YTPostFactory
from FlickrPosts.FlickrPostFactory import FlickrPostFactory
from utilities.utils import *

import logging

def initilize_default_logger():
    default_logger = logging.getLogger()
    default_logger.setLevel(level=logging.CRITICAL)
    return default_logger

default_logger = initilize_default_logger()

places = Utils.load_places_list_from_mongo()

limit = 5

counter = 0

PLACE_ID_KEY = "_id"

for place in places:

    place_id = str(place[PLACE_ID_KEY])
    place_name = place[Utils.PLACE_NAME_KEY]
    (lon, lat) = Utils.load_coordinates(place)
    
    #print("calling YTPostFactory.posts_in_given_place for '{place_name}' ({lon} / {lat}) (lon / lat)".format(place_name=place_name, lon=lon,lat=lat))
    #YTPostFactory.posts_in_given_place(place_name=place_name, place_lon=lon, place_lat=lat, place_id=place_id)
    print("__________________________________________________")
    print("")
    print("calling FlickrPostFactory.posts_in_given_place for '{place_name}' ({lon} / {lat}) (lon / lat) | place_id: {place_id}".format(place_name=place_name, lon=lon,lat=lat, place_id=place_id))
    FlickrPostFactory.posts_in_given_place(place_name=place_name, place_lon=lon, place_lat=lat, place_id=place_id)

    counter += 1

    if limit != -1 and counter > limit:
        break
    