from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
    Neo4jDriver
)

from utilities.utils import Utils

class NeoConnectionManager:

    NEO4J_URI                   = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME               = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER               = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD                = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_USER_LABEL            = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL           = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_POST_LABEL            = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_RELATION_USER_VISITED_PLACE = Utils.load_config("NEO4J_RELATION_USER_VISITED_PLACE")
    NEO4J_RELATION_USER_FOLLOWS_USER = Utils.load_config("NEO4J_RELATION_USER_FOLLOWS_USER")
    NEO4J_RELATION_USER_LIKES_POST = Utils.load_config("NEO4J_RELATION_USER_LIKES_POST")
    NEO4J_RELATION_USER_FAVOURITES_PLACE = Utils.load_config("NEO4J_RELATION_USER_FAVOURITES_PLACE")

    def __init__(self) -> None:
        self.neo_driver = GraphDatabase.driver(NeoConnectionManager.NEO4J_URI, auth=(NeoConnectionManager.NEO4J_DB_USER, NeoConnectionManager.NEO4J_DB_PWD))

    def get_driver(self) -> Neo4jDriver:
        if self:
            return self.neo_driver

    def set_static_obj(obj : 'NeoConnectionManager') -> bool :
        if getattr(NeoConnectionManager, 'obj', None) is not None:
            #static_obj already set
            return False
        else:
            NeoConnectionManager.obj = obj
            return True

    def get_static_obj() -> 'NeoConnectionManager':
        return getattr(NeoConnectionManager, 'obj', None)

    def get_static_driver() -> Neo4jDriver:
        obj = NeoConnectionManager.get_static_obj()
        if isinstance(obj, NeoConnectionManager):
            return obj.neo_driver

    def __del__(self) -> None:
        if hasattr(self, 'neo_driver') and isinstance(self.neo_driver, Neo4jDriver):
            print("[+] closing neo_driver connection...")
            self.neo_driver.close()

    def close_static_neo_driver():
        neo_driver = NeoConnectionManager.get_static_driver()
        if isinstance(neo_driver, Neo4jDriver):
            neo_driver.close()

neo_connection_obj = NeoConnectionManager()
NeoConnectionManager.set_static_obj(neo_connection_obj)