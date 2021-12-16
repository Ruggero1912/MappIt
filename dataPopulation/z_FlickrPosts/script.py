"""
retrieve the places from MongoDB
retrieve the activities from activities.json

NOTE: flickr does not have very much pictures, we cannot iterate over the tags, otherwhise we would get a lot of empty results
"""
import pymongo

import json

import os
from dotenv import load_dotenv

load_dotenv("../.env")   #By default loads .env configuration variables from current directory, searching in the file ".env"

CONNECTION_STRING       = os.getenv("MONGO_CONNECTION_STRING")
DATABASE_NAME           = os.getenv("MONGO_DATABASE_NAME")

COLLECTION_NAME_PLACES  = os.getenv("COLLECTION_NAME_PLACES")
FLICKR_API_KEY          = os.getenv("FLICKR_API_KEY")


ACTIVITY_NAME_KEY       = "activity"
ACTIVITY_TAG_KEY        = "tags"
ACTIVITY_CATEGORY_KEY   = "category"


PLACE_NAME_KEY  = "name"
PLACE_LOC_KEY   = "loc"

from dataPopulation.utilities.utils import Utils

#https://www.flickr.com/services/rest/?method=flickr.photos.search&api_key=2d81292efaa5c80af3dcb72ea977a339&text=rocca+della+verruca&lat=43.70720745038839&lon=10.533851401339211&radius=1mi&format=json&nojsoncallback=1

import requests

FLICKR_API_ENDPOINT = "https://www.flickr.com/services/rest/"

def flickr_query(place_name, activity_tag, lon, lat) -> list:
    """
    return a list containing the videos that respects the specified filters
    image link format: image_link = "https://live.staticflickr.com/{server}/{id}_{secret}.jpg"
    """
    params = {
        "method"    : "flickr.photos.search",
        "api_key"   : FLICKR_API_KEY,
        "text"      : "{place_name} {activity}".format(place_name=place_name, activity=""),
        "lat"       : lat,
        "lon"       : lon,
        "radius"    : "10mi",
        "format"    : "json",
        "nojsoncallback"    : 1 #required by Flickr to produce valid json
    }

    s = requests.Session()
    response = s.get(FLICKR_API_ENDPOINT, params=params)
    json_response = response.json()
    
    #in this way we retrieve only the first page of images, but it is enough because we have 250 photos per page
    return json_response['photos']['photo']  

def flickr_get_photo_details(photo_id) -> dict:
    params = {
        "method"    : "flickr.photos.getInfo",
        "api_key"   : FLICKR_API_KEY,
        "photo_id"  : photo_id,
        "format"    : "json",
        "nojsoncallback"    : 1 #required by Flickr to produce valid json
    }
    s = requests.Session()
    response = s.get(FLICKR_API_ENDPOINT, params=params)
    json_response = response.json()['photo']

    assert isinstance(json_response, dict)

    #delete unnecessary details:
    unnecessary_details = ['location', 'usage', 'people', 'publiceditability', 'visibility', 'geoperms', 'urls', 'media', 'isfavorite', 'rotation', 'originalsecret']
    for detail in unnecessary_details:
        json_response.pop(detail, None)

    print("###Going to print the content for photo details response from flickr for photo {id}".format(id=photo_id))

    print(json.dumps(json_response, indent=4))
    return json_response


places = Utils.load_places_list_from_mongo()

activities = Utils.load_activities_list()

counter = 0

for place in places:
    assert isinstance(place, dict)
    assert PLACE_NAME_KEY, PLACE_LOC_KEY in place   #a place document must have those keys that we need for the search
    name = place[PLACE_NAME_KEY]
    loc  = place[PLACE_LOC_KEY]

    lon  = loc["coordinates"][0]
    lat  = loc["coordinates"][1]

    print("PLACE '{place}' | lon = {lon} ; lat = {lat}".format(place=name, lon=lon, lat=lat))

    for activity in activities:
        assert isinstance(activity, dict)
        activity_name = activity[ACTIVITY_NAME_KEY]
        activity_category = activity[ACTIVITY_CATEGORY_KEY]
        activity_tags = activity[ACTIVITY_TAG_KEY]
        assert isinstance(activity_tags, list)

        for activity_tag in activity_tags:
            #query Flickr
            flickr_photos = flickr_query(place_name=name, activity_tag=activity_tag, lon=lon, lat=lat)
            
            for flickr_photo in flickr_photos:
                flickr_photo_server = flickr_photo["server"]
                flickr_photo_id     = flickr_photo["id"]
                flickr_photo_secret = flickr_photo["secret"]
                image_link = "https://live.staticflickr.com/{server}/{id}_{secret}.jpg".format(server=flickr_photo_server, id=flickr_photo_id, secret=flickr_photo_secret)
                flickr_photo["image_link"] = image_link
                flickr_photo_details = flickr_get_photo_details(flickr_photo_id)

                exit()
                pass

            print( flickr_photos )
            print( "---------------------------------" )
            print()
            if len(flickr_photos) > 0:
                break
                #exit()
        break

    counter += 1

    if counter > 10:
        break
