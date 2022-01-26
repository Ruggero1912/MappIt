from utilities.mongoConnectionManager import MongoConnectionManager
from posts.postFactory import PostFactory
from posts.Post import Post
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

    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")
    PLACES_COLLECTION_NAME      = Utils.load_config("COLLECTION_NAME_PLACES")

    DATABASE = MongoConnectionManager.get_database()

    neo_driver = NeoConnectionManager.get_static_obj()

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
        places_docs_updated_total_like_attr = 0

        start_time = datetime.now()

        print(f"RedundanciesUpdater.run started at: {start_time}")

        if update_followers:
            users_doc_updated   = self.__update_followers_counters()

        if update_favourites:
            places_doc_updated  = self.__update_favourites_counters()

        if update_likes:
            posts_doc_updated, places_docs_updated_total_like_attr = self.__update_likes_counters()

        end_time = datetime.now()

        elapsed_time = (end_time - start_time).total_seconds()

        print(f"{{RedundanciesUpdater::ResultSummary}}: updated {users_doc_updated} user docs, {places_doc_updated} places docs and {posts_doc_updated} posts docs | elapsed time in seconds : {elapsed_time}")

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
            user_infos = row['users']
            followers_num = row['numFollowers']
            if not isinstance(followers_num, int):
                print(f"[x] numFollowers attribute is not int! type: {type(followers_num)} | content: {followers_num}")
                exit()
            if not isinstance(user_infos, dict):
                print(f"[x] ['users'] attribute is not dict! type: {type(user_infos)} | content: {user_infos}")
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
            

            #if 'properties' not in row['places'].keys():
            #    print("the key 'properties' was not found in 'row['places'].keys()' | available keys:") 
            #    for key in row['places'].keys():
            #        print(key)
            #    exit()
            # NOTE: the row['label'] dict contains directly the attributes of the node
            place_infos = row['places']
            favourites_num = row['numFavourites']
            if not isinstance(favourites_num, int):
                print(f"[x] numFavourites attribute is not int! type: {type(favourites_num)} | content: {favourites_num}")
                exit()
            if not isinstance(place_infos, dict):
                print(f"[x] ['places'] attribute is not dict! type: {type(place_infos)} | content: {place_infos}")
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

    def __update_likes_counters(self):
        """
        returns the number of modified posts docs and of modified places docs
        - :return (modified_posts_docs, modified_places_docs)
        """
        modified_places_docs = 0
        modified_posts_docs = self.__update_likes_counters_in_post_documents()
        if modified_posts_docs > 0:
            modified_places_docs = self.__update_total_likes_counters_in_place_documents()
        print(f"'__update_likes_counters': Updated {modified_posts_docs} posts and {modified_places_docs} places documents")
        return modified_posts_docs, modified_places_docs

    def __update_likes_counters_in_post_documents(self):
        """
        updates the likes counters of all the users of the platform:
        - retrieves the number of likes of each :Post node and sets the obtained value in the Mongo Collection Post.likes
        """
        query = f"""
            MATCH (p:{NeoConnectionManager.NEO4J_POST_LABEL})
            WITH p as posts
            MATCH (posts)<-[l:{NeoConnectionManager.NEO4J_RELATION_USER_LIKES_POST}]-()
            RETURN posts, COUNT(l) AS numLikes
            """
        session = RedundanciesUpdater.neo_driver.session(default_access_mode=READ_ACCESS)
        ret = session.run(query)
        #result_summary = ret.consume()
        results = ret.data()
        session.close()
        posts_docs_updated = 0
        for row in results:
            post_infos = row['posts']
            likes_num = row['numLikes']
            if not isinstance(likes_num, int):
                print(f"[x] likes_num attribute is not int! type: {type(likes_num)} | content: {likes_num}")
                exit()
            if not isinstance(post_infos, dict):
                print(f"[x] ['posts'] attribute is not dict! type: {type(post_infos)} | content: {post_infos}")
                exit()
            post_id         = post_infos.get("id")
            #post_title     = post_infos.get("title")
            #here it should call a method that updates likesCounter of each post
            modified_counter = PostFactory.set_likes_counter(post_id, likes_num)
            if(modified_counter == 1):
                Utils.temporary_log(f"PostFactory.set_likes_counter for the post {post_id} has modified {modified_counter} rows")
            posts_docs_updated += modified_counter
        Utils.temporary_log()
        print(f"'__update_likes_counters_in_post_documents': Updated {posts_docs_updated} Post documents")
        return posts_docs_updated

    def __update_total_likes_counters_in_place_documents(self):
        """
        counts the total likes attribute of each place in the collection Place
        - it starts from the collection Post and in it groups by place_id, then project the place_id and { $sum : "$likes"} as "totalLikes"
        - for each row of the result set, updates the Place collection with the new value calculated for "totalLikes"
        :returns the number of modified Place documents 
        """
        posts_collection = RedundanciesUpdater.DATABASE.get_collection(RedundanciesUpdater.POSTS_COLLECTION_NAME)
        pipeline =  [{
                        '$group': {
                            '_id': f'${Post.KEY_PLACE}', 
                            'likes': {
                                '$sum': f'${Post.KEY_LIKES_COUNTER}'
                            }
                        }
                    }]
        cur = posts_collection.aggregate(pipeline=pipeline)
        places_docs_updated = 0
        for place in list(cur):
            place_id          = place['_id']
            place_total_likes = place['likes']
            modified_rows = PlaceFactory.set_total_likes_counter(place_id, place_total_likes)
            Utils.temporary_log(f"PlaceFactory.set_total_likes_counter for the place {place_id} has modified {modified_rows} rows")
            places_docs_updated += modified_rows
        Utils.temporary_log()
        print(f"'__update_total_likes_counters_in_place_documents': Updated {places_docs_updated} Place documents")
        return places_docs_updated