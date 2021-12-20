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

    DEFAULT_ACTIVITY    = os.getenv("POST_GENERIC_DEFAULT_ACTIVITY")
    DEFAULT_THUMBNAIL   = os.getenv("POST_DEFAULT_THUMBNAIL")

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

    def __init__(self, author_id, title=None, description=None, post_date : datetime =None, exp_date=None, tags_array : list =[], activity=None, pics_array : list =[], thumbnail=None) -> None:
        setattr(self, Post.KEY_TITLE            , title         if title        is not None else Post.fake.sentence(nb_words=4)        ) #short sentence as fake title
        setattr(self, Post.KEY_DESC             , description   if description  is not None else Post.fake.paragraph()                 )

        setattr(self, Post.KEY_PICTURES         , pics_array    if len(pics_array)          else []                                    )
        pics = self.get_pics_array()
        setattr(self, Post.KEY_THUMBNAIL        , thumbnail     if thumbnail    is not None else pics[0] if len(pics) else Post.DEFAULT_THUMBNAIL)                                    )
        setattr(self, Post.KEY_TAGS             , tags_array    if len(tags_array)          else []                                    )
        setattr(self, Post.KEY_ACTIVITY         , activity      if activity     is not None else self.determine_activity()             )
        setattr(self, Post.KEY_POST_DATE        , post_date     if post_date    is not None else datetime.now()                        )
        post_date = self.get_post_datetime().date()
        setattr(self, Post.KEY_EXPERIENCE_DATE  , exp_date      if exp_date     is not None else Post.fake.date_between(end_date=post_date, start_date='-3y')                      )

        #MANDATORY PARAMETERS:
        setattr(self, Post.KEY_AUTHOR           , author_id     )

        
    def determine_activity(self):
        """
        returns the activity that is supposed to happen in this post, based on title, description and tags
        """
        #TODO: method to parse the activity from text (a call to a method or here inline)
        return Post.DEFAULT_ACTIVITY

    def get_pics_array(self):
        pics = getattr(self, Post.KEY_PICTURES)
        assert isinstance(pics, list)
        return pics

    def get_post_datetime(self) -> datetime:
        post_datetime = getattr(self, Post.KEY_POST_DATE)
        assert isinstance(post_datetime, datetime)
        return post_datetime