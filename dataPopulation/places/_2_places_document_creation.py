import geojson
import pymongo

import shapely
from shapely import geometry

import os
from dotenv import load_dotenv

import requests

load_dotenv("../.env")   #By default loads .env configuration variables from current directory, searching in the file ".env"
#print(os.getenv("COLLECTION_NAME_PLACES"))

PLACES_SOURCE_FILE = "responses/PISA.geojson"

DEBUG = False
STORE_IN_MONGO = True
STORE_IN_NEO4J = True

CONNECTION_STRING       = os.getenv("MONGO_CONNECTION_STRING")
DATABASE_NAME           = os.getenv("MONGO_DATABASE_NAME")
COLLECTION_NAME_PLACES  = os.getenv("COLLECTION_NAME_PLACES")

def check_if_already_exists_in_mongo(document : dict, collection : str) -> str:
    """
    returns the _id if a document that is the same as the one given already exists in the given collection, else returns None
    """
    myclient = pymongo.MongoClient(CONNECTION_STRING)
    mydb = myclient[DATABASE_NAME]
    mycol = mydb[collection]
    myquery = document  #{NAME_KEY : document[NAME_KEY]}
    cur = mycol.find(myquery)
    if(len(list(cur))):
        cur.rewind()
        existing_doc = cur.next()
        return existing_doc['_id']
    else:
        return None

COLLECTION_NAME_OSM_DATA = os.getenv("COLLECTION_NAME_OSM_DATA")

def store_into_mongo(document : dict, osm_data : dict = None, collection=COLLECTION_NAME_PLACES):
    """
    returns the _id of the inserted document or False if it already exists
    """
    myclient = pymongo.MongoClient(CONNECTION_STRING)
    mydb = myclient[DATABASE_NAME]
    mycol = mydb[collection]
    existing_doc_id = check_if_already_exists_in_mongo(document, collection)
    if(existing_doc_id is not None):
        print("[!] it already exists an instance for the given doc whose name is {name}. The _id of the existing doc is {id}".format(name=document["name"], id=existing_doc_id))
        return existing_doc_id #return False
    x = mycol.insert_one(document)
    OSM_DATA_COLLECTION = mydb[COLLECTION_NAME_OSM_DATA]

    if osm_data:    OSM_DATA_COLLECTION.insert_one(osm_data)

    return x.inserted_id

from neo4j import (
    GraphDatabase,
    WRITE_ACCESS,
)

NEO4J_URI = os.getenv("NEO4J_CONNECTION_STRING")
NEO4J_DB_NAME = os.getenv("NEO4J_DATABASE_NAME")
NEO4J_DB_USER = os.getenv("NEO4J_DATABASE_USER")
NEO4J_DB_PWD = os.getenv("NEO4J_DATABASE_PWD")
NEO4J_PLACE_LABEL = os.getenv("NEO4J_PLACE_LABEL")

KEY_NAME = os.getenv("PLACE_NAME_KEY")
KEY_LOC = os.getenv("PLACE_LOC_KEY")
KEY_FITS = os.getenv("PLACE_FITS_KEY")
KEY_IMAGE= os.getenv("PLACE_IMAGE_KEY")
KEY_OSMID=os.getenv("PLACE_OSMID_KEY")
KEY_POSTS_ARRAY=os.getenv("PLACE_POST_ARRAY_KEY")
KEY_FAVOURITES_COUNTER=os.getenv("PLACE_FAVOURITES_COUNTER_KEY")
KEY_TOTAL_LIKES=os.getenv("PLACE_TOTAL_LIKES_COUNTER_KEY")

def store_into_neo4j(place_name : str, place_id : str):
    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_DB_USER, NEO4J_DB_PWD))
    session = driver.session(default_access_mode=WRITE_ACCESS)
    ret = session.run("MERGE (a:"+NEO4J_PLACE_LABEL+" {id: $id, name: $name})", {"id": str(place_id), "name": place_name})
    session.close()
    driver.close()
    result_summary = ret.consume()
    return result_summary


def load_centroid(geometry_value):  #geojson_infos["geometry"]
    """
    calculate the centroid given a polygon (/shape in general)
    it expects to receive the geojson_infos["geometry"] from the geojson object
    returns a dict in the form:
    - loc : { type: "Point", coordinates: [ -76.703347, 30.710459 ] }
    """
    s = shapely.geometry.shape(geometry_value)
    point = s.centroid
    lon = point.x
    lat = point.y
    return {"type" : "Point", "coordinates" : [lon, lat]}

