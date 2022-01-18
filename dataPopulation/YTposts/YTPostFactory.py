import json
import pymongo

from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

from YTposts.YTClient import YTClient
from YTposts.YTPost import YTPost
from users.userFactory import UserFactory
from places.placeFactory import PlaceFactory

from utilities.neoConnectionManager import NeoConnectionManager
from utilities.utils import Utils

import logging
from datetime import datetime, date


def start_logger(logger_name):
    logger = logging.getLogger(logger_name)
    logger.setLevel(level=logging.DEBUG)
    ch = logging.StreamHandler()
    ch.setLevel(level=logging.DEBUG)
    formatter = logging.Formatter('%(asctime)s - %(name)s - [%(levelname)s] - %(message)s')
    ch.setFormatter(formatter)
    logger.addHandler(ch)
    return logger

class YTPostFactory:

    LOGGER = start_logger("YTPostFactory")

    ACTIVITY_NAME_KEY       = "activity"
    ACTIVITY_TAG_KEY        = "tags"
    ACTIVITY_CATEGORY_KEY   = "category"

    CONNECTION_STRING           = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = Utils.load_config("MONGO_DATABASE_NAME")
    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")
    YT_DETAILS_COLLECTION_NAME  = Utils.load_config("COLLECTION_NAME_YT_DETAILS")

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

    POSTS_COLLECTION        = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][POSTS_COLLECTION_NAME]
    YT_DETAILS_COLLECTION   = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][YT_DETAILS_COLLECTION_NAME]
    
    def posts_in_given_place(place_name, place_lon, place_lat, place_id):

        yt_videos = YTClient.youtube_search_query(place_name=place_name, lon=place_lon, lat=place_lat)

        YTPostFactory.LOGGER.info("found {num} videos for the place '{place}'".format(num=len(yt_videos), place=place_name))

        posts = []

        video_results_index = 0

        for yt_video in yt_videos:

            video_results_index += 1
            
            yt_video_id     = yt_video['id']['videoId']

            Utils.temporary_log(f"Video {video_results_index} out of {len(yt_videos)} | Loading details...")

            #we have to check if it already exists a post for this YT video
            already_existing_post = YTPostFactory.load_post_from_video_id(yt_video_id)

            if already_existing_post is not None:
                #in this case we skip the video
                #in general we could try to parse more details about this video, like another category
                #we first have to clean the output before using the logger
                Utils.temporary_log()
                YTPostFactory.LOGGER.debug("the current video (id {videoid}) is already present. skipping...".format(videoid=yt_video_id))
                continue

            yt_video_full_details = YTClient.youtube_video_details(video_id=yt_video_id)

            yt_post = YTPostFactory.parse_post_from_details(yt_video_full_details, place_id, place_name)

            YTPostFactory.store_in_persistent_db(yt_post=yt_post, all_yt_details=yt_video_full_details)

            #here we should update the place document fits
            PlaceFactory.add_activity_to_fits(place_id=place_id, activity_name=yt_post.get_activity())
            
            
            posts.append(yt_post)
        
        PlaceFactory.update_last_yt_search(place_id=place_id)
        return posts

    def parse_post_from_details(yt_video_full_details : dict, place_id : str, place_name : str) -> YTPost:
        """
        receives a dict with the yt video details and crafts the post starting from them
        - the activity category can be determined by the YTPost constructor
        - the author is loaded by 'UserFactory.get_author_id_from_YTchannel' that is called inside this method
        :param yt_video_full_details dict
        :return the YTPost object of the created post (to be stored in the database still)
        """
        channel_id = YTPostFactory.get_channelId_yt_resp(yt_video_full_details)
        channel_name=YTPostFactory.get_channelName_yt_resp(yt_video_full_details)
        yt_video_id = YTPostFactory.get_videoId_yt_resp(yt_video_full_details)
        title = YTPostFactory.get_title_yt_resp(yt_video_full_details)
        description = YTPostFactory.get_description_yt_resp(yt_video_full_details)
        yt_post_date = YTPostFactory.get_date_yt_resp(yt_video_full_details)
        yt_tags     = YTPostFactory.get_tags_yt_resp(yt_video_full_details)
        yt_thumb_link = YTPostFactory.get_thumb_link_yt_resp(yt_video_full_details)

        author_obj = UserFactory.get_author_obj_from_YTchannel(channel_id=channel_id,
                                 channel_name=channel_name)
        author_id = author_obj.get_id()
        author_username = author_obj.get_username()

        #we will not specify pics_array, activity and experience date
        yt_post=YTPost(author_id=author_id  , yt_video_id=yt_video_id, yt_channel_id=channel_id ,
                       title=title          , description=description, post_date=yt_post_date   ,
                       tags_array=yt_tags   , thumbnail=yt_thumb_link, place_id=place_id        ,
                       author_username=author_username, place_name=place_name
        ) 
        return yt_post

    def store_in_persistent_db(yt_post : YTPost, all_yt_details : dict):
        #NOTE: how is the relationship between these two documents implemented?
        #we can easily retrieve the YT_DETAILS by using the yt_video_id field of the Post,
        #but maybe it would be better to use as _id of the doc the id given from yt
        yt_post_doc = yt_post.get_dict()
        ret = YTPostFactory.POSTS_COLLECTION.insert_one(yt_post_doc)
        yt_post_doc_id = ret.inserted_id

        ret_yt_details = YTPostFactory.YT_DETAILS_COLLECTION.insert_one(all_yt_details)
        yt_details_doc_id = ret_yt_details.inserted_id

        #we add the post_id to posts fields of User and Place Documents

        #user_modified_rows = UserFactory.add_post_id_to_post_array( yt_post.get_author(), yt_post_doc_id)
        user_modified_rows = UserFactory.add_post_preview_to_post_array( yt_post.get_author(), yt_post)
        if user_modified_rows != 1:
            YTPostFactory.LOGGER.warning("The YouTube post_id has not been added to the User posts field, modified_rows = " + str(user_modified_rows))

        #place_modified_rows = PlaceFactory.add_post_id_to_post_array( yt_post.get_place(), yt_post_doc_id)
        place_modified_rows = PlaceFactory.add_post_preview_to_post_array( yt_post.get_place(), yt_post)
        if place_modified_rows != 1:
            YTPostFactory.LOGGER.warning("The YouTube post_id has not been added to the Place posts field, modified_rows = " + str(place_modified_rows))

        YTPostFactory.store_in_neo(yt_post_doc_id, yt_post.get_title(), yt_post.get_description(), yt_post.get_thumbnail(), yt_post.get_place(), yt_post.get_author(), yt_post.get_experience_date())

        return (yt_post_doc_id, yt_details_doc_id)

    def store_in_neo(post_id, title, desc, thumbnail, place_id, author_id, date_visit : date):
        """
        the Post node should have the attributes:
        - id
        - title
        - description   (just an excerpt)
        - thumbnail
        We have to create the relationship between the Post node and the Place node (relation "LOCATION")
        We have to create the relationship between the Post node and the User node (relation "AUTHOR")
        """
        desc = desc[:75]    #first 75 chars of the description
        session = YTPostFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+YTPostFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+str(author_id)+"""')
                    MATCH (p:"""+YTPostFactory.NEO4J_PLACE_LABEL+""" WHERE p.id = '"""+str(place_id)+"""')
                    MERGE (a:"""+YTPostFactory.NEO4J_POST_LABEL+""" {id: $id, title: $title, description: $description, thumbnail: $thumbnail})
                    CREATE (u)-[:"""+YTPostFactory.NEO4J_RELATION_POST_USER+"""]->(a)
                    CREATE (a)-[:"""+YTPostFactory.NEO4J_RELATION_POST_PLACE+"""]->(p)
                """
        ret = session.run(query, {"id": str(post_id), "title": title, "description": desc, "thumbnail" : thumbnail})
        session.close()
        result_summary = ret.consume()
        UserFactory.user_visited_place(str(author_id), str(place_id), datetime_visit=Utils.convert_date_to_datetime(date_visit))
        return result_summary

    def load_post_from_video_id(yt_video_id):
        """
        :returns None if it does not exist any post associated with that video id,
         or the associated post
        """
        ret = YTPostFactory.POSTS_COLLECTION.find_one({YTPost.KEY_YT_VIDEO_ID : yt_video_id})
        return ret

    def get_videoId_yt_resp(yt_resp : dict):
        return yt_resp['id']

    def get_title_yt_resp(yt_resp : dict):
        return yt_resp['snippet']['title']

    def get_description_yt_resp(yt_resp : dict):
        return yt_resp['snippet']['description']

    def get_date_yt_resp(yt_resp : dict):
        return yt_resp['snippet']['publishedAt']

    def get_channelId_yt_resp(yt_resp : dict):
        return yt_resp['snippet']['channelId']

    def get_channelName_yt_resp(yt_resp : dict):
        return yt_resp['snippet']['channelTitle']

    def get_tags_yt_resp(yt_resp : dict):
        if 'tags' not in yt_resp['snippet'].keys():
            return []
        tags = yt_resp['snippet']['tags']
        assert isinstance(tags, list)
        return tags

    def get_thumb_link_yt_resp(yt_resp : dict, thumb_type : str = "default"):
        all_thumbs = yt_resp['snippet']['thumbnails']
        assert isinstance(all_thumbs, dict)
        if thumb_type not in all_thumbs.keys():
            old_thumb_type = thumb_type
            thumb_type = all_thumbs.keys()[0]
            print("'{old}' type thumbnail not found | using '{new}' thumb".format(old=old_thumb_type, new=thumb_type))
        return all_thumbs[thumb_type]['url']

    def dump_to_file(yt_video_dict : dict):
        #useful to generate a .json file to analyze
        with open("YT_response_example_videos_list_details.json", 'w') as fp:
            json.dump(yt_video_dict, fp=fp, indent=4)
        exit()