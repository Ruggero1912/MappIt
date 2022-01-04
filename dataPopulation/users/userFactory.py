from re import split
import pymongo
from bson.objectid import ObjectId
from faker import Faker

from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

import os
from dotenv import load_dotenv, find_dotenv

def start():
    load_dotenv(find_dotenv())
    return True

from users.user import User
from utilities.utils import Utils

class UserFactory:

    LOGGER                      = Utils.start_logger("UserFactory")

    fake                        = Utils.fake        #Faker(LOCALIZATION)

    fake                = Faker(LOCALIZATION)

    CONNECTION_STRING           = os.getenv("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = os.getenv("MONGO_DATABASE_NAME")
    USERS_COLLECTION_NAME       = os.getenv("COLLECTION_NAME_USERS")

    CONNECTION_STRING           = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = Utils.load_config("MONGO_DATABASE_NAME")
    USERS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_USERS")

    NEO4J_URI                   = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME               = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER               = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD                = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_USER_LABEL            = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL           = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_POST_LABEL            = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_RELATION_USER_VISITED_PLACE = Utils.load_config("NEO4J_RELATION_USER_VISITED_PLACE")
    NEO4J_RELATION_USER_FOLLOWS_USER = Utils.load_config("NEO4J_RELATION_USER_FOLLOWS_USER")
    NEO4J_RELATION_USER_LIKES_POST = Utils.load_config("NEO4J_RELATION_USER_LIKES_POST")
    NEO4J_RELATION_USER_FAVOURITES_PLACE = Utils.load_config("NEO4J_RELATION_USER_FAVOURITES_PLACE")

    neo_driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    USER_YT_CHANNEL_ID_KEY      = Utils.load_config("USER_YT_CHANNEL_ID_KEY")
    USER_ID_KEY                 = Utils.load_config("USER_ID_KEY")
    USER_POST_ARRAY_KEY         = Utils.load_config("USER_POST_ARRAY_KEY")

    USER_FLICKR_ACCOUNT_ID_KEY  = Utils.load_config("USER_FLICKR_ACCOUNT_ID_KEY")

    USERS_COLLECTION        = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME][USERS_COLLECTION_NAME]

    def get_author_id_from_YTchannel(channel_id, channel_name):
        """
        return the id of an user associated to the given channel_id
        if a user associated with that channel_id does not exists, 
        create a user starting from the given channel_name and returns its user _id
        """
        associated_user = UserFactory.find_user_by_YT_channel_id(channel_id)
        if associated_user is None:
            #if there is no user associated with that channel, we have to create it
            new_user_id = UserFactory.create_user_by_username(username=channel_name)
            #we have to bind the channel id to the newly created user
            UserFactory.bind_user_to_channel(user_id=new_user_id, channel_id=channel_id)
            return new_user_id
        else:
            return associated_user[UserFactory.USER_ID_KEY]

    def get_author_id_from_flickr_account_id(flickr_account_id, flickr_username, flickr_realname = None):
        """
        return the id of an user associated to the given flickr_account_id
        if a user associated with that flickr_account_id does not exists, 
        create a user starting from the given flickr_username and returns its user _id
        """
        associated_user = UserFactory.find_user_by_Flickr_account_id(flickr_account_id)
        if associated_user is None:
            #we have to create it

            name = None
            surname = None
            if isinstance(flickr_realname, str):
                if ' ' in flickr_realname:
                    splitted = flickr_realname.split(' ', maxsplit=1)
                    name = splitted[0]
                    surname = splitted[1]
                else:
                    name = flickr_realname

            new_user_id = UserFactory.create_user_by_username(username=flickr_username, name=name, surname=surname)
            #we have to bind the channel id to the newly created user
            UserFactory.bind_user_to_Flickr_account(user_id=new_user_id, flickr_account_id=flickr_account_id)
            return new_user_id
        else:
            return associated_user[UserFactory.USER_ID_KEY]

    def create_user_by_username(username, name=None, surname=None):
        """
        :param username str 
        :return the user id 
        create an user with the given username
        returns the _id of the created user
        """
        user = User(username, name, surname)
        #here it should store it in the database
        db_ret = UserFactory.USERS_COLLECTION.insert_one(user.get_dict())
        user_id = db_ret.inserted_id

        user.set_id(str(user_id))

        UserFactory.store_in_neo_usingObj(user)
        #it should return the associated _id
        return user_id

    def store_in_neo_usingObj(user : User):
        user_id = user.get_id()
        username = user.get_username()
        return UserFactory.store_in_neo(user_id=user_id, username=username)

    def store_in_neo(user_id, username):
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        ret = session.run("MERGE (a:"+UserFactory.NEO4J_USER_LABEL+" {id: $id, username: $username})", {"id": str(user_id), "username": username})
        session.close()
        result_summary = ret.consume()
        return result_summary

    def bind_user_to_channel(user_id, channel_id):
        """
        associates the user with id 'user_id' to the given channel if exists and store the information in mongoDB
        :return True if the associated User object was updated or None if the user was not found
        """

        newvalues = { "$set": { UserFactory.USER_YT_CHANNEL_ID_KEY : channel_id } }

        ret = UserFactory.USERS_COLLECTION.update_one(filter={UserFactory.USER_ID_KEY : user_id}, update=newvalues)

        if ret.modified_count == 1:
            return True
        elif ret.modified_count == 0:
            return None
        else:
            #never happens
            return False

    def find_user_by_YT_channel_id(channel_id) -> dict:
        """
        return the user doc associated with the given channel if any in Mongo (else return None)
        """
        user = UserFactory.USERS_COLLECTION.find_one({UserFactory.USER_YT_CHANNEL_ID_KEY : channel_id})

        return user

    def find_user_by_Flickr_account_id(flickr_account_id):
        user = UserFactory.USERS_COLLECTION.find_one({UserFactory.USER_FLICKR_ACCOUNT_ID_KEY : flickr_account_id})
        return user

    def bind_user_to_Flickr_account(user_id, flickr_account_id):
        """
        associates the user with id 'user_id' to the given flickr account if exists and store the information in mongoDB
        :return True if the associated User object was updated or None if the user was not found
        """

        newvalues = { "$set": { UserFactory.USER_FLICKR_ACCOUNT_ID_KEY : flickr_account_id } }

        ret = UserFactory.USERS_COLLECTION.update_one(filter={UserFactory.USER_ID_KEY : ObjectId(user_id)}, update=newvalues)

        if ret.modified_count == 1:
            return True
        elif ret.modified_count == 0:
            return None
        else:
            #never happens
            return False

    def add_post_id_to_post_array(user_id, post_id):
        """
        adds post_id published by a specific user into post_array 
        """
        ret = UserFactory.USERS_COLLECTION.update_one({UserFactory.USER_ID_KEY : ObjectId(user_id)}, update={'$addToSet' : {UserFactory.USER_POST_ARRAY_KEY : str(post_id)}})
        return ret.modified_count

    def user_visited_place(user_id : str, place_id : str, datetime_visit : datetime = datetime.now()):
        """
        stores the visit done by a given user to a given place
        """
        if isinstance(datetime_visit, date):
            datetime_visit = Utils.convert_date_to_datetime(datetime_visit)

        if not isinstance(datetime_visit, datetime):
            UserFactory.LOGGER.warning("user_visited_place received as datetime_visit a {type} instead of datetime! Cannot proceed. \t\t Content of datetime_visit : {datetime_visit}".format(type=type(datetime_visit), datetime_visit=datetime_visit))
            return False
        
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+UserFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+str(user_id)+"""')
                    MATCH (p:"""+UserFactory.NEO4J_PLACE_LABEL+""" WHERE p.id = '"""+str(place_id)+"""')
                    CREATE (u)-[:"""+UserFactory.NEO4J_RELATION_USER_VISITED_PLACE+""" {datetime: $datetime}]->(p)
                """
        ret = session.run(query, {"datetime" : datetime_visit})
        session.close()
        result_summary = ret.consume()
        return result_summary

    def user_follows_user(follower_id : str, followed_id : str, datetime_follow : datetime = datetime.now()):
        """
        stores the follow action done by a given user to another
        - follower_id -[:FOLLOWS{datetime: datetime_follow}]-> followed_id
        """
        if follower_id == followed_id:
            UserFactory.LOGGER.warning("[x] a user cannot follow himself! received follower_id: {follower_id}".format(follower_id=follower_id))
            return
        if isinstance(datetime_follow, date):
            datetime_follow = Utils.convert_date_to_datetime(datetime_follow)

        if not isinstance(datetime_follow, datetime):
            UserFactory.LOGGER.warning("user_visited_place received as datetime_follow a {type} instead of datetime! Cannot proceed. \t\t Content of datetime_follow : {datetime_follow}".format(type=type(datetime_follow), datetime_follow=datetime_follow))
            return False
        
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+UserFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+str(follower_id)+"""')
                    MATCH (f:"""+UserFactory.NEO4J_USER_LABEL+""" WHERE f.id = '"""+str(followed_id)+"""')
                    MERGE (u)-[:"""+UserFactory.NEO4J_RELATION_USER_FOLLOWS_USER+""" {datetime: $datetime}]->(f)
                """
        ret = session.run(query, {"datetime" : datetime_follow})
        session.close()
        result_summary = ret.consume()
        return result_summary

    def user_likes_post(user_id : str, post_id : str, datetime_like : datetime = datetime.now()):
        """
        stores the like action done by a given user to a post
        - user_id -[:LIKES{datetime: datetime_like}]-> post_id
        """
        if isinstance(datetime_like, date):
            datetime_like = Utils.convert_date_to_datetime(datetime_like)

        if not isinstance(datetime_like, datetime):
            UserFactory.LOGGER.warning("user_visited_place received as datetime_like a {type} instead of datetime! Cannot proceed. \t\t Content of datetime_like : {datetime_like}".format(type=type(datetime_like), datetime_like=datetime_like))
            return False
        
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+UserFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+str(user_id)+"""')
                    MATCH (p:"""+UserFactory.NEO4J_POST_LABEL+""" WHERE p.id = '"""+str(post_id)+"""')
                    MERGE (u)-[:"""+UserFactory.NEO4J_RELATION_USER_LIKES_POST+""" {datetime: $datetime}]->(p)
                """
        ret = session.run(query, {"datetime" : datetime_like})
        session.close()
        result_summary = ret.consume()
        return result_summary

    def user_adds_place_to_favourites(user_id : str, place_id : str, datetime_fav : datetime = datetime.now()):
        """
        stores the add to favourites action done by a given user to a post
        - user_id -[:FAVOURITES{datetime: datetime_like}]-> post_id
        """
        if isinstance(datetime_fav, date):
            datetime_fav = Utils.convert_date_to_datetime(datetime_fav)

        if not isinstance(datetime_fav, datetime):
            UserFactory.LOGGER.warning("user_visited_place received as datetime_fav a {type} instead of datetime! Cannot proceed. \t\t Content of datetime_fav : {datetime_fav}".format(type=type(datetime_fav), datetime_fav=datetime_fav))
            return False
        
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """ MATCH (u:"""+UserFactory.NEO4J_USER_LABEL+""" WHERE u.id = '"""+str(user_id)+"""')
                    MATCH (p:"""+UserFactory.NEO4J_PLACE_LABEL+""" WHERE p.id = '"""+str(place_id)+"""')
                    MERGE (u)-[:"""+UserFactory.NEO4J_RELATION_USER_FAVOURITES_PLACE+""" {datetime: $datetime}]->(p)
                """
        ret = session.run(query, {"datetime" : datetime_fav})
        session.close()
        result_summary = ret.consume()
        return result_summary

    def generate_follows(user_id : str, how_many : int = 10):
        users_to_follow_ids = UserFactory.get_random_ids(how_many=how_many)
        for user_to_follow in users_to_follow_ids:
            UserFactory.user_follows_user(follower_id=user_id, followed_id=user_to_follow)
        return

    def generate_followers(user_id : str, how_many : int = 10):
        follower_users_ids = UserFactory.get_random_ids(how_many=how_many)
        for future_follower_id in follower_users_ids:
            UserFactory.user_follows_user(follower_id=future_follower_id, followed_id=user_id)
        return

    def like_to_some_posts(user_id : str, how_many : int = 10):
        posts_ids = PostFactory.get_random_ids(how_many=how_many)
        for post_id in posts_ids:
            UserFactory.user_likes_post(user_id, post_id)
        return

    def add_to_favourites_some_places(user_id : str, how_many : int = 10):
        places_ids = PlaceFactory.get_random_ids(how_many=how_many)
        for place_id in places_ids:
            UserFactory.user_adds_place_to_favourites(user_id=user_id,place_id=place_id)
        return

    def visit_some_places(user_id : str, how_many : int = 10):
        places_ids = PlaceFactory.get_random_ids(how_many=how_many)
        for place_id in places_ids:
            UserFactory.user_visited_place(user_id=user_id, place_id=place_id)
        return

    def get_random_ids(how_many : int = 10) -> list :
        """
        returns a list of random user_ids
        - returns list<str>
        """
        list_of_ids = []

        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """
                MATCH (u:{user_label})
                RETURN u, rand() as r
                ORDER BY r
                LIMIT {how_many} 
                """.format(user_label=UserFactory.NEO4J_USER_LABEL, how_many=how_many)
        ret = session.run(query)
        result_set = ret.data()
        session.close()
        if len(result_set) == 0:
            UserFactory.LOGGER.warning("[!] empty result set for get_random_ids! specified how_many value: {how_many}".format(how_many=how_many))
            return []

        for element in result_set:
            list_of_ids.append(element['u']['id'])
        return list_of_ids