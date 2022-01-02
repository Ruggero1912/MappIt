from utilities.utils import Utils

import requests, json

class FlickrClient:

    FLICKR_API_KEY      = Utils.load_config("FLICKR_API_KEY")
    FLICKR_API_ENDPOINT = "https://www.flickr.com/services/rest/"
    STATIC_IMAGE_URI    = "https://live.staticflickr.com/{server}/{id}_{secret}_{size}.jpg"
    THUMBNAIL_SIZE      = "t"

    HTTP_SESSION        = requests.Session()


    def photos_search(place_name, activity_tag, lon, lat) -> list:
        """
        return a list containing the videos that respects the specified filters
        image link format: image_link = "https://live.staticflickr.com/{server}/{id}_{secret}.jpg"
        """
        params = {
            "method"    : "flickr.photos.search",
            "api_key"   : FlickrClient.FLICKR_API_KEY,
            "text"      : "{place_name} {activity}".format(place_name=place_name, activity=""),
            "lat"       : lat,
            "lon"       : lon,
            "radius"    : "10mi",
            "format"    : "json",
            "nojsoncallback"    : 1 #required by Flickr to produce valid json
        }

        response = FlickrClient.HTTP_SESSION.get(FlickrClient.FLICKR_API_ENDPOINT, params=params)
        json_response = response.json()
        
        #in this way we retrieve only the first page of images, but it is enough because we have 250 photos per page
        return json_response['photos']['photo']


    def photos_getInfo(photo_id) -> dict:
        params = {
            "method"    : "flickr.photos.getInfo",
            "api_key"   : FlickrClient.FLICKR_API_KEY,
            "photo_id"  : photo_id,
            "format"    : "json",
            "nojsoncallback"    : 1 #required by Flickr to produce valid json
        }

        response = FlickrClient.HTTP_SESSION.get(FlickrClient.FLICKR_API_ENDPOINT, params=params)
        json_response = response.json()['photo']

        assert isinstance(json_response, dict)

        #delete unnecessary details:
        unnecessary_details = ['location', 'usage', 'people', 'publiceditability', 'visibility', 'geoperms', 'urls', 'media', 'isfavorite', 'rotation', 'originalsecret']
        for detail in unnecessary_details:
            json_response.pop(detail, None)

        return json_response

    def get_photo_link_from_id(flickr_photo_id : str, flickr_photo_server : str, flickr_photo_secret : str, size : str = "b"):
        """
        available sizes:
         - s
         - z (t / m)    -> use _t for the thumbnail
         - b
        """
        if size not in [ "b" , "s", "z" , "t" , "m" ]:
            size = "b"

        return FlickrClient.STATIC_IMAGE_URI.format(server=flickr_photo_server, id=flickr_photo_id, secret=flickr_photo_secret, size=size)

    def get_thumbnail_link_from_id(flickr_photo_id : str, flickr_photo_server : str, flickr_photo_secret : str):

        return FlickrClient.get_photo_link_from_id(flickr_photo_id, flickr_photo_server, flickr_photo_secret, size=FlickrClient.THUMBNAIL_SIZE)

