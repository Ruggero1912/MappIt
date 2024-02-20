from utilities.clientUtilities import ClientUtilities

import json

class User:

    LOGGER = ClientUtilities.get_logger("UserClass")

    KEY_ID = "id"
    KEY_USERNAME = "username"
    KEY_NAME = "name"
    KEY_SURNAME = "surname"
    KEY_MAIL = "email"
    KEY_BIRTHDATE = "birthDate"
    KEY_COUNTRY_CODE = "countryCode"
    KEY_FOLLOWERS = "followersCounter"
    KEY_PROFILE_PIC = "profilePic"
    KEY_PUBLISHED_POSTS = "publishedPosts"
    KEY_USER_ROLES = "userRole"
    KEY_IS_ADMIN = "isAdmin"    #TODO: implement in User.java class

    ROLE_ADMIN = "ADMIN"
    ROLE_USER = "USER"

    MINIMUM_PASSWORD_LEN = 4

    def __init__(self, json_response : str) -> None:
        self.__dict__ = json.loads(json_response)
        
    def get_roles(self) -> list:
        roles = getattr(self, User.KEY_USER_ROLES, None)
        if roles is None:
            User.LOGGER.info(f"The roles attribute of the user obj: \t{self.get_dict()}\t is None")
        return roles

    def is_admin(self):
        if(getattr(self, User.KEY_IS_ADMIN, False) is True):
            return True
        elif(User.ROLE_ADMIN in self.get_roles()):
            return True
        else:
            return False

    def get_username(self):
        username = getattr(self, User.KEY_USERNAME, "")
        return username

    def get_dict(self):
        dict = self.__dict__.copy()
        return dict
