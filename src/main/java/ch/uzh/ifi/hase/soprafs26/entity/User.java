package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = true)
	private String favouriteGenre;

	@OneToOne(mappedBy = "user")
	private Leaderboard leaderboard;

	@OneToMany(mappedBy= "userA")
	private Set<Friendships> friendshipsInitiated = new HashSet<>();

	@OneToMany(mappedBy= "userB")
	private Set<Friendships> friendshipsReceived = new HashSet<>();

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Shelf> shelves = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getFavGenre(){
		return favouriteGenre;
	}

	public void setFavGenre(String favouriteGenre) {
		this.favouriteGenre = favouriteGenre;
	}

	public Set<User> getFriends(){
		Set <User> friends = new HashSet<>();
		for (Friendships f: friendshipsInitiated){
			friends.add(f.getUserB());
		}
		for (Friendships f: friendshipsReceived){
			friends.add(f.getUserA());
		}
		return friends;
	}

	public Leaderboard getLeaderboard(){
		return leaderboard;
	}

	public void setLeaderboard(Leaderboard leaderboard){
		this.leaderboard = leaderboard;
	}

	//counting friends
	public Long getNumFriends(){
		return (long) getFriends().size();
	}

	//counting read books and pages
	//helper method to get the "read" shelf
	public Shelf getReadShelf(){
		return shelves.stream()
		.filter(
			s -> !s.getShared() && "read".equals(s.getName())
		)
		.findFirst()
		.orElse(null);
	}

	public Long getBooksRead(){
		Shelf readShelf = getReadShelf();
		return readShelf == null ? 0L: readShelf.getBooks().size();
	}

	public Long getPagesRead() {
		Shelf readShelf = getReadShelf();
		return readShelf == null ? 0L: readShelf.getBooks().stream().mapToLong(Book::getPages).sum();
	}

}
