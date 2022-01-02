from re import split
import pymongo
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

class UserFactory:

    BEGIN               = start()

    LOCALIZATION        = os.getenv("LOCALIZATION")

    fake                = Faker(LOCALIZATION)

    CONNECTION_STRING           = os.getenv("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = os.getenv("MONGO_DATABASE_NAME")
    USERS_COLLECTION_NAME       = os.getenv("COLLECTION_NAME_USERS")

    NEO4J_URI = os.getenv("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME = os.getenv("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER = os.getenv("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD = os.getenv("NEO4J_DATABASE_PWD")
    NEO4J_USER_LABEL = os.getenv("NEO4J_USER_LABEL")

    neo_driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    USER_YT_CHANNEL_ID_KEY      = os.getenv("USER_YT_CHANNEL_ID_KEY")
    USER_ID_KEY                 = os.getenv("USER_ID_KEY")

    USER_FLICKR_ACCOUNT_ID_KEY  = os.getenv("USER_FLICKR_ACCOUNT_ID_KEY")

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

        pass

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

        user.set_id(user_id)

        UserFactory.store_in_neo(user)
        #it should return the associated _id
        return user_id

    def store_in_neo(user : User):
        user_id = user.get_id()
        username = user.get_username()
        return UserFactory.store_in_neo(user_id=user_id, username=username)

    def store_in_neo(user_id, username):
        session = UserFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        ret = session.run("MERGE (a:"+UserFactory.NEO4J_USER_LABEL+" {id: $id, name: $name})", {"id": user_id, "username": username})
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

        ret = UserFactory.USERS_COLLECTION.update_one(filter={UserFactory.USER_ID_KEY : user_id}, update=newvalues)

        if ret.modified_count == 1:
            return True
        elif ret.modified_count == 0:
            return None
        else:
            #never happens
            return False


