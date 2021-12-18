import json

from YTposts.YTClient import YTClient
from users.userFactory import UserFactory

class YTPostFactory:

    ACTIVITY_NAME_KEY       = "activity"
    ACTIVITY_TAG_KEY        = "tags"
    ACTIVITY_CATEGORY_KEY   = "category"

    def posts_in_given_place_iterative_queries(place_name, place_lon, place_lat, activities : list):

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

        for yt_video in yt_videos:
            

            yt_video_id     = yt_video['id']['videoId']

            yt_video_full_details = YTClient.youtube_video_details(video_id=yt_video_id)

            yt_post = YTPostFactory.parse_post_from_details(yt_video_full_details)

            #TODO: we have to determine the activity done in this video (biking, drones, etc...)
            #TODO: decide how to determine that:
            # looking at the tags?
            # searching for precise strings (i.e. the activity name) inside the title or description?

            #retrieve useful infos for the post
            yt_infos      = {
                "title"             : yt_video['snippet']['title'],
                "desc"              : yt_video['snippet']['description'],
                "thumbnail"         : yt_video['snippet']['thumbnails']['medium']['url'],    #320 x 180px
                "publish_date"      : yt_video['snippet']['publishedAt'],
                "channel_id"        : yt_video['snippet']['channelId'],
                "channel_name"      : yt_video['snippet']['channelTitle']
            }
            


            author_id = UserFactory.get_author_id_from_YTchannel(channel_id=yt_infos["channel_id"], channel_name=yt_infos["channel_name"])
            #TODO: store_in_mongo method
            store_in_mongo(details=yt_infos, yt_all_details=yt_video)

    def parse_post_from_details(yt_video_full_details : dict) -> str:
        """
        receives a dict with the yt video details and crafts the post starting from them
        :param yt_video_full_details dict
        :return the _id of the created post
        """     
        
        pass

    def dump_to_file(yt_video_dict : dict):
        #useful to generate a .json file to analyze
        with open("YT_response_example_videos_list_details.json", 'w') as fp:
            json.dump(yt_video_dict, fp=fp, indent=4)
        exit()