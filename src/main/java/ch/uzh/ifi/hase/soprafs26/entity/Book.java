package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
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
	private String coverUrl;

	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ShelfBook> shelves = new HashSet<>();

	@OneToMany(mappedBy = "book")
	private Set<Activities> activities;

	@OneToMany(mappedBy = "book")
	private Set<Reviews> reviews;

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

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
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

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl){
		this.coverUrl = coverUrl;
	}

	public Set<ShelfBook> getShelves(){
		return shelves;
	}


}
