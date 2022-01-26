from datetime import datetime

from utilities.utils import Utils
from posts.Post import Post

class FlickrPost(Post):


    KEY_FLICKR_POST_ID      = Utils.load_config("POST_FLICKR_POST_ID")

    DICT_IGNORED_ATTRIBUTES = Post.DICT_IGNORED_ATTRIBUTES + []

    def __init__(self, author_id, place_id, author_username: str, place_name: str, flickr_post_id : str, title=None, description=None, post_date: datetime = None, exp_date=None, country_code : str = None, tags_array: list = [], activity=None, pics_array: list = [], thumbnail=None) -> None:

        setattr(self, FlickrPost.KEY_FLICKR_POST_ID, flickr_post_id)

        super().__init__(author_id,  place_id, author_username, place_name, title=title, description=description, post_date=post_date, exp_date=exp_date, country_code=country_code, tags_array=tags_array, activity=activity, pics_array=pics_array, thumbnail=thumbnail)