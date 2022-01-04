from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

from utilities.utils import Utils

class PostFactory:

    LOGGER                          = Utils.start_logger("PostFactory")

    NEO4J_URI           = Utils.load_config("NEO4J_CONNECTION_STRING")
    NEO4J_DB_NAME       = Utils.load_config("NEO4J_DATABASE_NAME")
    NEO4J_DB_USER       = Utils.load_config("NEO4J_DATABASE_USER")
    NEO4J_DB_PWD        = Utils.load_config("NEO4J_DATABASE_PWD")
    NEO4J_POST_LABEL    = Utils.load_config("NEO4J_POST_LABEL")
    NEO4J_USER_LABEL    = Utils.load_config("NEO4J_USER_LABEL")
    NEO4J_PLACE_LABEL    = Utils.load_config("NEO4J_PLACE_LABEL")
    NEO4J_RELATION_POST_PLACE = Utils.load_config("NEO4J_RELATION_POST_PLACE")
    NEO4J_RELATION_POST_USER = Utils.load_config("NEO4J_RELATION_POST_USER")

    neo_driver          = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))

    def get_random_ids(how_many : int = 10) -> list :
        """
        returns a list of random post_ids
        - returns list<str>
        """
        list_of_ids = []

        session = PostFactory.neo_driver.session(default_access_mode=WRITE_ACCESS)
        
        query = """
                MATCH (p:{post_label})
                RETURN p, rand() as r
                ORDER BY r
                LIMIT {how_many} 
                """.format(post_label=PostFactory.NEO4J_POST_LABEL, how_many=how_many)
        ret = session.run(query)
        result_set = ret.data()
        session.close()
        if len(result_set) == 0:
            PostFactory.LOGGER.warning("[!] empty result set for get_random_ids! specified how_many value: {how_many}".format(how_many=how_many))
            return []

        for element in result_set:
            list_of_ids.append(element['p']['id'])
        return list_of_ids