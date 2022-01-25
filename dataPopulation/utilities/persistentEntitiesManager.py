from platform import node
from neo4j import (
    READ_ACCESS,
    GraphDatabase,
    WRITE_ACCESS,
)
import pymongo

from utilities.utils import Utils
from utilities.neoConnectionManager import NeoConnectionManager
from utilities.mongoConnectionManager import MongoConnectionManager
from places.placeFactory import PlaceFactory
from users.user import User

class PersistentEntitiesManager:
    LOGGER                      = Utils.start_logger("PersistentEntitiesManager")

    VERBOSE                     = Utils.load_config_boolean("VERBOSE_LOGGING")

    ENTITY_PLACES   = "Places"
    ENTITY_POSTS    = "Posts"
    ENTITY_USERS    = "Users"
    ENTITY_KINDS    =   [
                            ENTITY_PLACES,
                            ENTITY_USERS,
                            ENTITY_POSTS
                        ]

    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")
    PLACES_COLLECTION_NAME      = Utils.load_config("COLLECTION_NAME_PLACES")
    USERS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_USERS")
    MONGO_COLLECTIONS   =   [
                                POSTS_COLLECTION_NAME,
                                PLACES_COLLECTION_NAME,
                                USERS_COLLECTION_NAME
                            ]
    
    MONGO_DATABASE      = MongoConnectionManager.get_database()

    NEO4J_POST_LABEL    = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_USER_LABEL    = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL   = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_NODE_KINDS    = [
                            NEO4J_PLACE_LABEL,
                            NEO4J_POST_LABEL,
                            NEO4J_USER_LABEL
                          ]
    NEO4J_RELATION_POST_PLACE               = Utils.load_config("NEO4J_RELATION_POST_PLACE")
    NEO4J_RELATION_POST_USER                = Utils.load_config("NEO4J_RELATION_POST_USER")
    NEO4J_RELATION_USER_VISITED_PLACE       = Utils.load_config("NEO4J_RELATION_USER_VISITED_PLACE")
    NEO4J_RELATION_USER_FOLLOWS_USER        = Utils.load_config("NEO4J_RELATION_USER_FOLLOWS_USER")
    NEO4J_RELATION_USER_LIKES_POST          = Utils.load_config("NEO4J_RELATION_USER_LIKES_POST")
    NEO4J_RELATION_USER_FAVOURITES_PLACE    = Utils.load_config("NEO4J_RELATION_USER_FAVOURITES_PLACE")
    NEO4J_RELATIONS_KINDS = [
                                NEO4J_RELATION_POST_PLACE,
                                NEO4J_RELATION_POST_USER,
                                NEO4J_RELATION_USER_VISITED_PLACE,
                                NEO4J_RELATION_USER_FOLLOWS_USER,
                                NEO4J_RELATION_USER_LIKES_POST,
                                NEO4J_RELATION_USER_FAVOURITES_PLACE
                            ]

    neo_driver = NeoConnectionManager.get_static_obj() #GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    def delete_all_entity_kinds():
        """
        it erases all the databases, be careful!
        """
        if( not PersistentEntitiesManager.__ask_confirmation(kind="|||  ALL DATABASE! |||", what_obj="WHOLE DATABASE")):
            PersistentEntitiesManager.LOGGER.info("Confirmation not received. Aborting...")
            return
        for entity_kind in PersistentEntitiesManager.ENTITY_KINDS:
            PersistentEntitiesManager.delete_entity_kind(entity_kind)

        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"Deleted all the entities from both the databases")
        return

    def delete_entity_kind(entity_kind : str):
        """
        if the given entity_kind is a recognised kind of entity, deletes all the instances of the given kind from both Mongo and Neo4j
        - asks for user confirmation
        """
        if entity_kind not in PersistentEntitiesManager.ENTITY_KINDS:
            PersistentEntitiesManager.LOGGER.warning(f"The given entity kind {entity_kind} was not recognised! Skipping...")
            return

        if( not PersistentEntitiesManager.__ask_confirmation(kind=entity_kind) ):
            PersistentEntitiesManager.LOGGER.info("Confirmation not received. Aborting...")
            return

        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"going to delete all the entities for the entity {entity_kind} from both Mongo and Neo4j")

        if entity_kind == PersistentEntitiesManager.ENTITY_PLACES:
            PersistentEntitiesManager.__mongo_delete_collection(PersistentEntitiesManager.PLACES_COLLECTION_NAME)
            PersistentEntitiesManager.__neo_delete_node_kind(node_kind=PersistentEntitiesManager.NEO4J_PLACE_LABEL)
        elif entity_kind == PersistentEntitiesManager.ENTITY_POSTS:
            PersistentEntitiesManager.__mongo_delete_collection(PersistentEntitiesManager.POSTS_COLLECTION_NAME)
            PersistentEntitiesManager.__neo_delete_node_kind(node_kind=PersistentEntitiesManager.NEO4J_POST_LABEL)
        elif entity_kind == PersistentEntitiesManager.ENTITY_USERS:
            PersistentEntitiesManager.__mongo_delete_collection(PersistentEntitiesManager.USERS_COLLECTION_NAME)
            PersistentEntitiesManager.__neo_delete_node_kind(node_kind=PersistentEntitiesManager.NEO4J_USER_LABEL)
        else:
            PersistentEntitiesManager.LOGGER.warning(f"The given entity kind {entity_kind} was not recognised! Skipping...")
            return
        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"deleted all the entities of kind {entity_kind}!")


    def __neo_delete_node_kind(node_kind : str, detach : bool = True):
        """
        deletes from Neo4J all the nodes with the given Label, if it is a recognised one
        - if the :param detach is True, then it also deletes the relations that involves the deleted nodes, else the relations are kept
        """
        if node_kind not in PersistentEntitiesManager.NEO4J_NODE_KINDS:
            PersistentEntitiesManager.LOGGER.warning(f"the given node_kind {node_kind} is not in the list of the recognised node kinds. Skipping...")
            return
        if detach is True:
            detach = "DETACH"
        else:
            detach = ""
        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"going to delete all the nodes for the label {node_kind} | detach condition: {detach}")
        session = PersistentEntitiesManager.neo_driver.session(default_access_mode=WRITE_ACCESS)
        query = f""" MATCH (n:{node_kind})
                    {detach} DELETE n
                """
        ret = session.run(query) 
        result_summary = ret.consume()
        session.close()
        num_deleted_nodes = result_summary.counters.nodes_deleted
        num_deleted_relations = result_summary.counters.relationships_deleted
        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"deleted {num_deleted_nodes} nodes and {num_deleted_relations} relations")
        return result_summary

    def __mongo_delete_collection(collection_name : str):
        """
        deletes from Mongo all the Document of the given collection, if it is a recognised one
        """
        if collection_name not in PersistentEntitiesManager.MONGO_COLLECTIONS:
            PersistentEntitiesManager.LOGGER.warning(f"the given collection_name {collection_name} is not recognised. skipping...")
            return
        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"Going to drop the {collection_name} collection")

        # NOTE: if the collection that is going to be dropped is the Posts collection, we have to delete also the document linking and document embedding between:
        #       - places and posts
        #       - users and posts
        # NOTE: We have also to delete the attributes lastYTsearch and lastFlickrSearch from Place document
        if collection_name == PersistentEntitiesManager.POSTS_COLLECTION_NAME:
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.PLACES_COLLECTION_NAME, attribute=PlaceFactory.PLACE_POST_ARRAY_KEY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.PLACES_COLLECTION_NAME, attribute=PlaceFactory.PLACE_POST_ARRAY_IDS_KEY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.PLACES_COLLECTION_NAME, attribute=PlaceFactory.PLACE_LAST_YT_SEARCH_KEY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.PLACES_COLLECTION_NAME, attribute=PlaceFactory.PLACE_LAST_FLICKR_SEARCH_KEY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.PLACES_COLLECTION_NAME, attribute=PlaceFactory.PLACE_TOTAL_LIKES_COUNTER_KEY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.USERS_COLLECTION_NAME, attribute=User.KEY_POST_ARRAY)
            PersistentEntitiesManager.__drop_attribute_from_collection(collection_name=PersistentEntitiesManager.USERS_COLLECTION_NAME, attribute=User.KEY_POST_IDS_ARRAY)

        how_many_docs = PersistentEntitiesManager.MONGO_DATABASE.get_collection(collection_name).estimated_document_count()
        PersistentEntitiesManager.MONGO_DATABASE.drop_collection(collection_name)
        if PersistentEntitiesManager.VERBOSE: PersistentEntitiesManager.LOGGER.info(f"dropped the {collection_name} collection | deleted {how_many_docs}")
        return

    def __ask_confirmation(kind : str, what_obj : str = "entities"):
        """
        asks the confirmation of the user before deleting a collection / nodes set
        """
        value = input(f"Do you really want to delete all the {what_obj} of kind {kind}? (Y/N)")
        if value.lower() in ["t", "true", "1", "si", "sì", "yes", "y"]:
            return True
        else:
            return False

    def __drop_attribute_from_collection(collection_name : str, attribute : str):
        """
        drops the given :param attribute from the specified collection :param collection_name
        """
        ret = PersistentEntitiesManager.MONGO_DATABASE[collection_name].update_many(filter={}, update={"$unset" : {attribute : 1}})
        return ret.modified_count

    def drop_duplicate_places_from_neo(place_id : str, place_name : str):
        """
        drops all the places nodes in Neo4J that have the specified place_name but an id different from the specified id
        - it also deletes the relations in which the node is involved
        """
        session = PersistentEntitiesManager.neo_driver.session(default_access_mode=WRITE_ACCESS)
        ret = session.run(f"MATCH (p:{PersistentEntitiesManager.NEO4J_PLACE_LABEL} WHERE p.name=$place_name AND p.id<>$place_id) DETACH DELETE p", {"place_name":place_name, "place_id":place_id})
        result_summary = ret.consume()
        session.close()
        return result_summary.counters.nodes_deleted

    def delete_places_duplicate_nodes():
        places = PlaceFactory.load_places(0)    # loads all the places
        deleted = 0
        for place_dict in places:
            assert isinstance(place_dict, dict)
            place_name = place_dict.get(PlaceFactory.PLACE_NAME_KEY, "")
            place_id   = str( place_dict.get(PlaceFactory.PLACE_ID_KEY, "") )
            if place_id == "" or place_name == '':
                print("\t\t[!] skipping empty place obtained from mongo [!]")
                continue
            nodes_deleted = PersistentEntitiesManager.drop_duplicate_places_from_neo(place_id, place_name)
            deleted += nodes_deleted
            if nodes_deleted > 0:
                print(f"[+] Deleted {nodes_deleted} duplicate nodes for the place {place_name} (id : {place_id})")
        print(f"[+] places iteration concluded. Deleted {deleted} places in total")

    def find_spare_places_nodes():
        """
        returns a list of places that are present in Neo4j but not in MongoDB
        """
        neo_spare_nodes = []

        places_dicts = PlaceFactory.load_places(0)
        places_ids = []
        for place_dict in places_dicts:
            places_ids.append(str(place_dict["_id"]))
        session = PersistentEntitiesManager.neo_driver.session(default_access_mode=READ_ACCESS)
        ret = session.run(f"MATCH (p:{PersistentEntitiesManager.NEO4J_PLACE_LABEL}) RETURN p")
        places_nodes = ret.data()
        session.close()
        for place_row in places_nodes:
            place_node = place_row['p']
            assert isinstance(place_node, dict)
            if 'id' not in place_node:
                print("Did not found the key 'id' in the Places keys")
                print(place_node.keys())
                exit()
            neo_place_id = place_node['id']
            if neo_place_id not in places_ids:
                neo_spare_nodes.append(neo_place_id)
                print(f"[!] The Neo4j Place node '{neo_place_id}' was not found in MongoDB")
        print()
        print(f"Found {len(neo_spare_nodes)} nodes that are present in Neo4j but not in Mongo")

        print(f"the length of mongo places_ids array is {len(places_ids)} | the len of nodes array in Neo is {len(places_nodes)}")
        return neo_spare_nodes

    def find_spare_posts_nodes():
        """
        returns a list of posts that are present in Neo4j but not in MongoDB
        - also the list of ids of posts that are present in Mongo but not in Neo4j
        """
        neo_spare_nodes = []
        mongo_spare_nodes = []

        posts_dicts = PersistentEntitiesManager.MONGO_DATABASE[PersistentEntitiesManager.POSTS_COLLECTION_NAME].find()
        posts_ids = []
        for post_dict in list(posts_dicts):
            posts_ids.append(str(post_dict["_id"]))
        session = PersistentEntitiesManager.neo_driver.session(default_access_mode=READ_ACCESS)
        ret = session.run(f"MATCH (p:{PersistentEntitiesManager.NEO4J_POST_LABEL}) RETURN p")
        posts_nodes = ret.data()
        session.close()

        neo_posts_ids = []

        for post_row in posts_nodes:
            post_node = post_row['p']
            assert isinstance(post_node, dict)
            if 'id' not in post_node:
                print("Did not found the key 'id' in the Posts keys")
                print(post_node.keys())
                exit()
            neo_post_id = post_node['id']
            neo_posts_ids.append(neo_post_id)
            if neo_post_id not in posts_ids:
                neo_spare_nodes.append(neo_post_id)
                print(f"[!] The Neo4j Post node '{neo_post_id}' was not found in MongoDB")
        
        print()
        print(f"Found {len(neo_spare_nodes)} nodes that are present in Neo4j but not in Mongo")

        for mongo_post_id in posts_ids:
            if mongo_post_id not in neo_posts_ids:
                mongo_spare_nodes.append(mongo_post_id)
                print(f"[!] The Mongo Post node '{mongo_post_id}' was not found in Neo4j")

        print()
        print(f"Found {len(mongo_spare_nodes)} nodes that are present in MongoDB but not in Neo4j")

        print(f"the length of mongo posts_ids array is {len(posts_ids)} | the len of nodes array in Neo is {len(posts_nodes)}")
        
        return neo_spare_nodes, mongo_spare_nodes