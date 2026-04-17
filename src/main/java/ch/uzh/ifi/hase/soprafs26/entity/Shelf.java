package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Internal Shelf Representation
 * This class composes the internal representation of the shelf and defines how
 * shelfs (arrays of books) are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "shelf")
public class Shelf implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private boolean shared = false; //field to indicate if shelf is shared

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ShelfBook> shelfBooks = new HashSet<>();

	@ManyToOne //single-owner shelves
	@JoinColumn(name = "owner_id")
	private User owner; //null if shared

	@ManyToMany
	@JoinTable(name = "shared_shelves", 
	joinColumns = @JoinColumn(name = "shelf_id"), 
	inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<User> owners = new HashSet<>(); //only populated if shared true

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getName(){
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean getShared(){
		return shared;
	}
	public void setShared(boolean shared){
		this.shared = shared;
	}

	public User getOwner(){
		return owner;
	}
	public void setOwner(User owner){
		this.owner = owner;
	}

	public Set<User> getOwners(){
		return owners;
	}

	public void setOwners(Set<User> owners){
		this.owners = owners;
	}

	public Set<ShelfBook> getBooks(){
		return shelfBooks;
	}
	public void addBook(Book book){
		ShelfBook shelfBook = new ShelfBook();
		shelfBook.setShelf(this);
		shelfBook.setBook(book);
		shelfBooks.add(shelfBook);
	}
}
