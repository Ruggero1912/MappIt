from decimal import Decimal
from utilities.utils import Utils

class Place:

    KEY_ID                    = Utils.load_config("PLACE_ID_KEY")
    KEY_NAME                  = Utils.load_config("PLACE_NAME_KEY")
    KEY_OSM_ID                = Utils.load_config("PLACE_OSMID_KEY")
    KEY_FITS                  = Utils.load_config("PLACE_FITS_KEY")
    KEY_LOC                   = Utils.load_config("PLACE_LOC_KEY")
    KEY_IMAGE                 = Utils.load_config("PLACE_IMAGE_KEY")
    KEY_POSTS_ARRAY           = Utils.load_config("PLACE_POST_ARRAY_KEY")
    KEY_FAVOURITES_COUNTER    = Utils.load_config("PLACE_FAVOURITES_COUNTER_KEY")
    KEY_TOTAL_LIKES_COUNTER   = Utils.load_config("PLACE_TOTAL_LIKES_COUNTER_KEY")
    KEY_COUNTRY_CODE          = Utils.load_config("PLACE_COUNTRY_CODE_KEY")

    #attributes that are not parsed in the object
    KEY_LAST_YT_SEARCH        = Utils.load_config("PLACE_LAST_YT_SEARCH_KEY")
    KEY_LAST_FLICKR_SEARCH    = Utils.load_config("PLACE_LAST_FLICKR_SEARCH_KEY")

    #OLD FIELDS:
    KEY_POST_ARRAY_IDS        = Utils.load_config("PLACE_POST_IDS_ARRAY_KEY")
    

    def __init__(self, name : str, loc : dict, osm_id : str, country_code : str, fits = [], img_link :str = None, favs_counter = 0, total_likes = 0) -> None:
        setattr(self,   Place.KEY_NAME      , name      )
        setattr(self,   Place.KEY_LOC       , loc       )
        setattr(self,   Place.KEY_OSM_ID    , osm_id    )
        setattr(self,   Place.KEY_COUNTRY_CODE, country_code)

        #not mandatory attributes
        setattr(self,   Place.KEY_FITS      , fits      )
        setattr(self,   Place.KEY_IMAGE     , img_link  )
        setattr(self,   Place.KEY_TOTAL_LIKES_COUNTER, total_likes )
        setattr(self,   Place.KEY_FAVOURITES_COUNTER, favs_counter )
        return

    def parse_place(name, osm_id, lon, lat, country_code : str, img_link = None, fits = [], favs_counter = 0, total_likes = 0) -> 'Place':
        loc = Place.__parse_loc(lon, lat)
        return Place(name, loc, osm_id, country_code, fits, img_link, favs_counter, total_likes)

    def __parse_loc(lon : float, lat : float) -> dict:
        if type(lon) == Decimal:
            lon = float(lon)
            lat = float(lat)
        return {"type" : "Point", "coordinates" : [lon, lat]}

    def get_osm_id(self) -> str:
        return getattr(self, Place.KEY_OSM_ID, None)

    def get_loc(self) -> dict:
        return getattr(self, Place.KEY_LOC, None)

    def get_center(self) -> tuple:
        """
        returns a tuple lon, lat
        - lon and lat are of type float
        """
        loc = self.get_loc()
        if loc is None or not "coordinates" in loc.keys(): 
            print(f"[x] error! cannot parse coordinates from the place. place_dict: {self.get_dict()}")
            return False, False
        lon = loc["coordinates"][0]
        lat = loc["coordinates"][1]
        return lon, lat

    def set_id(self, id : str):
        setattr(self,   Place.KEY_ID, str(id))
        return self

    def get_id(self) -> str:
        return str( getattr(self, Place.KEY_ID, "") )

    def get_name(self) -> str:
        return getattr(self, Place.KEY_NAME, "")

    def get_dict(self) -> dict:
        """
        returns a copy of the attributes of self
        """
        ret_dict = self.__dict__.copy()
        return ret_dict