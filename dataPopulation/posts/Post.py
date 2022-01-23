from faker import Faker
from datetime import datetime, date, timedelta

import os
from dotenv import load_dotenv, find_dotenv
import logging

from dateutil import parser

from utilities.utils import Utils

def start():
    load_dotenv(find_dotenv())
    return True

class Post:

    LOGGER = logging.getLogger("Post")

    LOGGER.setLevel(level=logging.DEBUG)

    BEGIN               = start()

    DEFAULT_ACTIVITY    = os.getenv("POST_GENERIC_DEFAULT_ACTIVITY")
    DEFAULT_THUMBNAIL   = os.getenv("POST_DEFAULT_THUMBNAIL")
    DEFAULT_COUNTRY_CODE= Utils.load_config("DEFAULT_COUNTRY_CODE")
    MAX_YEARS_BETWEEN_EXP_AND_POST = int( os.getenv("MAX_YEARS_BETWEEN_EXP_AND_POST") )   #MAXIMUM INTERVAL BETWEEN POST DATE AND EXPERIENCE DATE

    EMBEDDED_DOC_ATTRIBUTE_SIZE_LIMIT = Utils.load_config_integer("EMBEDDED_DOC_ATTRIBUTE_SIZE_LIMIT")

    fake                = Utils.fake

    KEY_ID              = os.getenv("POST_ID_KEY")
    KEY_AUTHOR          = os.getenv("POST_AUTHOR_KEY")
    KEY_AUTHOR_USERNAME = os.getenv("POST_AUTHOR_USERNAME_KEY")
    KEY_PLACE           = os.getenv("POST_PLACE_KEY")
    KEY_PLACE_NAME      = os.getenv("POST_PLACE_NAME_KEY")
    KEY_TITLE           = os.getenv("POST_TITLE_KEY")
    KEY_DESC            = os.getenv("POST_DESCRIPTION_KEY")
    KEY_THUMBNAIL       = os.getenv("POST_THUMBNAIL_KEY")
    KEY_PICTURES        = os.getenv("POST_PICTURES_KEY")
    KEY_POST_DATE       = os.getenv("POST_DATE_KEY")
    KEY_EXPERIENCE_DATE = os.getenv("POST_EXPERIENCE_DATE_KEY")
    KEY_ACTIVITY        = os.getenv("POST_ACTIVITY_KEY")
    KEY_TAGS            = os.getenv("POST_TAGS_KEY")
    KEY_LIKES_COUNTER   = os.getenv("POST_LIKES_COUNTER_KEY")
    KEY_COUNTRY_CODE   = os.getenv("POST_COUNTRY_CODE_KEY")

    DICT_IGNORED_ATTRIBUTES = []

    TRUNCABLE_ATTRIBUTES = [KEY_DESC, KEY_TITLE]

##########################################
########   POST PREVIEW SETUP  ###########
##########################################
    POST_PREVIEW_FIELDS = [
        KEY_ID,
        KEY_TITLE,
        KEY_AUTHOR_USERNAME,
        KEY_DESC,
        KEY_THUMBNAIL
    ]
