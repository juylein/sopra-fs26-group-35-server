package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserStatsGetDTO {

	private Long id;
	private String username;
	private Long totalPoints;
	private Long booksRead;
	private Long pagesRead;
	private Long numFriends;
	private Long readingPoints;
	private Long quizPoints;

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

	public Long getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Long totalPoints) {
		this.totalPoints = totalPoints;
	}

	public Long getBooksRead() {
		return booksRead;
	}
	public void setBooksRead(Long booksRead) {
		this.booksRead = booksRead;
	}

	public Long getPagesRead() {
		return pagesRead;
	}
	public void setPagesRead(Long pagesRead) {
		this.pagesRead = pagesRead;
	}

	public Long getNumFriends(){
		return numFriends;
	}
	public void setNumFriends(Long numFriends){
		this.numFriends = numFriends;
	}

	public Long getReadingPoints() {
		return readingPoints;
	}
	public void setReadingPoints(Long readingPoints) {
		this.readingPoints = readingPoints;
	}

	public Long getQuizPoints() {
		return quizPoints;
	}
	public void setQuizPoints(Long quizPoints) {
		this.quizPoints = quizPoints;
	}
}
