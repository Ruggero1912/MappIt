package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import com.mongodb.BasicDBList;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
We decided to use abstract class instead of interface as the first let us define a common constructor
and also some shared abstract concrete method.
*/

public abstract class Post {

        public enum Category {URBEX, NATURALISTIC, HIKING, BIKING}
        public enum Season {WINTER, SPRING, SUMMER, AUTUMN}
        public enum Weather {SUNNY, CLOUDY, RAINY, FOGGY, SNOWY, STORMY}
        public enum Difficulty {EASY, INTERMEDIATE, HARD}
        public enum Activity {GENERIC, STREET_PHOTOGRAPHY, PORTRAIT_PHOTOGRAPHY, SPORT_PHOTOGRAPHY, LANDSCAPE_PHOTOGRAPHY,
            EVENT_PHOTOGRAPHY, WILDLIFE_PHOTOGRAPHY, AERIAL_PHOTOGRAPHY, ASTRO_PHOTOGRAPHY, MACRO_PHOTOGRAPHY, SKIING, SURFING,
            TREKKING, CLIMBING, PARAGLIDING, CANOEING, CYCLING, RUNNING, TRIATHLON}

        protected String _id;
        protected String author; //need to change the Post collection key names
        protected String authorID; //need to change the Post collection key names
        protected String title;
        protected Place location;
        protected Date date;
        protected Date postDate;
        protected String description;
        protected List<Category> category; //to maintain?
        protected Activity activity;
        protected Season suggestedSeason;
        protected Weather tripWeather;
        protected Weather suggestedWeather;
        protected Difficulty difficulty;
        protected List<String> tags;
        protected int likeCounter;


        public Post(String id, String author, String authorID, String t, Date d, String des, Season s, Weather tw, Weather sw, Difficulty diff, List<String> tags) {
            this._id = id;
            this.author = author;
            this.authorID = authorID;
            this.title= t;
            this.date = d;
            this.postDate = new Date();
            this.description = des;
            this.suggestedSeason = s;
            this.tripWeather = tw;
            this.suggestedWeather = sw;
            this.difficulty = diff;
            this.activity = Activity.GENERIC;
            if(tags!=null)
                this.tags = tags;
            else
                this.tags = new ArrayList<String>();
        }

    public Post(Document doc){
        this._id = doc.get("_id").toString();
        this.title = doc.get("title").toString();
        this.description = doc.get("desc").toString();
        this.author = doc.get("author").toString(); //key to add in mongo collection
        this.tags = Arrays.asList(doc.get("tags").toString());
        this.activity = Post.Activity.valueOf(doc.get("activity").toString());
        this.postDate = (Date) doc.get("postDate");
        this.date = (Date) doc.get("date");
    }

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            this._id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String user) {
            this.author = user;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String t) {
            this.title = t;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date d) {
            this.date = d;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String des) {
            this.description = des;
        }

        public List<Category> getCategory() {
            return category;
        }

        public void addCategory(Post.Category cat) {
            this.category.add(cat);
        }

        public Activity getActivity() {
        return this.activity;
    }

        public void setActivity(Activity act) {
        this.activity = act;
    }

        public Season getSuggestedSeason() {
            return suggestedSeason;
        }

        public void setSuggestedSeason(Post.Season ss) {
            this.suggestedSeason = ss;
        }

        public Weather getTripWeather() {
            return tripWeather;
        }

        public void setTripWeather(Post.Weather w) {
            this.tripWeather = w;
        }

        public Weather getSuggestedWeatherWeather() {
            return suggestedWeather;
        }

        public void setSuggestedWeather(Post.Weather w) {
            this.suggestedWeather = w;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(Post.Difficulty diff) {
            this.difficulty = diff;
        }

        public List<String> getTags() {
            return this.tags;
        }

        public void addTags(String t) {
            this.tags.add(t);
        }
}
