"""
retrieve the places from MongoDB
retrieve the activities from activities.json
"""

import pymongo

import json

import os
from dotenv import load_dotenv

load_dotenv("../.env")   #By default loads .env configuration variables from current directory, searching in the file ".env"

CONNECTION_STRING       = os.getenv("MONGO_CONNECTION_STRING")
DATABASE_NAME           = os.getenv("MONGO_DATABASE_NAME")
COLLECTION_NAME_PLACES  = os.getenv("COLLECTION_NAME_PLACES")
YT_API_KEY              = os.getenv("YT_API_KEY")   #TODO: create an API key with organization account?


ACTIVITES_JSON_FILE_PATH = "../../documentation/activities.json"
ACTIVITY_NAME_KEY       = "activity"
ACTIVITY_TAG_KEY        = "tags"
ACTIVITY_CATEGORY_KEY   = "category"


PLACE_NAME_KEY  = "name"
PLACE_LOC_KEY   = "loc"

def load_places_list_from_mongo() -> list:
    """
    returns the _id if a document that is the same as the one given already exists in the given collection, else returns None
    """
    myclient = pymongo.MongoClient(CONNECTION_STRING)
    mydb = myclient[DATABASE_NAME]
    mycol = mydb[COLLECTION_NAME_PLACES]
    
    cur = mycol.find()
    
    return list(cur)

def load_activities_list() -> list:
    with open(ACTIVITES_JSON_FILE_PATH, "r") as jf:
        ret = json.load(jf)
        ret = ret["activities"]
    return ret

#import google_auth_oauthlib.flow
import googleapiclient

import googleapiclient.discovery
import googleapiclient.errors

api_service_name = "youtube"
api_version = "v3"

youtube = googleapiclient.discovery.build(
    api_service_name, api_version, developerKey=YT_API_KEY)

def youtube_query(place_name, activity_tag, lon, lat) -> list:
    """
    return a list containing the videos that respects the specified filters
    """
    request = youtube.search().list(
        part="snippet",
        maxResults=25,
        q="{name} {tag}".format(name=place_name, tag=activity_tag),
        location="{lat},{lon}".format(lon=lon, lat=lat),
        locationRadius="1mi",
        type="video"
    )
    response = request.execute()
    return response['items']


places = load_places_list_from_mongo()

activities = load_activities_list()

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
            #query YT
            yt_videos = youtube_query(place_name=name, activity_tag=activity_tag, lon=lon, lat=lat)
            
            for yt_video in yt_videos:
                #retrieve useful infos for the post
                yt_infos      = {
                    "title"             : yt_video['snippet']['title'],
                    "desc"              : yt_video['snippet']['description'],
                    "thumbnail"         : yt_video['snippet']['thumbnails']['medium']['url'],    #320 x 180px
                    "publish_date"      : yt_video['snippet']['publishedAt'],
                    "channel_id"        : yt_video['snippet']['channelId'],
                    "channel_name"      : yt_video['snippet']['channelTitle']
                }
                #TODO: 
                # - the date attribute (the one which states when the experience took place) will be empty if we do not execute another API call to YT to obtain this info (but we do not have enough credits)
                # - tags : what should it contain? the activity_tag that was used to find this video? if it is found more than with only one query (and so with different tags), the other should be added? 
                # - determine the id of the activity for this post
                # - the thumbnail for the video which key should have? is it useful (I think that could be useful to use it as preview during posts listing)
                # - the YT video link which key should have? 
                # - all the other YT infos should be stored? in case, where? inside an attribute 'yt'? or in a different collection to prevent the document to become too big?

                pass

            print( yt_videos )
            print( "---------------------------------" )
            print()
            if len(yt_videos) > 0:
                
                exit()

    break

print(name, loc)