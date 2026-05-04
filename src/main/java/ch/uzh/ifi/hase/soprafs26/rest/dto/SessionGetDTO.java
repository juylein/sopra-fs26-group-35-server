package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public class SessionGetDTO {
	private Long id;
	private LocalDateTime start_time;
	private LocalDateTime end_time;
    private String bookId;
    private String bookTitle;
    private String coverUrl;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getStartTime(){
		return start_time;
	}
	public void setStartTime(LocalDateTime start_time){
		this.start_time = start_time;
	}

	public LocalDateTime getEndTime(){
		return end_time;
	}
	public void setEndTime(LocalDateTime end_time){
		this.end_time = end_time;
	}

	public long getDurationSeconds() {
		if (start_time == null || end_time == null) return 0;
		return Duration.between(start_time, end_time).getSeconds();
	}

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
}
