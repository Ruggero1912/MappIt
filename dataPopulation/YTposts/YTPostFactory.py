import json
import pymongo

from YTposts.YTClient import YTClient
from YTposts.YTPost import YTPost
from users.userFactory import UserFactory

from utilities.utils import Utils

import logging


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

    POSTS_COLLECTION        = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][POSTS_COLLECTION_NAME]
    YT_DETAILS_COLLECTION   = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][YT_DETAILS_COLLECTION_NAME]

    @DeprecationWarning
    def posts_in_given_place_iterative_queries(place_name, place_lon, place_lat, activities : list):
        print( """
        do not use this method, use 'posts_in_given_place' instead
        """ ) 
        return False
        for activity in activities:
            assert isinstance(activity, dict)
            activity_name = activity[YTPostFactory.ACTIVITY_NAME_KEY]
            activity_category = activity[YTPostFactory.ACTIVITY_CATEGORY_KEY]
            activity_tags = activity[YTPostFactory.ACTIVITY_TAG_KEY]

            for activity_tag in activity_tags:
                #query YT
                yt_videos = YTClient.youtube_search_query(place_name=place_name, activity_tag=activity_tag, lon=place_lon, lat=place_lat)

                
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
                    author_id = UserFactory.get_author_id_from_YTchannel(channel_id=yt_infos["channel_id"], channel_name=yt_infos["channel_name"])
                    #TODO: store_in_mongo method
                    store_in_mongo(details=yt_infos, yt_all_details=yt_video)
                    pass
    
    def posts_in_given_place(place_name, place_lon, place_lat):

        yt_videos = YTClient.youtube_search_query(place_name=place_name, lon=place_lon, lat=place_lat)

        YTPostFactory.LOGGER.info("found {num} videos for the place '{place}'".format(num=len(yt_videos), place=place_name))

        posts = []

        for yt_video in yt_videos:
            
            yt_video_id     = yt_video['id']['videoId']

            #we have to check if it already exists a post for this YT video
            already_existing_post = YTPostFactory.load_post_from_video_id(yt_video_id)

            if already_existing_post is not None:
                #in this case we skip the video
                #in general we could try to parse more details about this video, like another category
                YTPostFactory.LOGGER.debug("the current video (id {videoid}) is already present. skipping...".format(videoid=yt_video_id))
                continue

            yt_video_full_details = YTClient.youtube_video_details(video_id=yt_video_id)

            yt_post = YTPostFactory.parse_post_from_details(yt_video_full_details)

            YTPostFactory.store_in_persistent_db(yt_post=yt_post, all_yt_details=yt_video_full_details)
            
            posts.append(yt_post)
        
        return posts

    def parse_post_from_details(yt_video_full_details : dict) -> YTPost:
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

        author_id = UserFactory.get_author_id_from_YTchannel(channel_id=channel_id,
                                 channel_name=channel_name)

        #we will not specify pics_array, activity and experience date
        yt_post=YTPost(author_id=author_id  , yt_video_id=yt_video_id, yt_channel_id=channel_id ,
                       title=title          , description=description, post_date=yt_post_date   ,
                       tags_array=yt_tags   , thumbnail=yt_thumb_link
        ) 
        return yt_post

    def store_in_persistent_db(yt_post : YTPost, all_yt_details : dict):
        #NOTE: how is the relationship between these two documents implemented?
        #we can easily retrieve the YT_DETAILS by using the yt_video_id field of the Post,
        #but maybe it would be better to use as _id of the doc the id given from yt
        yt_post_doc = yt_post.get_dict()
        ret = YTPostFactory.POSTS_COLLECTION.insert_one(yt_post_doc)
        yt_post_doc_id = ret.inserted_id

        #TODO: store inside Neo4J?

        ret_yt_details = YTPostFactory.YT_DETAILS_COLLECTION.insert_one(all_yt_details)
        yt_details_doc_id = ret_yt_details.inserted_id

        return (yt_post_doc_id, yt_details_doc_id)

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