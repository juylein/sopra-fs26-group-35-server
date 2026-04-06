package ch.uzh.ifi.hase.soprafs26.rest.dto;
import java.time.LocalDateTime;

public class ActivitiesGetDTO {

	private Long id;
	private String username;
	private String book;
	private String actions;
	private LocalDateTime timestamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getBook(){
		return book;
	}

	public void setBook(String book){
		this.book = book;
	}

	public String getActions(){
		return actions;
	}

	public void setActions(String actions){
		this.actions = actions;
	}

	public LocalDateTime getTimestamp(){
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp){
		this.timestamp = timestamp;
	}
}
