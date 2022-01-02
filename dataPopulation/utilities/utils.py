from typing import Tuple
import pymongo
import json
from faker import Faker
import logging

import os
from dotenv import load_dotenv, find_dotenv


def begin():
    #dotenv_path = os.path.dirname(__file__) + "..\.env"
    #print("loading .env with filepath {path}".format(path=dotenv_path))
    load_dotenv(find_dotenv())   #By default loads .env configuration variables from current directory, searching in the file ".env"
    return True

class Utils:

    BEGUN                   = begin()

    CONNECTION_STRING       = os.getenv("MONGO_CONNECTION_STRING")
    DATABASE_NAME           = os.getenv("MONGO_DATABASE_NAME")
    COLLECTION_NAME_PLACES  = os.getenv("COLLECTION_NAME_PLACES")

    FAKER_LOCALIZATION      = os.getenv("FAKER_LOCALIZATION")

    fake                    = Faker(FAKER_LOCALIZATION)

    PLACE_NAME_KEY          = "name"
    PLACE_LOC_KEY           = "loc"

    ACTIVITES_JSON_FILE_PATH = "../documentation/activities.json"

    def load_config(config_key : str) -> str :
        return os.getenv(config_key)

    def start_logger(logger_name : str):
        logger = logging.getLogger(logger_name)
        logger.setLevel(level=logging.DEBUG)
        ch = logging.StreamHandler()
        ch.setLevel(level=logging.DEBUG)
        formatter = logging.Formatter('%(asctime)s - %(name)s - [%(levelname)s] - %(message)s')
        ch.setFormatter(formatter)
        logger.addHandler(ch)
        return logger

    def load_places_list_from_mongo() -> list:
        """
        returns the _id if a document that is the same as the one given already exists in the given collection, else returns None
        """
        myclient = pymongo.MongoClient(Utils.CONNECTION_STRING)
        mydb = myclient[Utils.DATABASE_NAME]
        mycol = mydb[Utils.COLLECTION_NAME_PLACES]
        
        cur = mycol.find()
        
        return list(cur)

    def load_coordinates(place : dict) -> Tuple:
        """
        :param place dict
        :return a tuple (lon, lat)
        """

        lon = place[Utils.PLACE_LOC_KEY]["coordinates"][0]
        lat = place[Utils.PLACE_LOC_KEY]["coordinates"][1]
        return lon, lat

    def load_activities_list() -> list:
        """
        list of dict, each dict is:
        {
            "activity"  : str,
            "tags"      : list of str,
            "category": str
        }
        """
        with open(Utils.ACTIVITES_JSON_FILE_PATH, "r") as jf:
            ret = json.load(jf)
            ret = ret["activities"]
        return ret

    def load_activities_names(activities_list : list = None) -> list:
        """
        :returns list of str (the activities names)
        """
        if not activities_list:
            activities_list = Utils.load_activities_list()
        activities_names = []
        for activity in activities_list:
            activities_names.append(activity['activity'])
        return activities_names
        

