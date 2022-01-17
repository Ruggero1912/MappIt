import json, logging, pymongo
from datetime import datetime, date
from dateutil import parser
from users.userFactory import UserFactory

from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

from utilities.utils import Utils
from FlickrPosts.FlickrClient import FlickrClient
from FlickrPosts.FlickrPost import FlickrPost
from places.placeFactory import PlaceFactory

class FlickrPostFactory:

    LOGGER                          = Utils.start_logger("FlickrPostFactory")


    CONNECTION_STRING               = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME                   = Utils.load_config("MONGO_DATABASE_NAME")
    POSTS_COLLECTION_NAME           = Utils.load_config("COLLECTION_NAME_POSTS")
    FLICKR_DETAILS_COLLECTION_NAME  = Utils.load_config("COLLECTION_NAME_FLICKR_DETAILS")

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_POST_LABEL    = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_USER_LABEL    = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL    = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_RELATION_POST_PLACE = Utils.load_config("NEO4J_RELATION_POST_PLACE")
    NEO4J_RELATION_POST_USER = Utils.load_config("NEO4J_RELATION_POST_USER")

    neo_driver          = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

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

        flickr_pic_index = 0

        for flickr_photo in flickr_photos:

                flickr_pic_index += 1

                flickr_photo_id     = flickr_photo["id"]

                Utils.temporary_log(f"Flickr Pic {flickr_pic_index} out of {len(flickr_photos)} | Loading details...")
                #here we should check if it already exists a post in the database for the current pic
                already_existing_post = FlickrPostFactory.load_post_from_flickr_post_id(flickr_photo_id)

                if already_existing_post is not None:
                    #in this case we skip the video
                    #in general we could try to parse more details about this video, like another category
                    #we first have to clean the output before using the logger
                    Utils.temporary_log()
                    FlickrPostFactory.LOGGER.debug("the current Flickr post (id {flickr_photo_id}) is already present. skipping...".format(flickr_photo_id=flickr_photo_id))
                    continue

                #call the proper method to get the full details about a picture
                flickr_photo_details = FlickrClient.photos_getInfo(flickr_photo_id)

                #parse the flickr-origin post from the json response
                flickr_post = FlickrPostFactory.parse_post_from_details(flickr_photo_details, place_id, place_name)

                #store the post
                FlickrPostFactory.store_in_persistent_db(flickr_post=flickr_post, all_flickr_details=flickr_photo_details)

                #here we should update the place document fits
                PlaceFactory.add_activity_to_fits(place_id=place_id, activity_name=flickr_post.get_activity())

                posts.append(flickr_post)
       
        PlaceFactory.update_last_flickr_search(place_id)
        #return the list of creatd posts
        return posts

    def parse_post_from_details(flickr_post_full_details : dict, place_id : str, place_name : str) -> FlickrPost:
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

        author_obj = UserFactory.get_author_obj_from_flickr_account_id(flickr_author_id, flickr_author_username, flickr_realname=flickr_author_realname)
        author_id = author_obj.get_id()
        author_username = author_obj.get_username()

        #we specify everything to the constructor except for the activity, that will be determined by the script
        flickr_post = FlickrPost(author_id=author_id    ,  place_id=place_id, title=flickr_title      ,   description=flickr_description, 
                                post_date=flickr_posted_datetime, exp_date=flickr_taken_datetime,tags_array=flickr_tags_array, 
                                pics_array=[flickr_pic_link], thumbnail=flickr_thumb_link, flickr_post_id=flickr_post_id    ,
                                place_name=place_name,      author_username=author_username)

        #FlickrPostFactory.LOGGER.debug("pics_array: {pics_arr}".format(pics_arr=flickr_post.get_pics_array()))
        return flickr_post

    def store_in_persistent_db(flickr_post : FlickrPost, all_flickr_details : dict):
        #NOTE: how is the relationship between these two documents implemented?
        #we can easily retrieve the FLICKR_DETAILS by using the flickr_post_id field of the Post

        flickr_post_doc = flickr_post.get_dict()
        ret = FlickrPostFactory.POSTS_COLLECTION.insert_one(flickr_post_doc)
        flickr_post_doc_id = ret.inserted_id

        ret_flickr_details = FlickrPostFactory.FLICKR_DETAILS_COLLECTION.insert_one(all_flickr_details)
        flickr_details_doc_id = ret_flickr_details.inserted_id

         #we add the post_id to posts fields of User and Place Documents

        #user_modified_rows = UserFactory.add_post_id_to_post_array( flickr_post.get_author(), flickr_post_doc_id)
        user_modified_rows = UserFactory.add_post_preview_to_post_array(flickr_post.get_author(), flickr_post)
        if user_modified_rows != 1:
            FlickrPostFactory.LOGGER.warning("The Flickr post_id has not been added to the User posts field, modified_rows = " + str(user_modified_rows))

        #place_modified_rows = PlaceFactory.add_post_id_to_post_array( flickr_post.get_place(), flickr_post_doc_id)
        place_modified_rows = PlaceFactory.add_post_preview_to_post_array(flickr_post.get_place(),  flickr_post)
        if place_modified_rows != 1:
            FlickrPostFactory.LOGGER.warning("The Flickr post_id has not been added to the Place posts field, modified_rows = " + str(place_modified_rows))

        FlickrPostFactory.store_in_neo(flickr_post_doc_id, flickr_post.get_title(), flickr_post.get_description(), flickr_post.get_thumbnail(), flickr_post.get_place(), flickr_post.get_author(), flickr_post.get_experience_date())

        return (flickr_post_doc_id, flickr_details_doc_id)

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
        desc = desc[:75] + "..." if len(desc) > 75 else ""    #first 75 chars of the description
        session = FlickrPostFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+FlickrPostFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+ str(author_id) +"""')
                    MATCH (p:"""+FlickrPostFactory.NEO4J_PLACE_LABEL+""" WHERE p.id = '"""+ str(place_id) +"""')
                    MERGE (a:"""+FlickrPostFactory.NEO4J_POST_LABEL+""" {id: $id, title: $title, description: $description, thumbnail: $thumbnail})
                    CREATE (u)-[:"""+FlickrPostFactory.NEO4J_RELATION_POST_USER+"""]->(a)
                    CREATE (a)-[:"""+FlickrPostFactory.NEO4J_RELATION_POST_PLACE+"""]->(p)
                """
        ret = session.run(query, {"id": str(post_id), "title": title, "description": desc, "thumbnail" : thumbnail})
        session.close()
        result_summary = ret.consume()
        UserFactory.user_visited_place(str(author_id), str(place_id), datetime_visit=Utils.convert_date_to_datetime(date_visit))
        return result_summary

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

    