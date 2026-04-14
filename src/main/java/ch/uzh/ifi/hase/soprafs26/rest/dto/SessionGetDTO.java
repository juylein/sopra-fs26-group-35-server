package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class SessionGetDTO {
    private Long id;
    private Long hostId;
    private String bookId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
