import textwrap
from threading import Thread
from textwrap import *

from YTposts.YTPostFactory import YTPostFactory
from FlickrPosts.FlickrPostFactory import FlickrPostFactory
from places.osmPlaceFactory import OsmPlaceFactory
from places.placeFactory import PlaceFactory
from utilities.utils import Utils
from utilities.persistentEntitiesManager import PersistentEntitiesManager
from users.userFactory import UserFactory

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

class CommandPrompt:
    LOGGER = Utils.start_logger("CommandPrompt")

    COMMANDS_DESCRIPTIONS = {
                                "list"              : "params: \"entity\" | \"relations\" | \"post-sources\" | places-areas",
                                "delete"            : "deletes the specified entities from the db | if 'ALL' is given: deletes all the entities",
                                "generate"          :   {
                                                            "posts"             : "generates posts from the specified sources | use the option -n (/ --num) to specify how many different places | use -a (/ --all-places) to run the generation on all the places in db | if -s (/ --skip) is given, skips all the places for which at least it has been done one search for the specified kinds of generation",
                                                            "users"             : "generates users. use the option -n (/--num) followed by the required number of users use -s (/--social-relations) if the generation is required | use -ns (/--no-social-relations) if the generation is forbidden | NOTE: it is suggested to use 'generate posts', which handles also the generation of users starting from the API obtained infos",
                                                            "social-relations"  : "generates social relations for the given kinds of relations. use the option -n (/--how-many) to specify how many different users must be used for the generations",
                                                            "places"        : "if you specify an option from [ --node-ids | --relations-ids | ways-ids ], parses all the following parameters of ids of the given kinds and tries to load the places with the given ids. else you can specify the places-areas for which you want to generate places from OpenStreetMap"
                                                        },
                            }

    POST_SOURCE_YT      = "youtube"
    POST_SOURCE_FLICKR  = "flickr"                 

    POST_SOURCES = [
        POST_SOURCE_YT,
        POST_SOURCE_FLICKR
    ]

    SOCIAL_RELATION_FOLLOW  = PersistentEntitiesManager.NEO4J_RELATION_USER_FOLLOWS_USER
    SOCIAL_RELATION_LIKE    = PersistentEntitiesManager.NEO4J_RELATION_USER_LIKES_POST
    SOCIAL_RELATION_FAV     = PersistentEntitiesManager.NEO4J_RELATION_USER_FAVOURITES_PLACE
    SOCIAL_RELATION_VISITED = PersistentEntitiesManager.NEO4J_RELATION_USER_VISITED_PLACE

    GENERABLE_SOCIAL_RELATIONS =  [
                            SOCIAL_RELATION_VISITED,
                            SOCIAL_RELATION_FAV,
                            SOCIAL_RELATION_FOLLOW,
                            SOCIAL_RELATION_LIKE
                        ]

    HOW_MANY_PLACES = 10
    HOW_MANY_USERS = 10
    HOW_MANY_USERS_FOR_SOCIAL_GENERATIONS = 10

    run = True

    def __init__(self) -> None:
        self.commands = {
                            ""                  : self.dummy,
                            "list"              : self.list,
                            "delete"            : self.delete,
                            "generate"          : self.generate,
                            "exit"              : self.stop,
                            "exit()"            : self.stop
                        }
        self.generations_commands = {
                                        "posts"             : self.generate_posts,
                                        "users"             : self.generate_users,
                                        "social-relations"  : self.generate_social_relations,
                                        "places"            : self.generate_places
                                    }

    def start(self):
        self.thread = Thread(target=self.loop)
        self.thread.start()

    def stop(self, param=[]):
        if not hasattr(self, "thread"): return
        if isinstance(self.thread, Thread):
            self.run = False
            del self.thread
            print("CommandPrompt terminated. press ENTER to quit")

    def __del__(self):
        self.stop()

    def loop(self):
        """
        this function must be associated to the Thread as target
        """
        while self.run:
            self.list_commands()
            self.prompt()
            separator()
        return

    def list_commands(self):
        print("Available commands:")
        width = 120
        for command in CommandPrompt.COMMANDS_DESCRIPTIONS:
            output = ""
            if isinstance(CommandPrompt.COMMANDS_DESCRIPTIONS[command], dict):
                for sub_command in CommandPrompt.COMMANDS_DESCRIPTIONS[command]:
                    sub_desc = CommandPrompt.COMMANDS_DESCRIPTIONS[command][sub_command]
                    prefix = f"{command} {sub_command} \t\t-> "
                    wrapper = textwrap.TextWrapper(initial_indent=prefix, width=width,  subsequent_indent=' '*len(prefix))
                    print(wrapper.fill(sub_desc))
                    #output += f"{command} {sub_command} \t-> {sub_desc}\n"
            else:
                desc = CommandPrompt.COMMANDS_DESCRIPTIONS[command]
                prefix = f"{command}\t\t\t-> "
                wrapper = textwrap.TextWrapper(initial_indent=prefix, width=width,  subsequent_indent=' '*len(prefix))
                print(wrapper.fill(desc))
                #output = f"{command}\t\t-> {desc}"
            #print(output)
        print()
        

    def prompt(self):
        print(green("aide@MappIt") +":" + red("dataPopulation") + "$ ", end="")
        command_string = input()
        #the first part of the string  is the command (using ' ' as delimiter)
        user_parameters = command_string.split(' ')
        user_command = user_parameters.pop(0)

        if user_command not in self.commands.keys():
            print("\t[!] Unrecognized command [!]")
            return

        self.commands[user_command](param = user_parameters)
        return

    
    def dummy(self, param):
        return
    
    def list(self, param):
        """
        - if no param is given or "entity", lists the available entity kinds 
        - if a param "relations" is given, lists instead the available social relations
        - if a param "post-sources" is given, returns the list of available post sources
        """
        print_entity_list = False
        if param is None or param == [] or param == "":
            print_entity_list = True
            param = []

        for option in param:
            assert isinstance(option, str)
            if option.lower() in ["entity", "entities", "entity-list"]:
                    print_entity_list = True
            if option.lower() in ["relations", "rel", "relation", "relation-list"]:
                print("Available relation kinds:")
                for relation_kind in PersistentEntitiesManager.NEO4J_RELATIONS_KINDS:
                    print(relation_kind)
            if option.lower() in ["post-sources", "posts-sources", "postsources", "postssources", "post-source", "posts", "post", "sources", "source"]:
                print("Available post generation sources:")
                for post_source in CommandPrompt.POST_SOURCES:
                    print(post_source)
            if option.lower() in ["generations", "generate", "generable"]:
                print("Available generations kinds:")
                for generable_instance in self.generations_commands:
                    print(generable_instance)
            if option.lower() in ["places-areas", "place-area", "places-area", "place-areas"]:
                print("Available places' generation areas: ")
                for area in OsmPlaceFactory.AREAS.keys():
                    print(area)

        if print_entity_list:
            print("Available entity kinds:")
            for entity_kind in PersistentEntitiesManager.ENTITY_KINDS:
                print(entity_kind)
        return

    def delete(self, param):
        """
        deletes the specified entities
        - if receives ALL, deletes all the entities in the database
        """
        assert isinstance(param, list)
        for option in param:
            assert isinstance(option, str)
            if(option.lower() == "all"):
                PersistentEntitiesManager.delete_all_entity_kinds()
                return
            if(option.lower() in ["places-duplicate-nodes"]):
                PersistentEntitiesManager.delete_places_duplicate_nodes()
                return
            if(option not in PersistentEntitiesManager.ENTITY_KINDS):
                print(f"The specified kind was not recognised: {option}")
                separator()
                self.list(None)
                continue
            PersistentEntitiesManager.delete_entity_kind(entity_kind=option)

    def generate(self, param):
        """
        handles all the available kinds of generations.
        if the kind given is not recognised, lists the available generations kinds
        """
        assert isinstance(param, list)
        user_parameters = param
        user_specified_generation = user_parameters.pop(0)

        if user_specified_generation not in self.generations_commands.keys():
            print("\t[!] Unrecognized user specified generation kind [!]")
            return

        self.generations_commands[user_specified_generation](param = user_parameters)

    def generate_places(self, param):
        """
        if you specify an option from [ --node-ids | --relations-ids | ways-ids ], parses all the following parameters of ids of the given kinds and tries to load the places with the given ids. else you can specify the places-area for which you want to generate places from OpenStreetMap
        """
        parsing_nodes = False
        parsing_ways = False
        parsing_relations = False
        nodes_ids = []
        ways_ids = []
        relations_ids = []
        places_area = ""

        for option in param:
            assert isinstance(option, str)
            if option.lower() in ["--node-ids", "--nodes-ids", "--nodes-id"]:
                parsing_nodes = True
                parsing_ways = False
                parsing_relations = False
            elif option.lower() in ["--relations-ids", "--relation-id", "relations-id", "relations-ids"]:
                parsing_nodes = False
                parsing_ways = False
                parsing_relations = True
            elif option.lower() in ["--ways-ids", "--way-id", "ways-id", "ways-ids"]:
                parsing_nodes = False
                parsing_ways = True
                parsing_relations = False
            if parsing_nodes:
                nodes_ids.append(int(option))
            elif parsing_ways:
                ways_ids.append(int(option))
            elif parsing_relations:
                relations_ids.append(int(option))
            else:
                places_area = option
                break
        if places_area != "":
            generated, skipped = OsmPlaceFactory.search_and_parse(places_area)
        else:
            generated, skipped = OsmPlaceFactory.search_and_parse_by_ids(nodes_ids, ways_ids, relations_ids)
        str_outcome = f"Place generation task completed! Generated {len(generated)} places and {len(skipped)} skipped"
        Utils.say_something(text=str_outcome)
        print()
        print(str_outcome)
        print()

    def generate_social_relations(self, param):
        """
        generates social relations of the given kind
        - as first parameter in param list should receive the kinds of required generations
        - use the option -n (/--how-many) to specify how many different users must be used for the generations (default value is CommandPrompt.HOW_MANY_USERS_FOR_SOCIAL_GENERATIONS)
        """
        generate_all = False
        generate_favs = False
        generate_follows = False
        generate_likes = False
        generate_visited = False
        how_many_users = CommandPrompt.HOW_MANY_USERS_FOR_SOCIAL_GENERATIONS

        assert isinstance(param, list)

        if(len(param) == 0): print("\t[-] Please specify the relations types or 'all' [-]")

        for index, option in enumerate(param):
            assert isinstance(option, str)
            if option.lower() in ["all", "*"]:
                generate_all = True
            elif option.lower() in ["-n", "--num", "--how-many", "-h"]:
                #the next element in the param array must be the how_many_users param
                if len(param) <= index + 1:
                    print(f"The how_many_users value must be provided after '{option}' keyword!")
                    return
                try:
                    how_many_users = int(param[index + 1])
                except:
                    print(f"The specified how_many_users value is not acceptable. Specified value: {param[index+1]}")
            elif option.lower() == CommandPrompt.SOCIAL_RELATION_FAV:
                generate_favs = True
            elif option.lower() == CommandPrompt.SOCIAL_RELATION_FOLLOW:
                generate_follows = True
            elif option.lower() == CommandPrompt.SOCIAL_RELATION_LIKE:
                generate_likes = True
            elif option.lower() == CommandPrompt.SOCIAL_RELATION_VISITED:
                generate_visited = True

        random_users = UserFactory.get_random_ids(how_many_users)
        if generate_all:
            for user_id in random_users:
                UserFactory.generate_social_relations_for_the_user(user_id=user_id)
        else:
            for user_id in random_users:
                if generate_favs:
                    UserFactory.add_to_favourites_some_places(user_id=user_id, how_many=how_many_users)
                if generate_follows:
                    UserFactory.generate_followers(user_id=user_id, how_many=how_many_users)
                if generate_likes:
                    UserFactory.like_to_some_posts(user_id=user_id, how_many=how_many_users)
                if generate_visited:
                    UserFactory.visit_some_places(user_id, how_many_users)

    def generate_users(self, param):
        """
        generates the given number of users
        - specify the required number of user using the option -n (/--num) followed by the required number of users
        - specify if is required the generation of social relations for the generated users using:
            - the parameter -s (/--social-relations) if the generation is required
            - the parameter -ns (/--no-social-relations) if the generation is forbidden
        - note: it is suggested to use generate_posts, which handles also the generation of users starting from the API obtained infos
        """
        assert isinstance(param, list)

        how_many_users = CommandPrompt.HOW_MANY_USERS
        generate_social_relations = None

        for index, option in enumerate(param):
            assert isinstance(option, str)
            if option.lower() in ["-n", "--num", "--how-many", "-h"]:
                #the next element in the param array must be the how_many_users param
                if len(param) <= index + 1:
                    print(f"The how_many_users value must be provided after '{option}' keyword!")
                    return
                try:
                    how_many_users = int(param[index + 1])
                except:
                    print(f"The specified how_many_users value is not acceptable. Specified value: {param[index+1]}")
            elif option.lower() in ["-s", "--social-relations", "--social-relation"]:
                generate_social_relations = True
            elif option.lower() in ["-ns", "--no-social-relations", "--no-social-relation"]:
                generate_social_relations = False
            
        generated_users = []

        print(f"Going to generate {how_many_users} users...")

        for i in range(0, how_many_users):
            user = UserFactory.create_user_from_scratch(generate_social_relations)
            generated_users.append(user)

        print("\nDone!")

    def generate_posts(self, param):
        """
        generates the given kind of posts (and at least on x places where x is given like this: -n x)
        - use the option -n (/ --num) to specify how many different places should be used for the post generation (defaults to CommandPrompt.HOW_MANY_PLACES)
        - use the option -a (/ --all-places) to run the post generation on all the places present in the database
        - if an option -s (/ --skip) is given, skips all the places for which at least it has been done one search for the specified kind of generation
        """
        assert isinstance(param, list)

        how_many_places = CommandPrompt.HOW_MANY_PLACES
        all_the_places = False
        skip = False
        generate_yt = False
        generate_flickr = False

        skip_next_option = False

        for index, option in enumerate(param):
            if skip_next_option:
                skip_next_option = False
                continue
            assert isinstance(option, str)
            if option.lower() in ["-n", "--num", "--how-many", "-h"]:
                #the next element in the param array must be the how_many_places param
                if len(param) <= index + 1:
                    print(f"The how_many_places value must be provided after '{option}' keyword!")
                    return
                try:
                    how_many_places = int(param[index + 1])
                    skip_next_option = True
                except:
                    print(f"The specified how_many_places value is not acceptable. Specified value: {param[index+1]}")
            elif option.lower() in ["-a", "--all-places", "--all-place"]:
                all_the_places = True
            elif option.lower() in ["-s", "--skip"]:
                skip = True
            elif option.lower() in [CommandPrompt.POST_SOURCE_YT, "you-tube", "yt"]:
                generate_yt = True
            elif option.lower() in [CommandPrompt.POST_SOURCE_FLICKR, "flick", "flikr"]:
                generate_flickr = True
            elif option.lower() not in CommandPrompt.POST_SOURCES:
                print(f"Unrecognised post source '{option}'")
                self.list(["post-sources"])

        if not generate_flickr and not generate_yt: 
            print("\t[x] Error: no valid post source specified! Skipping... [x]")
            return
        else:
            tmp = how_many_places if all_the_places is False else "all the"
            print(f"Going to generate posts for {tmp} places with option skip set to {skip}...")
            if generate_yt:
                print(" [+] Using YouTube as source [+] ")
            if generate_flickr:
                print(" [+] Using Flickr as source  [+] ")

        how_many_posts_from_yt = 0
        how_many_posts_from_flickr = 0

        if all_the_places:
            how_many_places = 0
            places = PlaceFactory.load_places(0)
            for place in places:
                assert isinstance(place, dict)
                place_id = str(place[PlaceFactory.PLACE_ID_KEY])
                place_name = place[PlaceFactory.PLACE_NAME_KEY]
                (lon, lat) = Utils.load_coordinates(place)
                if generate_yt:
                    if not skip or not place.get(PlaceFactory.PLACE_LAST_YT_SEARCH_KEY, None):
                        yt_posts = YTPostFactory.posts_in_given_place(place_name, lon, lat, place_id)
                        how_many_posts_from_yt += len(yt_posts)
                if generate_flickr:
                    if not skip or not place.get(PlaceFactory.PLACE_LAST_FLICKR_SEARCH_KEY, None):
                        flickr_posts = FlickrPostFactory.posts_in_given_place(place_name, lon, lat, place_id)
                        how_many_posts_from_flickr += len(flickr_posts)
        else:
            if generate_yt:
                yt_places = PlaceFactory.load_places_for_yt_search(how_many_places)
                for place in yt_places:
                    assert isinstance(place, dict)
                    place_id = str(place[PlaceFactory.PLACE_ID_KEY])
                    place_name = place[PlaceFactory.PLACE_NAME_KEY]
                    (lon, lat) = Utils.load_coordinates(place)
                    if not skip or not place.get(PlaceFactory.PLACE_LAST_YT_SEARCH_KEY, None):
                        yt_posts = YTPostFactory.posts_in_given_place(place_name, lon, lat, place_id)
                        how_many_posts_from_yt += len(yt_posts)
            
            if generate_flickr:
                flickr_places = PlaceFactory.load_places_for_flickr_search(how_many_places)
                for place in flickr_places:
                    assert isinstance(place, dict)
                    place_id = str(place[PlaceFactory.PLACE_ID_KEY])
                    place_name = place[PlaceFactory.PLACE_NAME_KEY]
                    (lon, lat) = Utils.load_coordinates(place)
                    if not skip or not place.get(PlaceFactory.PLACE_LAST_FLICKR_SEARCH_KEY, None):
                        flickr_posts = FlickrPostFactory.posts_in_given_place(place_name, lon, lat, place_id)
                        how_many_posts_from_flickr += len(flickr_posts)

        str_outcome = ""

        if how_many_posts_from_flickr > 0 and how_many_posts_from_yt > 0:
            str_outcome = f"Generated {how_many_posts_from_yt + how_many_posts_from_flickr} posts! {how_many_posts_from_yt} from YouTube and {how_many_posts_from_flickr} from Flickr"
        elif how_many_posts_from_flickr > 0:
            str_outcome = f"Generated {how_many_posts_from_flickr} posts using Flickr as source"
        elif how_many_posts_from_yt > 0:
            str_outcome = f"Generated {how_many_posts_from_yt} posts using YouTube as source"
        else:
            str_outcome = "Hey, no posts were generated..."
        
        Utils.say_something(text=str_outcome)
        print()
        print(str_outcome)
        print()

