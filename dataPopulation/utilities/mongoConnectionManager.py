import pymongo

from utilities.utils import Utils

class MongoConnectionManager:
    LOGGER                          = Utils.start_logger("MongoConnectionManager")

    CONNECTION_STRING           = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = Utils.load_config("MONGO_DATABASE_NAME")

    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")
    
    __mongo_client = pymongo.MongoClient(CONNECTION_STRING)
    __mongo_database = __mongo_client[DATABASE_NAME]

    def get_collection(collection_name : str):
        return MongoConnectionManager.__mongo_database.get_collection(collection_name)

    def get_database():
        return MongoConnectionManager.__mongo_database