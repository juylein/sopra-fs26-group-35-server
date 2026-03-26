package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class UserPostDTO {

	private String name;
	private String username;
    private String password;
    private String bio;
    private List<String> genres;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
