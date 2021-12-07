import os

from pygeos.constructive import centroid
os.add_dll_directory(os.getcwd())

import geojson

import pymongo


#import shapely.geometry
#from shapely.geometry import shape
#from shapely.geometry import Point

POSTO = """
{
            "type": "Feature",
            "id": "way/34262137",
            "properties": {
                "building": "yes",
                "historic": "ruins",
                "historic:civilization": "medieval",
                "name": "Rocca della Verruca",
                "ruins": "castle",
                "wikidata": "Q3939451",
                "wikipedia": "it:Rocca della Verruca",
                "id": "way/34262137"
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            10.5333396,
                            43.7072802
                        ],
                        [
                            10.5333165,
                            43.7072632
                        ],
                        [
                            10.5333207,
                            43.7072421
                        ],
                        [
                            10.5334133,
                            43.7071993
                        ],
                        [
                            10.5335419,
                            43.7071541
                        ],
                        [
                            10.5335914,
                            43.7071248
                        ],
                        [
                            10.5336277,
                            43.707096
                        ],
                        [
                            10.5336503,
                            43.7070343
                        ],
                        [
                            10.5336245,
                            43.7069297
                        ],
                        [
                            10.5339046,
                            43.706992
                        ],
                        [
                            10.5341599,
                            43.7070579
                        ],
                        [
                            10.5341929,
                            43.7070441
                        ],
                        [
                            10.5342622,
                            43.7070582
                        ],
                        [
                            10.5342721,
                            43.7070961
                        ],
                        [
                            10.5342507,
                            43.7071275
                        ],
                        [
                            10.5342204,
                            43.70714
                        ],
                        [
                            10.5341894,
                            43.7071433
                        ],
                        [
                            10.5341858,
                            43.7071776
                        ],
                        [
                            10.5341937,
                            43.7072149
                        ],
                        [
                            10.5341846,
                            43.7072688
                        ],
                        [
                            10.5341279,
                            43.7073795
                        ],
                        [
                            10.5341661,
                            43.7073993
                        ],
                        [
                            10.5341728,
                            43.7074304
                        ],
                        [
                            10.5341257,
                            43.7074614
                        ],
                        [
                            10.5340565,
                            43.7074682
                        ],
                        [
                            10.534008,
                            43.7074651
                        ],
                        [
                            10.5339754,
                            43.7074374
                        ],
                        [
                            10.5337398,
                            43.7073798
                        ],
                        [
                            10.5333396,
                            43.7072802
                        ]
                    ]
                ]
            }
        }
"""

CONNECTION_STRING = ""
DATABASE_NAME = ""
COLLECTION_NAME = "places"

geojson_infos = geojson.loads(POSTO)    #use geojson.load to load from file, use .loads to load from string variable

name = geojson_infos["properties"]["name"]

"""
s = shapely.geometry.shape(geojson_infos["geometry"])
point = s.centroid
print(point)
"""

import pygeos

geom = (geojson_infos["geometry"])

print(type(geom))

print(centroid(geom))