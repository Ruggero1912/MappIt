from faker import Faker
from datetime import datetime, date

import os
from dotenv import load_dotenv, find_dotenv

def start():
    load_dotenv(find_dotenv())
    return True

from posts.Post import Post

class YTPost(Post):

    BEGIN               = start()

    KEY_YT_CHANNEL      = os.getenv("YTPOST_USER_YT_CHANNEL_ID_KEY")
    KEY_YT_VIDEO_ID     = os.getenv("YTPOST_YT_VIDEO_ID")

    DICT_IGNORED_ATTRIBUTES = Post.DICT_IGNORED_ATTRIBUTES + [KEY_YT_CHANNEL]  #we won't store in mongo the channelID, it is redundant

    def __init__(self, author_id, yt_video_id : str, yt_channel_id, title=None, description=None, post_date: datetime = None, exp_date=None, tags_array: list = [], activity=None, pics_array: list = [], thumbnail=None) -> None:
        setattr(self, YTPost.KEY_YT_VIDEO_ID, yt_video_id  )
        setattr(self, YTPost.KEY_YT_CHANNEL , yt_channel_id)
        super().__init__(author_id, title=title, description=description, post_date=post_date, exp_date=exp_date, tags_array=tags_array, activity=activity, pics_array=pics_array, thumbnail=thumbnail)
