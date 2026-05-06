package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.io.Serializable;

/**
 * Internal Leaderboard Representation
 * This class composes the internal representation of the leaderboard and defines how
 * the leaderboard is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "leaderboard")
public class Leaderboard implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(nullable = true)
	private Long readingPoints;

	@Column(nullable = true)
	private Long quizPoints;

	@Column(nullable = false)
	private Long totalPoints;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user){
		this.user = user;
	}

	public Long getReadingPoints() {
		return readingPoints;
	}

	public void addReadingPoints(Long readingPoints) {
		if (this.readingPoints == null){
			this.readingPoints = 0L;
		}
		this.readingPoints += readingPoints;
		updateTotalPoints();
	}

	public Long getQuizPoints() {
		return quizPoints;
	}

	public void addQuizPoints(Long quizPoints) {
		if (this.quizPoints == null){
			this.quizPoints = 0L;
		}
		this.quizPoints += quizPoints;
		updateTotalPoints();
	}

	public Long getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints() {
		this.totalPoints = 0L;
	}

	public void updateTotalPoints() {
		long reading = (readingPoints != null) ? readingPoints : 0;
		long quiz = (quizPoints != null) ? quizPoints : 0;
		this.totalPoints = reading + quiz;
	}
}
