package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String password;

	@Column(nullable = true, unique = true)
	private String token;

	@Column(nullable = false)
	private UserStatus status;

    @Column(nullable = true)
    private String bio;

    @CreationTimestamp
    private LocalDateTime creationDate;

    @ElementCollection
    @CollectionTable(
            name = "user_genres",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "genre")
    private List<String> genres;
	@Column(nullable = true)
	private String favouriteGenre;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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

  	public String getBio() { 
		return bio; 
	}
  	public void setBio(String bio) { 
		this.bio = bio; 
	}

  	public String getPassword() { return password; }
  	public void setPassword(String password) { 
		this.password = password; 
	}

  	public List<String> getGenres() { 
		return genres; 
	}
  	public void setGenres(List<String> genres) { 
		this.genres = genres; 
	}

	public LocalDateTime getCreationDate(){
		return creationDate;
	}
	public void setCreationDate(LocalDateTime creationDate){
		this.creationDate = creationDate;
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
    public Long getNumFriends(){
        return (long) getFriends().size();
    }	//counting friends

	public Leaderboard getLeaderboard(){
		return leaderboard;
	}	
	public void setLeaderboard(Leaderboard leaderboard){
		this.leaderboard = leaderboard;
	}

    //counting read books and pages
	//helper method to get the "read" shelf
	public Shelf getReadShelf(){
		return shelves.stream()
		.filter(
			s -> !s.getShared() && "read".equalsIgnoreCase(s.getName())
		)
		.findFirst()
		.orElse(null);
	}

	public List<Shelf> getShelves() {
	 	return shelves;
	}

	public Long getBooksRead(){
		Shelf readShelf = getReadShelf();
		return readShelf == null ? 0L: (long) readShelf.getBooks().size();
	}

	public Long getPagesRead() {
    Shelf readShelf = getReadShelf();
    return readShelf == null ? 0L : readShelf.getBooks().stream()
            .map(ShelfBook::getBook)
            .mapToLong(b -> b.getPages() != null ? b.getPages() : 0L)
            .sum();
}

}
