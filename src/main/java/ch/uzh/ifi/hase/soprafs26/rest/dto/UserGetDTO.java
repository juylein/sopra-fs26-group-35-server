package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.util.List;

public class UserGetDTO {

	private Long id;
	private String name;
	private String username;
    private String bio;
	private UserStatus status;
    private String token;
    private List<String> genres;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

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

	public UserStatus getStatus() {
		return status;
	}
	public void setStatus(UserStatus status) {
		this.status = status;
	}

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}
