import pymongo
import json
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

    ACTIVITES_JSON_FILE_PATH = "../documentation/activities.json"

    def load_places_list_from_mongo() -> list:
        """
        returns the _id if a document that is the same as the one given already exists in the given collection, else returns None
        """
        myclient = pymongo.MongoClient(Utils.CONNECTION_STRING)
        mydb = myclient[Utils.DATABASE_NAME]
        mycol = mydb[Utils.COLLECTION_NAME_PLACES]
        
        cur = mycol.find()
        
        return list(cur)

    def load_activities_list() -> list:
        with open(Utils.ACTIVITES_JSON_FILE_PATH, "r") as jf:
            ret = json.load(jf)
            ret = ret["activities"]
        return ret