##########################################
########   POST PREVIEW SETUP  ###########
##########################################

    def __init__(self, author_id, place_id, author_username : str, place_name : str, title=None, description=None, post_date : datetime =None, exp_date=None, country_code=DEFAULT_COUNTRY_CODE, tags_array : list =[], activity=None, pics_array : list =[], thumbnail=None) -> None:
        setattr(self, Post.KEY_TITLE            , title         if title        is not None else Post.fake.sentence(nb_words=4)        ) #short sentence as fake title
        setattr(self, Post.KEY_DESC             , description   if description  is not None else Post.fake.paragraph()                 )

        setattr(self, Post.KEY_PICTURES         , pics_array    if len(pics_array)          else []                                    )
        pics = self.get_pics_array()
        setattr(self, Post.KEY_THUMBNAIL        , thumbnail     if thumbnail    is not None else pics[0] if len(pics) else Post.DEFAULT_THUMBNAIL)
        setattr(self, Post.KEY_TAGS             , tags_array    if len(tags_array)          else []                                    )
        setattr(self, Post.KEY_ACTIVITY         , activity      if activity     is not None else self.determine_activity()             )
        setattr(self, Post.KEY_POST_DATE        , post_date     if post_date    is not None else datetime.now()                        )
        post_date = self.get_post_datetime().date()
        setattr(self, Post.KEY_EXPERIENCE_DATE  , exp_date      if exp_date     is not None else Post.fake.date_between(end_date=post_date, start_date=(post_date - timedelta(days=365*Post.MAX_YEARS_BETWEEN_EXP_AND_POST)))                      )
        setattr(self, Post.KEY_COUNTRY_CODE     , country_code  if country_code is not None else Post.DEFAULT_COUNTRY_CODE             )

        #empty params:
        setattr(self, Post.KEY_LIKES_COUNTER    ,   0)
        

        #MANDATORY PARAMETERS:
        setattr(self, Post.KEY_AUTHOR           , author_id     )
        setattr(self, Post.KEY_AUTHOR_USERNAME  , author_username)
        setattr(self, Post.KEY_PLACE            , place_id      )
        setattr(self, Post.KEY_PLACE_NAME       , place_name    )

        
    def determine_activity(self):
        """
        returns the activity that is supposed to happen in this post, based on title, description and tags
        """
        activities = Utils.load_activities_list()
        for activity in activities:
            act_name = activity['activity']
            act_tags = activity['tags']
            for act_tag in act_tags:
                assert isinstance(act_tag, str)
                if(act_tag.lower() in self.get_title().lower() or act_tag.lower() in self.get_description().lower() or act_tag.lower() in self.get_tags_array()):
                    return act_name

        return Post.DEFAULT_ACTIVITY

    def get_activity(self):
        activity_name = getattr(self, Post.KEY_ACTIVITY)
        assert isinstance(activity_name,str)
        return activity_name

    def get_title(self):
        title = getattr(self, Post.KEY_TITLE)
        assert isinstance(title, str)
        return title

    def get_description(self):
        desc = getattr(self, Post.KEY_DESC)
        assert isinstance(desc, str)
        return desc

    def get_country_code(self):
        country_code = getattr(self, Post.KEY_COUNTRY_CODE)
        assert isinstance(country_code,str)
        return country_code

    def get_tags_array(self):
        """
        return the tags array (in lower case)
        """
        tags = getattr(self, Post.KEY_TAGS)
        assert isinstance(tags, list)
        ret_tags = []
        for tag in tags:
            assert isinstance(tag, str)
            tag = tag.lower()
            ret_tags.append(tag)
        return ret_tags

    def get_pics_array(self):
        pics = getattr(self, Post.KEY_PICTURES)
        assert isinstance(pics, list)
        return pics

    def get_post_datetime(self) -> datetime:
        post_datetime = getattr(self, Post.KEY_POST_DATE)
        Post.LOGGER.debug("[Post.get_post_datetime] the type of post_datetime is: {type}".format(type=type(post_datetime)))
        if not isinstance(post_datetime, datetime):
            post_datetime = parser.parse(post_datetime)
        return post_datetime

    def get_experience_date(self) -> date:
        experience_date = getattr(self, Post.KEY_EXPERIENCE_DATE)
        if not isinstance(experience_date, date) and experience_date is not None:
            experience_date = parser.parse(experience_date).date()
        return experience_date

    def get_thumbnail(self) -> str:
        return getattr(self, Post.KEY_THUMBNAIL, None)

    def get_author(self) -> str:
        return getattr(self, Post.KEY_AUTHOR)
    
    def get_place(self) -> str:
        return getattr(self, Post.KEY_PLACE)

    def get_id(self) -> str:
        return str(getattr(self, Post.KEY_ID, ""))

    def set_id(self, id):
        """
        sets the id of self to the given one
        - converts the given id to str
        - if an id was already set for self, LOGS a warning
        """
        current_id = self.get_id()
        if(current_id != ""):
            Post.LOGGER.warning(f"received a call to the method 'set_id' on a Post that already has an attribute id! Current value: {current_id} | received value: {id}")
        setattr(self, Post.KEY_ID, str(id))

    def get_dict(self) -> dict:
        ret_dict = vars(self).copy()   #self.__dict__  #NOTE: in the commented way you have a linking between the returned dict and the class object!
        #in this way it return a copy of ret_dict, not a reference to ret_dict (which by itself references to self)

        for ignored_attribute in self.DICT_IGNORED_ATTRIBUTES:
            del ret_dict[ignored_attribute] # use ret_dict.pop(ignored_attribute, None) if u want to ignore the key not found error

        post_date = self.get_post_datetime() #ret_dict[Post.KEY_POST_DATE]
        assert isinstance(post_date, datetime)
        ret_dict[Post.KEY_POST_DATE] = post_date #.isoformat()

        exp_date = ret_dict[Post.KEY_EXPERIENCE_DATE]
        assert isinstance(exp_date, date)
        ret_dict[Post.KEY_EXPERIENCE_DATE] = Utils.convert_date_to_datetime(exp_date) #datetime(exp_date.year, exp_date.month, exp_date.day)

        return ret_dict  

    def get_post_preview_dict(self) -> dict:
        ret_dict = {}
        for key in Post.POST_PREVIEW_FIELDS:
            post_preview_attr = getattr(self, key)
            if type(post_preview_attr) == str and key in Post.TRUNCABLE_ATTRIBUTES:
                #useful to limit the size of embedded posts docs on truncable attributes
                if(len(post_preview_attr) > Post.EMBEDDED_DOC_ATTRIBUTE_SIZE_LIMIT):
                    post_preview_attr = post_preview_attr[:Post.EMBEDDED_DOC_ATTRIBUTE_SIZE_LIMIT] + "..."
            ret_dict[key] = post_preview_attr
        return ret_dict