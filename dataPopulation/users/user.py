from faker import Faker
from datetime import datetime, date

import os
from dotenv import load_dotenv, find_dotenv

from utilities.utils import Utils

def start():
    load_dotenv(find_dotenv())
    return True

class User:

    BEGIN               = start()

    KEY_YT_CHANNEL      = os.getenv("USER_YT_CHANNEL_ID_KEY")
    KEY_ID              = os.getenv("USER_ID_KEY")
    KEY_USERNAME        = os.getenv("USER_USERNAME_KEY")
    KEY_NAME            = os.getenv("USER_NAME_KEY")
    KEY_SURNAME         = os.getenv("USER_SURNAME_KEY")
    KEY_MAIL            = os.getenv("USER_MAIL_KEY")
    KEY_BIRTH_DATE      = os.getenv("USER_BIRTH_DATE_KEY")
    KEY_PASSWORD        = os.getenv("USER_PASSWORD_KEY")
    KEY_ROLE            = os.getenv("USER_ROLE_KEY")
    KEY_PROFILE_PIC     = os.getenv("USER_PROFILE_PIC_KEY")
    KEY_POST_ARRAY      = os.getenv("USER_POST_ARRAY_KEY")
    KEY_FOLLOWER_COUNTER= os.getenv("USER_FOLLOWER_COUNTER")

    DEFAULT_PROFILE_PIC = os.getenv("USER_DEFAULT_PROFILE_PIC")
    DEFAULT_HASHED_PWD  = os.getenv("USER_DEFAULT_HASHED_PWD")
    DEFAULT_USER_ROLE   = os.getenv("USER_DEFAULT_USER_ROLE")

    fake                = Utils.fake

    def __init__(self, username=None, name=None, surname=None, mail=None, profile_pic=None, post_array=[]) -> None:
        """
        generates a random user object using given parameter if any, or the faker generator
        """
        #setattr(object, field_name, value)
        #sets the attribute of object of name 'field_name' to value
        setattr(self, User.KEY_USERNAME    , username if username is not None else User.fake.user_name()                )
        setattr(self, User.KEY_NAME        , name if name is not None else User.fake.first_name()                       )
        setattr(self, User.KEY_SURNAME     , surname if surname is not None else User.fake.last_name()                  )
        setattr(self, User.KEY_MAIL        , mail if mail is not None else User.fake.safe_email()                       )
        setattr(self, User.KEY_BIRTH_DATE  , User.fake.date_of_birth(minimum_age=20, maximum_age=75)                    )
        setattr(self, User.KEY_PASSWORD    , User.DEFAULT_HASHED_PWD                                                    )
        setattr(self, User.KEY_ROLE        , User.DEFAULT_USER_ROLE                                                     )
        setattr(self, User.KEY_PROFILE_PIC , profile_pic if profile_pic is not None else User.DEFAULT_PROFILE_PIC       )
        setattr(self, User.KEY_POST_ARRAY  , post_array                                                                 )

        setattr(self, User.KEY_FOLLOWER_COUNTER, 0)

    def get_dict(self) -> dict:
        ret_dict = self.__dict__
        birth_date = ret_dict[User.KEY_BIRTH_DATE]
        assert isinstance(birth_date, date)
        ret_dict[User.KEY_BIRTH_DATE] = datetime(year=birth_date.year, month=birth_date.month, day=birth_date.day) #.isoformat()
        return ret_dict

    def get_birth_date(self):
        return getattr(self, User.KEY_BIRTH_DATE)

    def set_id(self, id : str):
        if self.get_id() == None:
            setattr(self, User.KEY_ID, id)
        return self.get_id()

    def get_id(self):
        return getattr(self, User.KEY_ID, None)

    def get_username(self):
        return getattr(self, User.KEY_USERNAME, None)
        