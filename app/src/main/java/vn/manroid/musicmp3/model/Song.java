package vn.manroid.musicmp3.model;

/**
 * Created by manro on 23/06/2017.
 */

public class Song {

    private String songName;
    private String songSinger;
    private String imageURL;
    private String songURL;

    public Song() {
    }

    public Song(String songName, String songSinger, String imageURL, String songURL) {
        this.songName = songName;
        this.songSinger = songSinger;
        this.imageURL = imageURL;
        this.songURL = songURL;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongSinger() {
        return songSinger;
    }

    public void setSongSinger(String songSinger) {
        this.songSinger = songSinger;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }
}
