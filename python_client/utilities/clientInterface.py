from auth_manager.authManager import AuthManager
from utilities.clientUtilities import ClientUtilities
from entities.user import User

import requests

class ClientInterface:

    LOGGER = ClientUtilities.get_logger("ClientInterface")

    def __init__(self, server_uri : str) -> None:
        self.server_uri = server_uri
        self.session = requests.session()
        self.auth_manager = AuthManager(self.__get_session(), self.__get_server_uri())
    
    def __get_session(self):
        sess = self.session
        if not isinstance(sess, requests.Session):
            ClientInterface.LOGGER.info(f"the self.session is not instance of requests.Session! | type: {type(sess)}")
            return None
        else:
            return sess

    def __get_server_uri(self):
        uri = self.server_uri
        if not isinstance(uri, str):
            ClientInterface.LOGGER.error(f"The current server uri is not a str! | type: {type(uri)}")
            return False
        else:
            return uri

    def __is_logged_in(self) -> bool:
        return self.auth_manager.is_logged_in()

    def __get_current_user(self) -> User:
        """
        - returns User obj of the currently loggedin user if exists
        - else return None
        """
        return self.auth_manager.get_current_user()

    def __has_admin_access(self) -> bool:
        """
        return True if the currently logged in user has admin access
        """
        return self.auth_manager.has_admin_access()

    def __login(self, username : str, password : str) -> bool:
        """
        returns True if the client logins, else False
        """
        if(username == "" or username == None):
            print(f"[x] empty username given")
            return False
        if(password == "" or password == None):
            print("[x] empty password given!")
            return False
        if(len(password) < User.MINIMUM_PASSWORD_LEN):
            print(f"[x] password given too short! Minimum length : {User.MINIMUM_PASSWORD_LEN} {len(password)}")
            return False
        outcome = self.auth_manager.login(username, password)
        if outcome is True:
            current_user = self.__get_current_user()
            if type(current_user) == User:
                print(f"[+] logged in as {current_user.get_username()}")
                if self.__has_admin_access():
                    print(f"[+] You are an ADMIN [+]")
                return True
            else:
                ClientInterface.LOGGER.warning(f"The current_user is of type {type(current_user)}!")
        else:
            print("[x] wrong credentials [x]")
        return False

