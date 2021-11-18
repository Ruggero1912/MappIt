package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.util.Date;

public class Trip {

    public enum TripTypes {URBEX, NATURALISTIC, HIKING, BIKING};
    public enum Season {WINTER, SPRING, SUMMER, AUTUMN};
    public enum Weather {SUNNY, CLOUDY, RAINY, FOGGY, SNOWY, STORMY};
    public enum Difficulty {EASY, INTERMEDIATE, HARD};

    private int id;
    public User author;
    public String title;
    public Date date;
    public String description;
    public Season suggestedSeason;
    public Weather tripWeather;
    public Weather suggestedWeather;
    public Difficulty difficulty;
    public String videoLink;
    //private List<Image> images;


    public Trip(int id, User u, String t, Date d, String des, Season s, Weather tw, Weather sw, Difficulty diff, String link) {
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

    public void setSuggestedWeatherWeather(Weather w) {
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

        String ret = "User{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author.username + '\'' +
                ", date='" + date.toString() + '\'' +
                ", description='" + description + '\'' +
                '}';

        return ret;
    }
}
