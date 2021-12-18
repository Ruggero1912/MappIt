from faker import Faker
from datetime import datetime, date

import os
from dotenv import load_dotenv, find_dotenv

def start():
    load_dotenv(find_dotenv())
    return True

from posts.Post import Post

class YTPost(Post):

    BEGIN               = start()

    KEY_YT_CHANNEL      = os.getenv("USER_YT_CHANNEL_ID_KEY")
    KEY_ID              = os.getenv("USER_ID_KEY")

    #TODO: the constructor, that should also call the super method