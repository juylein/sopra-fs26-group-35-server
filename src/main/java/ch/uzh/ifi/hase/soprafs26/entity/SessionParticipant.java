package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal Session Participant Representation
 * This class composes the internal representation of the session participants and defines how
 * the session participants are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "session_participant")
public class SessionParticipant implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne //many participant in one session
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@ManyToOne //one user can participate in many sessions
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "shelfbookId", nullable = false)
	private ShelfBook shelfBook;

	@Column(nullable = false)
	private LocalDateTime joinedAt;

	@Column(nullable = true)
	private LocalDateTime leftAt;

	@Column(nullable = true)
	private Long pagesRead;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Session getSession(){
		return session;
	}

	public void setSession(Session session){
		this.session = session;
	}

	public User getUser(){
		return user;
	}

	public void setUser(User user){
		this.user = user;
	}

	public ShelfBook getShelfBook(){
		return shelfBook;
	}

	public void setShelfBook(ShelfBook shelfBook){
		this.shelfBook = shelfBook;
	}

	public LocalDateTime getJoinedAt(){
		return joinedAt;
	}

	public void setJoinedAt(LocalDateTime joinedAt){
		this.joinedAt = joinedAt;
	}

	public LocalDateTime getLeftAt(){
		return leftAt;
	}

	public void setLeftAt(LocalDateTime leftAt){
		this.leftAt = leftAt;
	}

	public Long getPagesRead() {
		return pagesRead;
	}

	public void setPagesRead(Long pagesRead) {
		this.pagesRead = pagesRead;
	}

	public Duration getReadingTime(){
		return Duration.between(getJoinedAt(), getLeftAt());
	}
}
