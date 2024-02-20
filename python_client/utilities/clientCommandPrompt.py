from utilities.clientInterface import ClientInterface

def separator():
    print()
    print("-" * 120)
    print()

WHITE_COLOR = "\033[97m"
DEFAULT_COLOR = WHITE_COLOR
RED_COLOR = "\033[91m"
GREEN_COLOR = "\033[92m"

def green(string):
    return GREEN_COLOR + string + DEFAULT_COLOR

def red(string):
    return RED_COLOR + string + DEFAULT_COLOR


from threading import Thread

class ClientCommandPrompt(ClientInterface):
    def __init__(self, server_uri: str) -> None:
        super().__init__(server_uri)

    def start(self):
        self.thread = Thread(target=self.__loop)
        self.thread.start()

    def stop(self, param=[]):
        if not hasattr(self, "thread"): return
        if isinstance(self.thread, Thread):
            self.run = False
            del self.thread
            print("CommandPrompt terminated. press ENTER to quit")

    def __del__(self):
        self.stop()

    def __loop(self):
        """
        this function must be associated to the Thread as target
        """
        while self.run:
            self.__list_commands()
            self.__prompt()
            separator()
        return

    def __list_commands(self):
        if self.__is_logged_in():
            user = self.__get_current_user()
            print(f"""
            Hi {user.get_username()}, what do you want to do?
            
            Commands available for logged in users:

            browse posts --suggested      
            browse-posts --liked <user_id> --> if no user_id specified, shows yours likes
            browse-posts --most-popular
            browse-posts --published <user_id>  --> if no user_id is specified, shows your posts
            browse-posts --title <title>

            find-post <id>
            like-post <id>
            unlike-post <id>
            create-post <place_id>
            delete-post <id>    --> you can only delete posts that you have published

            browse-places-nearby lon lat --radius X ---> specify lon lat in decimal notation, radius in km
            browse-places --visited
            browse-places --favourites  --> returns your favourites places
            browse-places --suggested
            browse-places --most-popular
            browse-places --name <name>

            find-place <place_id>
            add-fav-place <place_id>
            rem-fav-place <place_id>
            visit-place <place_id>

            browse-activities
            """)
            if self.__has_admin_access():
                print("""
                ++++++++++++++++++++++
                Admin methods:

                delete-post <post_id> ---> you can delete a post of other users
                delete-posts <user_id> ---> you can delete all the posts of a given user
                delete-user <user_id> ---> you can delete a user and all of its posts
                browse-most-active-users
                browse-posts-categories-per-year
                ++++++++++++++++++++++
                """)
        else:
            print("""
            Available commands for anonymous user:
            login -u <username> -p <password>   ---> login with the given credentials
            register                            ---> prompts for user registration infos
            """)
        

    def __prompt(self):
        if self.__is_logged_in():
            username = self.__get_current_user().get_username()
        else:
            username = "anon"
        
        print(green(f"{username}@MappIt") +":" + red("client") + "$ ", end="")
        command_string = input()
        #the first part of the string  is the command (using ' ' as delimiter)
        user_parameters = command_string.split(' ')
        user_command = user_parameters.pop(0)
        #TODO: add commands.keys() + methods
        if user_command not in self.commands.keys():
            print("\t[!] Unrecognized command [!]")
            return

        self.commands[user_command](param = user_parameters)
        return