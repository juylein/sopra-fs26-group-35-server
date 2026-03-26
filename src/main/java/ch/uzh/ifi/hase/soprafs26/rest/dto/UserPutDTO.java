package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class UserPutDTO {
    private String name;
    private String password;
    private String bio;
    private List<String> genres;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}
