package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.util.Date;
import java.util.List;

public class Post {

    public enum Category {URBEX, NATURALISTIC, HIKING, BIKING}
    public enum Season {WINTER, SPRING, SUMMER, AUTUMN}
    public enum Weather {SUNNY, CLOUDY, RAINY, FOGGY, SNOWY, STORMY}
    public enum Difficulty {EASY, INTERMEDIATE, HARD}

    private int id;
    private User author;
    private String title;
    private Place location;
    private Date date;
    private String description;
    private List<Category> category;
    private Season suggestedSeason;
    private Weather tripWeather;
    private Weather suggestedWeather;
    private Difficulty difficulty;
    private String videoLink;
    private int likeCounter;
    //private List<Image> images;


    public Post(int id, User u, String t, Date d, String des, Season s, Weather tw, Weather sw, Difficulty diff, String link) {
        this.id = id;
        this.author = u;
        this.title= t;
        this.date = d;
        this.description = des;
        this.suggestedSeason = s;
        this.tripWeather = tw;
        this.suggestedWeather = sw;
        this.difficulty = diff;
        this.videoLink = link;
        //this.images = ...
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User user) {
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

    public void addCategory(Category cat) {
        this.category.add(cat);
    }

    public Season getSuggestedSeason() {
        return suggestedSeason;
    }

    public void setSuggestedSeason(Season ss) {
        this.suggestedSeason = ss;
    }

    public Weather getTripWeather() {
        return tripWeather;
    }

    public void setTripWeather(Weather w) {
        this.tripWeather = w;
    }

    public Weather getSuggestedWeatherWeather() {
        return suggestedWeather;
    }

    public void setSuggestedWeather(Weather w) {
        this.suggestedWeather = w;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty diff) {
        this.difficulty = diff;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String link) {
        this.videoLink = link;
    }

    /*
    public List<Image> getImages() {
        return description;
    }

    public void setImages(List<Image> imgs) {
        this.images = imgs;
    }
    */

    @Override
    public String toString() {

        String ret =
                "User{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author.getUsername() + '\'' +
                ", date='" + date.toString() + '\'' +
                ", description='" + description + '\'' +
                '}';

        return ret;
    }
}
