import pymongo
from faker import Faker

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

    CONNECTION_STRING       = os.getenv("MONGO_CONNECTION_STRING")
    DATABASE_NAME           = os.getenv("MONGO_DATABASE_NAME")
    USERS_COLLECTION_NAME   = os.getenv("COLLECTION_NAME_USERS")

    USER_YT_CHANNEL_ID_KEY  = os.getenv("YTPOST_USER_YT_CHANNEL_ID_KEY")
    USER_ID_KEY             = os.getenv("USER_ID_KEY")

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

    def create_user_by_username(username):
        """
        :param username str 
        :return the user id 
        create an user with the given username
        returns the _id of the created user
        """
        user = User(username)
        #here it should store it in the database
        db_ret = UserFactory.USERS_COLLECTION.insert_one(user.get_dict())

        user_id = db_ret.inserted_id
        #it should return the associated _id
        return user_id

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



