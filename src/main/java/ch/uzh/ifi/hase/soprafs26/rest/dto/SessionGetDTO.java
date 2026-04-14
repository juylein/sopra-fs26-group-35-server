package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public class SessionGetDTO {
	private Long id;
	private LocalDateTime start_time;
	private LocalDateTime end_time;

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
}
