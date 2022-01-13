from platform import node
from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)
import pymongo

from utilities.utils import Utils

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

    CONNECTION_STRING           = Utils.load_config("MONGO_CONNECTION_STRING")
    DATABASE_NAME               = Utils.load_config("MONGO_DATABASE_NAME")
    POSTS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_POSTS")
    PLACES_COLLECTION_NAME      = Utils.load_config("COLLECTION_NAME_PLACES")
    USERS_COLLECTION_NAME       = Utils.load_config("COLLECTION_NAME_USERS")
    MONGO_COLLECTIONS   =   [
                                POSTS_COLLECTION_NAME,
                                PLACES_COLLECTION_NAME,
                                USERS_COLLECTION_NAME
                            ]
    
    MONGO_DATABASE      = pymongo.MongoClient(CONNECTION_STRING)[DATABASE_NAME]

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
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

    neo_driver          = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

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