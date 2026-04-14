package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Internal Session Representation
 * This class composes the internal representation of the session and defines how
 * the session is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "session")
public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true) //if session disappears then all its participants too
	private List<SessionParticipant> participants = new ArrayList<>();

	@Column
	private LocalDateTime start_time;

	@Column
	private LocalDateTime end_time;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<SessionParticipant> getParticipants(){
		return participants;
	}

	public void setParticipants(List<SessionParticipant> participants){
		this.participants = participants;
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

	public Duration getSessionTime(){
		return Duration.between(getStartTime(), getEndTime());
	}
}
