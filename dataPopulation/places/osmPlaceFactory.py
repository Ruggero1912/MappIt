from queue import Empty
import overpy
import requests
import datetime
import geojson

from places.placeFactory import PlaceFactory
from places.place import Place
from places.wikiImageFactory import WikiImageFactory

class OsmPlaceFactory(PlaceFactory):

    OVERPASS_HTTP_API_ENDPOINT = "http://overpass-api.de/api/interpreter/"

    PISA  = "Pisa"
    ITALY = "Italy"
    LEGHORN = "Leghorn"
    PISTOIA = "Pistoia"
    FLORENCE = "Florence"
    LUCCA = "Lucca"
    #https://gis.stackexchange.com/questions/178424/overpass-turbo-area-code-lookup/178431
    AREAS =  {
                PISA    : "area[name=\"Pisa\"]->.searchArea;",
                ITALY   : "area[\"ISO3166-1:alpha2\"=\"IT\"]->.searchArea;",
                LEGHORN : "area[name=\"Livorno\"]->.searchArea;",
                PISTOIA : "area[name=\"Pistoia\"]->.searchArea;",
                FLORENCE: "area[name=\"Firenze\"]->.searchArea;",
                LUCCA   : "area[name=\"Lucca\"]->.searchArea;",
            }
    
    AREA_CODES = {
        PISA    : "IT",
        ITALY   : "IT",
        LEGHORN : "IT",
        PISTOIA : "IT",
        FLORENCE: "IT",
        LUCCA   : "IT"
    }

    AREAS_SPECIAL_ATTRIBUTES = {
        PISA    : {"zone" : "Pisa"      },
        LEGHORN : {"zone" : "Livorno"   },
        PISTOIA : {"zone" : "Pistoia"   },
        FLORENCE: {"zone" : "Firenze"   },
        LUCCA   : {"zone" : "Lucca"     }
    }
    
    OSM_KEY_NAME = "name"
    OSM_KEY_HISTORIC = "historic"
    OSM_KEY_WIKIPEDIA = "wikipedia"

    #NOTE: use 'center meta' to receive from OSM the coordinates of the center of the ways / relations
    QUERY = """
            [out:json];
            {area}
            (
            node["historic"]["historic"!~"{excluded_historic}"]["name"](area.searchArea);
            way["historic"]["historic"!~"{excluded_historic}"]["name"](area.searchArea);
            {relations}
            );
            out body center meta;
            >;
            out skel qt;
            out center meta;
            """
    EXCLUDED_HISTORIC_VALUES = [
        'cannon', 'charcoal_pile', 'boundaray_stone', 'city_gate', 'creamery', 'farm', 'gallows', 'highwater_mark', 'milestone', 'optical_telegraph', 'pa', 'railway_car', 'rune_stone', 'vehicle', 'wayside_cross', 'wayside_shrine', 'yes'
        ]

    def __load_query_string(area : str = ITALY, include_historic_values : list = [], include_relations : bool = False):
        """
        :area the geo area in which the query should be performed
        :include_historic_values specify a list of values for the attribute historic that should be included in the result response
        :include_relations bool : if set to True, includes entities of type relation to the result set
        :returns the query string for the overpass API
        """
        if area not in OsmPlaceFactory.AREAS:
            print(f"The given area {area} is not recognised")
            return False
        area_filter = OsmPlaceFactory.AREAS[area]

        historic_filter = OsmPlaceFactory.EXCLUDED_HISTORIC_VALUES
        for include_element in include_historic_values:
            if include_element in historic_filter:
                historic_filter.remove(include_element)
        historic_filter_str ="|".join(historic_filter)
        
        relations = ""
        if include_relations is True:
            relations = f'relation["historic"]["historic"!~"{historic_filter_str}"]["name"](area.searchArea);'

        return OsmPlaceFactory.QUERY.format(area=area_filter,excluded_historic=historic_filter_str, relations=relations)

    def __load_query_by_ids(nodes_ids : list, ways_ids : list, relations_ids : list) -> str:
        basic_query =   """
                        [out:json];
                        (
                            {nodes}
                            {ways}
                            {relations}
                        );
                        out center meta;
                        """
        nodes_query = ""
        for node_id in nodes_ids:
            nodes_query += f"node({node_id});\n"
        ways_query = ""
        for way_id in ways_ids:
            ways_query += f"way({way_id});\n"
        relations_query = ""
        for relation_id in relations_ids:
            relations_query += f"relation({relation_id});\n"
        return basic_query.format(nodes=nodes_query, ways=ways_query, relations=relations_query)

    def __perform_overpy_query(query : str, first_try : bool = True):
        """
        :returns an overpy.Result object containing the response of the query
        """
        API = overpy.Overpass()
        result = None
        try:
            result = API.query(query)
        except Exception as e:
            if first_try:
                OsmPlaceFactory.__perform_overpy_query(query, first_try=False)
            else:
                print("\t\t[x][x]   OverPass not reachable    [x][x]")
                print(e)
                raise(e)
                return
        assert isinstance(result, overpy.Result)
        return result

    def __perform_python_requests_query(query : str, save_into=None):
        """
        :param query the Overpass QL query to be executed
        :param save_into if set specifies the file name in which the query result should be stored
        :returns a string containing the server response
        """
        response = requests.get(OsmPlaceFactory.OVERPASS_HTTP_API_ENDPOINT, params={'data': query})
        data = response.text
        if save_into:
            with open(save_into, "w", encoding="utf-8") as file:
                geojson.dump(response.json(), file)
        return data

    def __search(area : str, include_historic_values : list = [], include_relations : bool = False, use_overpy : bool = True):
        """
        :param use_overpy if set to False, performs a query using Python Requests and returns a string containing the server response and also stores the result to a file
        - the file relative path is 'responses/{area}.osm'
        :param area the area in which the results must be placed
        :param include_historic_values excludes from the filter historic the specified keywords
        - if :param use_overpy is set to True, :returns an overpy.Result object
        """
        query = OsmPlaceFactory.__load_query_string(area, include_historic_values, include_relations)
        if not query:
            print("Query parameters' error")
            return
        begin_time = datetime.datetime.now()
        print(f"going to perform the query. begin_time: {begin_time}")
        if use_overpy:
            ret = OsmPlaceFactory.__perform_overpy_query(query)
        else:
            save_path = f"responses/{area}.osm"
            ret = OsmPlaceFactory.__perform_python_requests_query(query, save_path)
        response_time = datetime.datetime.now()
        elapsed_time = (response_time - begin_time).total_seconds()
        print(f"{response_time} [+] query performed! | elapsed_time: {elapsed_time}")
        return ret

    def __parse_places(overpy_result : overpy.Result, area : str, TEST : bool = False):
        """
        parses all the distinct places that are not still present in the Places collection
        - it will parse all the nodes, ways and relations that have the attribute 'historic' and 'name'
        - returns two lists: (generated_places, duplicates)
        """
        if area not in OsmPlaceFactory.AREAS:
            print(f"[!] The given area {area} is not recognised")
            return False

        country_code = OsmPlaceFactory.AREA_CODES[area]

        generated_places = []

        duplicates = []

        nameless = 0
        
        for filter_cls in [overpy.Node, overpy.Way, overpy.Relation]:
            for element in overpy_result.get_elements(filter_cls):
                osm_id = ""
                center_lat = None
                center_lon = None
                elem_attributes = None
                elem_tags = None
                elem_name = None
                if isinstance(element, overpy.Node):
                    osm_id = f"node/{element.id}"
                    center_lat = element.lat
                    center_lon = element.lon
                    elem_attributes = element.attributes
                    elem_tags = element.tags
                elif isinstance(element, overpy.Way):
                    osm_id = f"way/{element.id}"
                    center_lat = element.center_lat
                    center_lon = element.center_lon
                    elem_attributes = element.attributes
                    elem_tags = element.tags
                elif isinstance(element, overpy.Relation):
                    osm_id = f"relation/{element.id}"
                    center_lat = element.center_lat
                    center_lon = element.center_lon
                    elem_attributes = element.attributes
                    elem_tags = element.tags
                else:
                    print("[x] unrecognised overpass result type")
                    continue
                elem_name = elem_tags.get(OsmPlaceFactory.OSM_KEY_NAME, None)
                elem_historic = elem_tags.get(OsmPlaceFactory.OSM_KEY_HISTORIC, False)
                elem_wikipedia = elem_tags.get(OsmPlaceFactory.OSM_KEY_WIKIPEDIA, None)
                if elem_name is None:
                    #print(f"[x] osm POI id {osm_id}: error name not found | tags: {elem_tags}")
                    nameless += 1
                    continue
                if elem_historic is False:
                    print(f"[x] osm POI id {osm_id}: Required property '{OsmPlaceFactory.OSM_KEY_HISTORIC}' not found")
                    continue
                if elem_wikipedia is not None:
                    img_link = WikiImageFactory.load_img_link_from_wikipedia(elem_wikipedia)
                else:
                    img_link = None
                parsed_place = Place.parse_place(elem_name, osm_id, center_lon, center_lat, img_link=img_link, country_code=country_code)

                if not PlaceFactory.is_place_already_present(parsed_place):
                    if TEST:
                        print("The parsed place is not present yet. Place infos:")
                        print(parsed_place.get_dict())
                        generated_places.append(parsed_place)
                        continue
                    parsed_place = PlaceFactory.store_place(parsed_place)
                    #note that now parsed_place has also the id set
                    generated_places.append(parsed_place)
                else:
                    print(f"The place '{parsed_place.get_name()}' is already present. Skipping...")
                    duplicates.append(parsed_place)

        print(f"Parsed {len(generated_places)} new places! Skipped {len(duplicates)} duplicates")
        return generated_places, duplicates

    def search_and_parse(area : str, include_historic_values : list = [], include_osm_relations : bool = False, test : bool = False) -> tuple:
        """
        :param area the area in which the results must be placed
        :param include_historic_values excludes from the filter historic the specified keywords
        :param include_osm_relations if set to True, includes in the results also the OSM entities of type relation
        :returns two lists: generated_places, duplicates
        """
        if area not in OsmPlaceFactory.AREAS:
            print(f"[!] The given area {area} is not recognised")
            return [], []
        overpy_result = OsmPlaceFactory.__search(area, include_historic_values, include_osm_relations)
        generated_places, duplicates = OsmPlaceFactory.__parse_places(overpy_result, area, test)
        return generated_places, duplicates

    def search_and_parse_by_ids(nodes_ids : list = [], ways_ids : list = [], relations_ids : list = [], area : str = ITALY, test : bool = False) -> tuple:
        """
        receives the ids of the entity to search in osm
        - parses the correspondent Place objects and stores them in db
        :param test if set to True returns the two lists but does not stores them
        :returns two lists: generated_places, duplicates
        """
        if nodes_ids == [] and ways_ids == [] and relations_ids == []:
            print("Please specify some ids")
            return [], []
        query = OsmPlaceFactory.__load_query_by_ids(nodes_ids, ways_ids, relations_ids)
        #print("Generated Overpass Query: ", query)
        begin_time = datetime.datetime.now()
        print(f"going to perform the query. begin_time: {begin_time}")
        overpy_result = OsmPlaceFactory.__perform_overpy_query(query)

        response_time = datetime.datetime.now()
        elapsed_time = (response_time - begin_time).total_seconds()
        print(f"{response_time} [+] query performed! | elapsed_time: {elapsed_time}")

        generated_places, duplicates = OsmPlaceFactory.__parse_places(overpy_result, area, test)
        return generated_places, duplicates
        