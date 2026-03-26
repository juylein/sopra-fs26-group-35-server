package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.entity.User;

/**
 * Internal Friendship Representation
 * This class composes the internal representation of friendships and defines how
 * friendships are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "friendships")
public class Friendships implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="user_a_id", nullable = false)
	private User userA;

	@ManyToOne
	@JoinColumn(name = "user_b_id", nullable = false)
	private User userB;

	@Column(nullable = false)
	private LocalDateTime since = LocalDateTime.now(); 


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUserA(){
		return userA;
	}

	public void setUserA(User userA){
		this.userA = userA;
	}

	public User getUserB(){
		return userB;
	}

	public void setUserB(User userB){
		this.userB = userB;
	}

	public LocalDateTime getSince(){
		return since;
	}

	public void setSince(LocalDateTime since){
		this.since = since;
	}
}
