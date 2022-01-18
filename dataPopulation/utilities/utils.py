from typing import Tuple
import pymongo
import json
from faker import Faker
import logging
from datetime import date, datetime

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

    def load_config_boolean(config_key : str) -> bool :
        """
        parse a boolean from the specified config_key key and returns it
        """
        value = Utils.load_config(config_key=config_key)
        if value.lower() in ["t", "true", "1", "on", "yes", "y"]:
            return True
        else:
            return False

    def load_config_integer(config_key : str) -> int :
        """
        parse an int from the specified config_key key and returns it
        """
        value = Utils.load_config(config_key=config_key)
        return int(value)

    def load_config_json(config_key : str):
        """
        trys to parse a json object from the given config key
        """
        return json.loads(Utils.load_config(config_key))

    def start_logger(logger_name : str):
        logger = logging.getLogger(logger_name)
        logger.setLevel(level=logging.DEBUG)
        ch = logging.StreamHandler()
        ch.setLevel(level=logging.DEBUG)
        formatter = logging.Formatter('%(asctime)s - %(name)s - [%(levelname)s] - %(message)s')
        ch.setFormatter(formatter)
        logger.addHandler(ch)
        return logger

    def temporary_log(text : str = "", new_line : bool = False):
        """
        - if no text is given, clear the current line
        - if new_line is True, returns to new line so that the current content of the temporary_log gets "stored"
        - the separator between a temporary_log text is \r, so that the previous log is overwritten
        """
        CLEAR_LINE = "\033[K"
        if new_line:
            print()
            return
        if text == "" or text == None:
            print(f"{CLEAR_LINE}\r", end="")
        else:
            print(f"{CLEAR_LINE}\r{text}", end="")

    def convert_date_to_datetime(date : date) -> datetime:
        """
        converts a date object to a datetime object and returns it
        """
        return datetime(date.year, date.month, date.day)

    @DeprecationWarning
    def load_places_list_from_mongo() -> list:
        """
        WARNING: THIS METHOD IS DEPRECATED!
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

    __notifications_files_counter = 0

    def say_something(text : str = "Task completed!", lang : str ="en"):
        """
        - use the parameter lang to specify a different language for the input text (default en) (for italian use it)
        """
        from gtts import gTTS
        import playsound
        tts_obj = gTTS(text=text, lang=lang)
        file_name = f"notifications{Utils.__notifications_files_counter}.mp3"
        Utils.__notifications_files_counter += 1
        tts_obj.save(file_name)        

        from threading import Thread
        complete_path = f'{os.getcwd()}\\{file_name}'
        def thread_run():
            try:
                playsound.playsound(complete_path)
            except:
                print("Sorry, I did not announce the notification...")
            exit()
        thread = Thread(target=thread_run)
        thread.start()

        return
        

