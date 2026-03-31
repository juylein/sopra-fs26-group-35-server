package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Book;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Internal Review Representation
 * This class composes the internal representation of the reviews and defines how
 * the reviews are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "reviews")
public class Reviews implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user; 

	@ManyToOne
	@JoinColumn(name = "book_id")
	private Book book;

	@Column(nullable = false)
    private String review;

	@Column(nullable = true)
	private LocalDateTime timestamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDateTime(){
		return timestamp;
	}

	public void setDateTime(LocalDateTime timestamp){
		this.timestamp = timestamp;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book){
		this.book = book;
	}

	public String getReview(){
		return review;
	}

	public void setReview(String review){
		this.review = review;
	}

}
