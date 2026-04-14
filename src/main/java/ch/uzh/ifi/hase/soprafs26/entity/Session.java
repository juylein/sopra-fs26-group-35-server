package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
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

	@ManyToOne
	@JoinColumn(name = "host_id", nullable = false)
	private User host;

	//add session participants here if needed

	@ManyToOne
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false)
	private LocalDateTime startTime;

	@Column(nullable = true)
	private LocalDateTime endTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getHost() { return host; }
	public void setHost(User host) { this.host = host; }

	public Book getBook() { return book; }
	public void setBook(Book book) { this.book = book; }

	public LocalDateTime getStartTime() { return startTime; }
	public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

	public LocalDateTime getEndTime() { return endTime; }
	public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

}
