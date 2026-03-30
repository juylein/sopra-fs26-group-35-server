package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.io.Serializable;


/**
 * Internal Book Representation
 * This class composes the internal representation of the book and defines how
 * the books are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "books")
public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String googleId; //use API ID as primary key

	@Column(nullable = false)
	private String name;

	@ElementCollection
	@CollectionTable(name = "book_authors")
	@Column(name = "author")
	private List <String> authors;

	@Column(nullable = true)
	private Long pages;

	@Column(nullable = true)
	private Integer release_year;

	@Column(nullable = true)
	private String genre;
	
	@Column(nullable = true, length = 2000)
	private String description;

	@Column(nullable = true)
	private Long status_page_num;

	@ManyToMany(mappedBy = "books")
	private Set<Shelf> shelves = new HashSet<>();

	public String getId() {
		return googleId;
	}
	public void setId(String googleId) {
		this.googleId = googleId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List getAuthors() {
		return authors;
	}
	public void setAuthors(List authors) {
		this.authors = authors;
	}

	public Long getPages() {
		return pages;
	}
	public void setPages(Long pages) {
		this.pages = pages;
	}

	public Integer getReleaseYear() {
		return release_year;
	}
	public void setReleaseYear(Integer release_year) {
		this.release_year = release_year;
	}

	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getStatusPageNum(){
		return status_page_num;
	}

	public void setStatusPageNum(Long status_page_num){
		this.status_page_num = status_page_num;
	}

	public Set<Shelf> getShelves(){
		return shelves;
	}

}
