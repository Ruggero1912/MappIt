from places.placeFactory import PlaceFactory
from users.userFactory import UserFactory
from utilities.utils import Utils
from utilities.neoConnectionManager import NeoConnectionManager

from neo4j import (
    #GraphDatabase,
    #WRITE_ACCESS,
    READ_ACCESS
)

from datetime import datetime

class RedundanciesUpdater:

    LOGGER  = Utils.start_logger("RedundaciesUpdater")

    DELETED_USERS_MINIMUM_THRESHOLD = Utils.load_config_integer("DELETED_USERS_MINIMUM_THRESHOLD")

    neo_driver = NeoConnectionManager.get_static_driver()

    def run(self, update_followers = True, update_favourites = True, update_likes = True):
        """
        - returns True if the procedure is run, False if it is not (because of running conditions to prevent unuseful updates)
        """
        how_many_deleted = self.get_how_many_users_deleted_since_last_run()
        if how_many_deleted < RedundanciesUpdater.DELETED_USERS_MINIMUM_THRESHOLD:
            RedundanciesUpdater.LOGGER.info(f"RedundanciesUpdater: skipping redundancy update as the number of deleted user ({how_many_deleted}) is lower than the threshold ({RedundanciesUpdater.DELETED_USERS_MINIMUM_THRESHOLD})")
            return False
        #METHODS calls
        users_doc_updated   = 0
        places_doc_updated  = 0
        posts_doc_updated   = 0

        if update_followers:
            users_doc_updated   = self.__update_followers_counters()

        if update_favourites:
            places_doc_updated  = self.__update_favourites_counters()

        if update_likes:
            posts_doc_updated   = -1    #TODO: update_likes_counter

        print(f"{{RedundanciesUpdater::ResultSummary}}: updated {users_doc_updated} user docs, {places_doc_updated} places docs, {posts_doc_updated}")

        self.__update_last_run()
        return True

    def get_last_run(self):
        """
        loads the last run timestamp from persistence store
        """
        value = datetime.now()
        print(f"[!] 'get_last_run' dummy return {value}")
        return value

    def get_how_many_users_deleted_since_last_run(self) -> int:
        """
        returns the number of user deleted since the last run
        """
        value = 15
        print(f"[!] 'users_deleted_since_last_run' dummy return {value}")
        return value

    def __update_last_run(self):
        """
        - updates the last run timestamp in persistence store
        - resets the how_many_users_deleted counter
        """
        print(f"[!] '__update_last_run' not implemented")
        pass

    def __update_followers_counters(self):
        """
        updates the follower counters of all the users of the platform:
        - retrieves the number of followers of each :User node and sets the obtained value in the Mongo Collection User.followers
        """
        query = f"""
            MATCH (u:{NeoConnectionManager.NEO4J_USER_LABEL})
            WITH u as users
            MATCH (users)<-[f:{NeoConnectionManager.NEO4J_RELATION_USER_FOLLOWS_USER}]-()
            RETURN users, COUNT(f) AS numFollowers
            """
        session = RedundanciesUpdater.neo_driver.session(default_access_mode=READ_ACCESS)
        ret = session.run(query)
        #result_summary = ret.consume()
        results = ret.data()
        session.close()
        users_docs_updated = 0
        for row in results:
            user_infos = row['users']['properties']
            followers_num = row['numFollowers']
            if not isinstance(followers_num, int):
                print(f"[x] numFollowers attribute is not int! type: {type(followers_num)} | content: {followers_num}")
                exit()
            if not isinstance(user_infos, dict):
                print(f"[x] ['users']['properties'] attribute is not dict! type: {type(user_infos)} | content: {user_infos}")
                exit()
            user_id         = user_infos.get("id")
            user_username   = user_infos.get("username")
            #here it should call a method that updates followerCounter of each user
            modified_counter = UserFactory.set_follower_counter(user_id, followers_num)
            if(modified_counter == 1):
                Utils.temporary_log(f"UserFactory.set_follower_counter for the user {user_id} has modified {modified_counter} rows")
            users_docs_updated += modified_counter
        Utils.temporary_log()
        print(f"'__update_followers_counters': Updated {users_docs_updated} user documents")
        return users_docs_updated

    def __update_favourites_counters(self):
        """
        updates the favourites counters of all the places of the platform:
        - retrieves the number of favourites of each :Place node and sets the obtained value in the Mongo Collection Place.favs
        """
        query = f"""
            MATCH (p:{NeoConnectionManager.NEO4J_PLACE_LABEL})
            WITH p as places
            MATCH (places)<-[f:{NeoConnectionManager.NEO4J_RELATION_USER_FAVOURITES_PLACE}]-()
            RETURN places, COUNT(f) AS numFavourites
            """
        session = RedundanciesUpdater.neo_driver.session(default_access_mode=READ_ACCESS)
        ret = session.run(query)
        #result_summary = ret.consume()
        results = ret.data()
        session.close()
        places_docs_updated = 0
        for row in results:
            place_infos = row['places']['properties']
            favourites_num = row['numFavourites']
            if not isinstance(favourites_num, int):
                print(f"[x] numFavourites attribute is not int! type: {type(favourites_num)} | content: {favourites_num}")
                exit()
            if not isinstance(place_infos, dict):
                print(f"[x] ['places']['properties'] attribute is not dict! type: {type(place_infos)} | content: {place_infos}")
                exit()
            place_id         = place_infos.get("id")
            place_name       = place_infos.get("name")
            #here it should call a method that updates favouritesCounter of each Place
            modified_counter = PlaceFactory.set_favourites_counter(place_id, favourites_num)
            if(modified_counter == 1):
                Utils.temporary_log(f"PlaceFactory.set_favourites_counter for the place {place_id} has modified {modified_counter} rows")
            places_docs_updated += modified_counter
        Utils.temporary_log()
        print(f"'__update_favourites_counters': Updated {places_docs_updated} places documents")
        return places_docs_updated