import overpy
import requests
import datetime
import geojson

HTTP_API_ENDPOINT = "http://overpass-api.de/api/interpreter/"

def perform_overpy_query(query):
    API = overpy.Overpass()

    result = API.query(query)

    assert isinstance(result, overpy.Result)

    return result

def perform_pyhton_requests_query(query, save_into=None):
    """
    :query the Overpass QL query to be executed
    :query type: string

    :save_into optional parameter, if set, specifies the file name in which the query result should be stored

    returns a string containing the server response
    """
    begin_time = datetime.datetime.now()
    print("going to perform the query. begin_time: " + str(begin_time))
    response = requests.get(HTTP_API_ENDPOINT, 
                        params={'data': query})
    response_time = datetime.datetime.now()
    elapsed_time = (response_time - begin_time).total_seconds()

    print("[+] query performed! end_time: " + str(response_time) + " | elapsed_time: " + str(elapsed_time))

    data = response.text
    
    if save_into:
        with open(save_into, "w", encoding="utf-8") as file:
            geojson.dump(response.json(), file) #file.write(str(response.json()))
            #file.close()

    return data

QUERY = """
[out:json];
{area}
(
  node["historic"]["historic"!~"cannon|charcoal_pile|boundaray_stone|city_gate|creamery|farm|gallows|highwater_mark|milestone|optical_telegraph|pa|railway_car|rune_stone|vehicle|wayside_cross|wayside_shrine|yes"]["name"](area.searchArea);
  way["historic"]["historic"!~"cannon|charcoal_pile|boundaray_stone|city_gate|creamery|farm|gallows|highwater_mark|milestone|optical_telegraph|pa|railway_car|rune_stone|vehicle|wayside_cross|wayside_shrine|yes"]["name"](area.searchArea);
);
out body;
>;
out skel qt;
"""

AREA_FILTER = {
    "PISA"  : "area[name=\"Pisa\"]->.searchArea;",
    "ITALY" : "area[\"ISO3166-1:alpha2\"=\"IT\"]->.searchArea;"
}

def run_query_on_place(place, save_to_file=False):
    """
    possible values for place are the keys of AREA_FILTER
    """
    if place not in AREA_FILTER.keys():
        print("place {place} not valid!".format(place=place))
        return False

    final_query = QUERY.format(area=AREA_FILTER[place])

    if save_to_file:
        save_path = "responses/{place}.osm".format(place=place)
    else:
        save_path = None

    data = perform_pyhton_requests_query(final_query, save_path)
    return data


def open_resultset(file_name):
    data = ""
    with (open(file_name, "r", encoding="utf-8") as file):
        data = file.read()

    print("geojson data len: {len}".format(len=len(data)))

    jsondata = geojson.loads(data)
    assert isinstance(jsondata, dict)

    result = overpy.Result.from_json(jsondata)
    return result

#run_query_on_place("PISA", True)

exit()

#--------------- USE CASES EXAMPLES ----------------

result = open_resultset("responses/PISA.osm")

counter = 0

for node in result.nodes:
    assert isinstance(node, overpy.Node)
    node.id
    tags = node.tags
    print(type(tags), tags)
    counter += 1
    if counter > 4:
        break

counter = 0

for way in result.ways:
    assert isinstance(way, overpy.Way)

    print(way)

    print(way.tags)
    print(way.attributes)
    print(way.center_lat, way.center_lon)
    print(way.id)

    nodes = way.get_nodes()

    print(len(nodes))

    if counter >= 5:
        exit()

    counter += 1
