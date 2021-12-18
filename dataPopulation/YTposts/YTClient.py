import os
from dotenv import load_dotenv, find_dotenv

def begin():
    load_dotenv(find_dotenv())
    return True

import googleapiclient
import googleapiclient.discovery
import googleapiclient.errors

class YTClient:

    BEGIN               = begin()

    API_SERVICE_NAME    = "youtube"
    API_VERSION         = "v3"
    YT_API_KEY          = os.getenv("YT_API_KEY")

    youtube = googleapiclient.discovery.build(API_SERVICE_NAME, API_VERSION, developerKey=YT_API_KEY)

    def youtube_search_query(place_name, lon, lat, activity_tag=None) -> list:
        """
        return a list containing the videos that respects the specified filters
        NOTE: the YT DATA API search method only return a truncated description and does not give back the tags for the video
        """
        search_query = "{name}".format(name=place_name)
        if activity_tag is not None: search_query = search_query + " {tag}".format(tag=activity_tag)

        request = YTClient.youtube.search().list(
            part="snippet",
            maxResults=25,
            q=search_query,
            location="{lat},{lon}".format(lon=lon, lat=lat),
            locationRadius="1mi",
            type="video"
        )
        response = request.execute()
        return response['items']

    def youtube_video_details(video_id) -> dict:
        """
        return a dict with all the information about the given YouTube video (specified by its id)
        """
        request = YTClient.youtube.videos().list(
            part="snippet,contentDetails,statistics",
            id=video_id
        )
        response = request.execute()
        return response['items'][0]