def load_img_link_from_wikipedia(wikipedia_page_name : str) -> str:
    """
    given a valid Wikipedia page title, returns an image resource URI associated with that page
    NOTES:
        firstly it queries Wikipedia for images associated to a given page, then, 
        once it has the title of the image, it can retrieve it using this path:
        #https://commons.wikimedia.org/wiki/Special:FilePath/{img_name}?width=200   (width is optional)
    returns a string or None in case no image found
    """
    S = requests.Session()
    WIKIPEDIA_API_URL = "https://{country}.wikipedia.org/w/api.php"
    DEFAULT_WIKIPEDIA_API_COUNTRY_CODE = os.getenv("DEFAULT_WIKIPEDIA_API_COUNTRY_CODE")

    if(':' in wikipedia_page_name):
        country_code = wikipedia_page_name.split(':')[0]
        if(DEBUG): print("detected country_code: {cc} for the given wikipedia resource".format(cc=country_code))
        WIKIPEDIA_API_URL = WIKIPEDIA_API_URL.format(country=country_code)
    else:
        if(DEBUG): print("resource's country code not detected, using default country code '{cc}' for wikipedia API url".format(cc=DEFAULT_WIKIPEDIA_API_COUNTRY_CODE))
        WIKIPEDIA_API_URL=WIKIPEDIA_API_URL.format(country=DEFAULT_WIKIPEDIA_API_COUNTRY_CODE)

    img_link = None
    img_name = None

    PARAMS = {
        "action": "query",
        "format": "json",
        "titles": wikipedia_page_name,
        "prop": "images"
    }

    R = S.get(url=WIKIPEDIA_API_URL, params=PARAMS)
    DATA = R.json()
    PAGES = DATA['query']['pages']

    for k, v in PAGES.items():
        if('images' not in v): continue
        for img in v['images']:
            #if(DEBUG): print(img)
            img_name = img['title']
            assert isinstance(img_name, str)
            if("File:" in img_name):
                img_name = img_name.replace("File:", "")
            if(not img_name.endswith((".jpg", ".jpeg", ".png"))):
                #skip the file if it is not a valid image
                continue
            else:
                #in the case we have a valid image File name from wikipedia
                img_link = "https://commons.wikimedia.org/wiki/Special:FilePath/{img_name}".format(img_name = img_name)
                break
        if(img_link is not None):
            break
    return img_link


with open(PLACES_SOURCE_FILE, mode='r', encoding="UTF-8") as file_pointer:
    
    geoj = geojson.load(file_pointer)
    geojson_array = geoj["features"]

total_places = len(geojson_array)
print(total_places)

counter = 0
wiki_images_found = 0
sym = '\\'

for geojson_infos in geojson_array:
    name = geojson_infos["properties"]["name"]
    centroid_location_attribute = load_centroid(geojson_infos["geometry"])
    if("wikipedia" in geojson_infos["properties"]):
        img_link = load_img_link_from_wikipedia(geojson_infos["properties"]["wikipedia"])
        if(img_link is not None): wiki_images_found += 1
    #---------- Place document definition ----------------
    place_doc = {
        KEY_NAME  : name,
        KEY_LOC   : centroid_location_attribute,
        KEY_FITS  : [],
        KEY_IMAGE : img_link,
        KEY_OSMID : geojson_infos["id"],
        KEY_POSTS_ARRAY : [],
        KEY_FAVOURITES_COUNTER : 0,
        KEY_TOTAL_LIKES : 0
    }
    if(DEBUG): print(place_doc)

    if(STORE_IN_MONGO): 
        id_doc = store_into_mongo(place_doc, osm_data=geojson_infos)
        if(id_doc): print("[+] stored inside mongo the place '{name}' | given _id: {id}".format(name=name, id=id_doc))

    if (STORE_IN_NEO4J):
        neo_res_infos = store_into_neo4j(place_name=name, place_id=str(id_doc))
        if(neo_res_infos): print("[+] stored also inside neo4j the place '{name}' | given node: {node}".format(name=name, node=neo_res_infos))

    counter += 1

    sym='-' if (sym=='\\') else '\\' if (sym=='/') else '/' if (sym=='|') else '|'
    print("[{sym}] Analyzed place number {counter} out of {total_places}".format(counter=counter, total_places=total_places, sym=sym))

    #if counter > 9: break

print("[+] analyzed {c} places, found {wiki_imgs_counter} images from wikipedia".format(c=counter, wiki_imgs_counter=wiki_images_found))


