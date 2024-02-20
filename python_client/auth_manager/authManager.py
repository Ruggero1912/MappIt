import requests

from entities.user import User

class AuthManager:
    def __init__(self, session : requests.session, server_uri : str):
        self.session = session
        self.server_uri = server_uri

    def login(self, username, password) -> bool:
        """
        performs python requests login query, then returns true if loggedin
        - sets the token in the authorization header
        """
        pass

    def logout(self):
        """
        if is logged in, do log out deleting the authorization header and the current loggedin user infos
        """
        pass

    def is_logged_in(self) -> bool:
        """
        returns if the user is currently logged in or not
        """
        pass

    def get_current_user(self) -> User:
        """
        returns the currently logged in user object if exists, else returns None
        """

    def has_admin_access(self) -> bool:
        """
        returns True if the currently logged in user is admin
        """
        user = self.get_current_user()
        if isinstance(user, User):
            return user.is_admin()
        else:
            return False