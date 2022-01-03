import json, logging, pymongo
from datetime import datetime
from dateutil import parser
from users.userFactory import UserFactory


from utilities.utils import Utils
from FlickrPosts.FlickrClient import FlickrClient
from FlickrPosts.FlickrPost import FlickrPost

class FlickrPostFactory:

    LOGGER                          = Utils.start_logger("FlickrPostFactory")


    CONNECTION_STRING               = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME                   = Utils.load_config("MONGO_DATABASE_NAME")
    POSTS_COLLECTION_NAME           = Utils.load_config("COLLECTION_NAME_POSTS")
    FLICKR_DETAILS_COLLECTION_NAME  = Utils.load_config("COLLECTION_NAME_FLICKR_DETAILS")

    POSTS_COLLECTION                = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][POSTS_COLLECTION_NAME]
    FLICKR_DETAILS_COLLECTION       = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][FLICKR_DETAILS_COLLECTION_NAME]


    def posts_in_given_place(place_name, place_lon, place_lat, place_id):
        """
        finds all the posts associated to the given place
        """

        posts = []

        activity_tag = None #we will not use the activity tag to filter the flickr posts (because of the low number of posts)

        flickr_photos = FlickrClient.photos_search(place_name=place_name, activity_tag=activity_tag, lon=place_lon, lat=place_lat)

        FlickrPostFactory.LOGGER.info("Found {num} pictures for {place}".format(num=len(flickr_photos), place=place_name))

        for flickr_photo in flickr_photos:

                flickr_photo_id     = flickr_photo["id"]

                #here we should check if it already exists a post in the database for the current pic
                already_existing_post = FlickrPostFactory.load_post_from_flickr_post_id(flickr_photo_id)

                if already_existing_post is not None:
                    #in this case we skip the video
                    #in general we could try to parse more details about this video, like another category
                    FlickrPostFactory.LOGGER.debug("the current Flickr post (id {flickr_photo_id}) is already present. skipping...".format(flickr_photo_id=flickr_photo_id))
                    continue

                #call the proper method to get the full details about a picture
                flickr_photo_details = FlickrClient.photos_getInfo(flickr_photo_id)

                #parse the flickr-origin post from the json response
                flickr_post = FlickrPostFactory.parse_post_from_details(flickr_photo_details, place_id)

                #store the post
                FlickrPostFactory.store_in_persistent_db(flickr_post=flickr_post, all_flickr_details=flickr_photo_details)

                posts.append(flickr_post)
       
        #return the list of creatd posts
        return posts

    def parse_post_from_details(flickr_post_full_details : dict, place_id : str) -> FlickrPost:
        """
        receives a dict with the FlickrPost details and crafts the post starting from them
        - the activity category can be determined by the FlickrPost constructor
        - the author is loaded by 'UserFactory.get_author_id_from_FlickrAccount' that is called inside this method
        :param flickr_post_full_details dict
        :return the FlickrPost object of the created post (to be stored in the database still)
        """
        
        flickr_author_id = FlickrPostFactory.get_author_id_from_flickr_response(flickr_post_full_details)
        flickr_author_username = FlickrPostFactory.get_author_username_from_flickr_response(flickr_post_full_details)
        flickr_author_realname = FlickrPostFactory.get_author_realname_from_flickr_response(flickr_post_full_details)
        flickr_post_id   = FlickrPostFactory.get_flickr_post_id_from_flickr_response(flickr_post_full_details)
        flickr_title = FlickrPostFactory.get_title_from_flickr_response(flickr_post_full_details)
        flickr_description = FlickrPostFactory.get_description_from_flickr_response(flickr_post_full_details)
        flickr_posted_datetime = FlickrPostFactory.get_posted_date_from_flickr_response(flickr_post_full_details)
        flickr_taken_datetime = FlickrPostFactory.get_taken_date_from_flickr_response(flickr_post_full_details)
        flickr_tags_array = FlickrPostFactory.get_tags_from_flickr_response(flickr_post_full_details)

        flickr_thumb_link = FlickrPostFactory.get_thumb_from_flickr_response(flickr_post_full_details)

        flickr_pic_link   = FlickrPostFactory.get_pic_link_from_flickr_response(flickr_post_full_details)

        #NOTE: at the moment the comments are ignored for anything, included category detection

        author_id = UserFactory.get_author_id_from_flickr_account_id(flickr_author_id, flickr_author_username, flickr_realname=flickr_author_realname)

        #FlickrPostFactory.LOGGER.debug("pic link: {link}".format(link=flickr_pic_link))

        #we specify everything to the constructor except for the activity, that will be determined by the script
        flickr_post = FlickrPost(author_id=author_id    ,  place_id=place_id, title=flickr_title      ,   description=flickr_description, post_date=flickr_posted_datetime, exp_date=flickr_taken_datetime,tags_array=flickr_tags_array, 
        pics_array=[flickr_pic_link], thumbnail=flickr_thumb_link, flickr_post_id=flickr_post_id)

        #FlickrPostFactory.LOGGER.debug("pics_array: {pics_arr}".format(pics_arr=flickr_post.get_pics_array()))

        return flickr_post

    def store_in_persistent_db(flickr_post : FlickrPost, all_flickr_details : dict):
        #NOTE: how is the relationship between these two documents implemented?
        #we can easily retrieve the FLICKR_DETAILS by using the flickr_post_id field of the Post

        flickr_post_doc = flickr_post.get_dict()
        ret = FlickrPostFactory.POSTS_COLLECTION.insert_one(flickr_post_doc)
        flickr_post_doc_id = ret.inserted_id

        #TODO: store inside Neo4J?

        ret_flickr_details = FlickrPostFactory.FLICKR_DETAILS_COLLECTION.insert_one(all_flickr_details)
        flickr_details_doc_id = ret_flickr_details.inserted_id

        return (flickr_post_doc_id, flickr_details_doc_id)

    def load_post_from_flickr_post_id(flickr_post_id):
        """
        :returns None if it does not exist any post associated with that flickr post id,
         or the associated post
        """
        ret = FlickrPostFactory.POSTS_COLLECTION.find_one({FlickrPost.KEY_FLICKR_POST_ID : flickr_post_id})
        return ret

    """
    "owner": {
        "nsid": "44340529@N07",
        "username": "La stella di Eli",
        "realname": "Elisabetta Bernardini",
        "location": "",
        "iconserver": "7014",
        "iconfarm": 8,
        "path_alias": "estrella-luna"
    }
    """
    def get_author_id_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["owner"]["nsid"]

    def get_author_username_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["owner"]["username"]

    def get_author_realname_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["owner"]["realname"]

    def get_flickr_post_id_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["id"]

    def get_title_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["title"]["_content"]

    def get_description_from_flickr_response(flickr_post_full_details : dict):
        return flickr_post_full_details["description"]["_content"]

    """
    "dates": {
        "posted": "1428326947",
        "taken": "2015-04-06 15:29:07",
        "takengranularity": 0,
        "takenunknown": "1",
        "lastupdate": "1428326950"
    }
    """
    def get_posted_date_from_flickr_response(flickr_post_full_details : dict) -> datetime:
        raw_posted_date = flickr_post_full_details["dates"]["posted"]
        search_characters = [' ', '-', ':']

        if not any(x in raw_posted_date for x in search_characters):
            #we assume it is epoch encoded
            epoch_time_posted = int( raw_posted_date )
            posted_datetime = datetime.fromtimestamp(epoch_time_posted)
        else:
            posted_datetime = parser.parse(raw_posted_date)
        return posted_datetime

    def get_taken_date_from_flickr_response(flickr_post_full_details : dict) -> datetime:
        raw_taken_date = flickr_post_full_details["dates"]["taken"]
        search_characters = [' ', '-', ':']

        if not any(x in raw_taken_date for x in search_characters):
            #we assume it is epoch encoded
            epoch_time_taken = int( raw_taken_date )
            taken_datetime = datetime.fromtimestamp(epoch_time_taken)
        else:
            taken_datetime = parser.parse(raw_taken_date)
        return taken_datetime

    """
    "tags": {
        "tag": [
            {
                "id": "44319199-17052953252-60504812",
                "author": "44340529@N07",
                "authorname": "La stella di Eli",
                "raw": "instagram app",
                "_content": "instagramapp",
                "machine_tag": 0
            },
            //...
            ...//
            ]
        }
    """
    def get_tags_from_flickr_response(flickr_post_full_details : dict) -> list:
        flickr_tags = []
        raw_tags_details = flickr_post_full_details["tags"]["tag"]

        if type(raw_tags_details) == list and len(raw_tags_details) > 0:

            for raw_tag in raw_tags_details:
                tag_content     = raw_tag["_content"]
                tag_raw_content = raw_tag["raw"]
                #raw content contains also spaces, here we choose which one to use:
                flickr_tags.append(tag_raw_content)
        return flickr_tags

    def get_thumb_from_flickr_response(flickr_post_full_details : dict) -> str:
        pic_id = FlickrPostFactory.get_flickr_post_id_from_flickr_response(flickr_post_full_details)
        pic_secret = flickr_post_full_details["secret"]
        pic_server = flickr_post_full_details["server"]
        return FlickrClient.get_thumbnail_link_from_id(pic_id, pic_server, pic_secret)

    def get_pic_link_from_flickr_response(flickr_post_full_details : dict) -> str:
        pic_id = FlickrPostFactory.get_flickr_post_id_from_flickr_response(flickr_post_full_details)
        pic_secret = flickr_post_full_details["secret"]
        pic_server = flickr_post_full_details["server"]

        #FlickrPostFactory.LOGGER.debug("call to FlickrClient.get_photo_link_from_id({pic_id}, {pic_server}, {pic_secret})".format(pic_id=pic_id, pic_server=pic_server, pic_secret=pic_secret))

        return FlickrClient.get_photo_link_from_id(pic_id, pic_server, pic_secret)

    