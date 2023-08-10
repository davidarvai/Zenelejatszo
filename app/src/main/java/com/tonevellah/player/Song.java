package com.tonevellah.player;

import android.net.Uri;

public class Song {
    String title;
    Uri uri; // zene azonosito
    Uri artworkUri; // album azonosito
    int size;
    int duration;

    public Song(String title, Uri uri, Uri artworkUri, int size, int duration) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.size = size;
        this.duration = duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public int getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }
}
