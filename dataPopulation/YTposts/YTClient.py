from utilities.utils import Utils

import googleapiclient
import googleapiclient.discovery
import googleapiclient.errors


class YTClient:

    LOGGER              = Utils.start_logger("YTClient")
    API_SERVICE_NAME    = "youtube"
    API_VERSION         = "v3"
    YT_OVER_QUOTA       = 10000

    YT_DEFAULT_API_KEY  = Utils.load_config("YT_API_KEY")
    YT_API_KEYS_ARRAY   = Utils.load_config_json("YT_API_KEYS_ARRAY")

    API_KEYS_INFOS      = {}

    YT_API_KEY          = YT_DEFAULT_API_KEY

    youtube = googleapiclient.discovery.build(API_SERVICE_NAME, API_VERSION, developerKey=YT_API_KEY)

    def update_api_key_usages(inc : int = 1):
        """
        updates the usage of the current api key by a inc value
        - the current API key is the one in YTClient.YT_API_KEY
        """
        if YTClient.YT_API_KEY in YTClient.API_KEYS_INFOS.keys():
            YTClient.API_KEYS_INFOS[YTClient.YT_API_KEY] += inc
        else:
            YTClient.API_KEYS_INFOS[YTClient.YT_API_KEY] = inc

    def get_api_key_usages(api_key = None):
        """
        returns the usages of the specified api key, if no key is specified, returns the usages of the currently selected api key
        """
        if api_key is None: api_key = YTClient.YT_API_KEY
        return YTClient.API_KEYS_INFOS.get(api_key, 0)  #returns 0 if the given key is still not present in the dict

    def is_api_key_overquota():
        """
        returns true if the current YT_API_KEY is over quota, else False
        """
        return YTClient.API_KEYS_INFOS.get(YTClient.YT_API_KEY, 0) >= YTClient.YT_OVER_QUOTA

    def __update_api_key(new_api_key : str):
        YTClient.LOGGER.info(f"Changing API key from {YTClient.YT_API_KEY} with {YTClient.get_api_key_usages()} usages to {new_api_key} with {YTClient.get_api_key_usages(new_api_key)} usages")
        #we want to pop out from the available keys the current one, as it is supposed to be overquota
        assert isinstance(YTClient.YT_API_KEYS_ARRAY, list)
        YTClient.YT_API_KEYS_ARRAY.remove(YTClient.YT_API_KEY)
        YTClient.YT_API_KEY = new_api_key
        YTClient.youtube = googleapiclient.discovery.build(YTClient.API_SERVICE_NAME, YTClient.API_VERSION, developerKey=YTClient.YT_API_KEY)
        return

    def change_api_key():
        """
        updates the current YTClient.YT_API_KEY to another API key which has less usages than the current key
        - returns True if the key has been changed, else False
        """
        current_api_key = YTClient.YT_API_KEY
        current_api_key_usages = YTClient.get_api_key_usages()
        for api_key in YTClient.YT_API_KEYS_ARRAY:
            if api_key != current_api_key and current_api_key_usages > YTClient.get_api_key_usages(api_key):
                YTClient.__update_api_key(api_key)
                break
        if(YTClient.is_api_key_overquota()):
            YTClient.LOGGER.error(" [x] ALL THE YOUTUBE API KEYS ARE OVER QUOTA! [x] ")
        return YTClient.YT_API_KEY != current_api_key


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
        try:
            response = request.execute()
        except:
            YTClient.change_api_key()
            return YTClient.youtube_search_query(place_name, lon, lat, activity_tag)
        YTClient.update_api_key_usages()
        if YTClient.is_api_key_overquota():
            YTClient.change_api_key()
        return response['items']

    def youtube_video_details(video_id) -> dict:
        """
        return a dict with all the information about the given YouTube video (specified by its id)
        """
        request = YTClient.youtube.videos().list(
            part="snippet,contentDetails,statistics",
            id=video_id
        )
        try:
            response = request.execute()
        except:
            YTClient.change_api_key()
            return YTClient.youtube_video_details(video_id)
        YTClient.update_api_key_usages()
        if YTClient.is_api_key_overquota():
            YTClient.change_api_key()
        return response['items'][0]