package me.lokesh.lamp.service.player;

/**
 * Created by lokesh.
 */
public class Track implements Comparable {
    private String url;
    private String name;

    public Track() {
    }

    public Track(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Track{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return name.compareToIgnoreCase(((Track) o).getName());
    }
}
