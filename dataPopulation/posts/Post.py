from faker import Faker
from datetime import datetime

import os
from dotenv import load_dotenv, find_dotenv

from utilities.utils import Utils

def start():
    load_dotenv(find_dotenv())
    return True

class Post:

    BEGIN               = start()



    fake                = Utils.fake

    KEY_ID              = os.getenv("POST_ID_KEY")
    KEY_AUTHOR          = os.getenv("POST_AUTHOR_KEY")
    KEY_TITLE           = os.getenv("POST_TITLE_KEY")
    KEY_DESC            = os.getenv("POST_DESCRIPTION_KEY")
    KEY_THUMBNAIL       = os.getenv("POST_THUMBNAIL_KEY")
    KEY_PICTURES        = os.getenv("POST_PICTURES_KEY")
    KEY_POST_DATE       = os.getenv("POST_DATE_KEY")
    KEY_EXPERIENCE_DATE = os.getenv("POST_EXPERIENCE_DATE_KEY")
    KEY_ACTIVITY        = os.getenv("POST_ACTIVITY_KEY")
    KEY_TAGS            = os.getenv("POST_TAGS_KEY")

    def __init__(self, title=None, description=None, post_date=None, exp_date=None) -> None:
        setattr(self, Post.KEY_TITLE            , title         if title        is not None else Post.fake.sentence(nb_words=4)        ) #short sentence as fake title
        setattr(self, Post.KEY_DESC             , description   if description  is not None else Post.fake.paragraph()                 )
        setattr(self, Post.KEY_POST_DATE        , post_date     if post_date    is not None else datetime.now()                        )
        setattr(self, Post.KEY_EXPERIENCE_DATE  , exp_date      if exp_date     is not None else Post.fake.date()                        )
        #TODO: complete the constructor with the other fields
