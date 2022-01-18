import requests

from utilities.utils import Utils

class WikiImageFactory:

    WIKI_API_URL = "https://{country}.wikipedia.org/w/api.php"
    DEFAULT_API_COUNTRY_CODE = Utils.load_config("DEFAULT_WIKIPEDIA_API_COUNTRY_CODE")

    def __get_localized_api_url(page_name):
        if(':' in page_name):
            country_code = page_name.split(':')[0]
            WIKI_API_URL = WikiImageFactory.WIKI_API_URL.format(country=country_code)
        else:
            WIKI_API_URL=WikiImageFactory.WIKI_API_URL.format(country=WikiImageFactory.DEFAULT_API_COUNTRY_CODE)
        return WIKI_API_URL
    
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
        
        WIKIPEDIA_API_URL = WikiImageFactory.__get_localized_api_url(wikipedia_page_name)

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