package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;

import java.io.Serializable;


/**
 * Internal ShelfBook Representation
 * This class composes the internal representation of the shelf-book and defines how
 * the shelf-books are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "shelfbook")
public class ShelfBook implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "shelf_id", nullable = false)
	private Shelf shelf;

	@ManyToOne
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private BookStatus status = BookStatus.UNREAD;

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public Shelf getShelf(){
		return shelf;
	}

	public void setShelf(Shelf shelf){
		this.shelf = shelf;
	}

	public Book getBook(){
		return book;
	}

	public void setBook(Book book){
		this.book = book;
	}

	public BookStatus getStatus(){
		return status;
	}

	public void setStatus(BookStatus status){
		this.status = status;
	}}
