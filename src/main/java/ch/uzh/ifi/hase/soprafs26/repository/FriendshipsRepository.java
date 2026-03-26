package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Friendships;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("friendshipsRepository")
public interface FriendshipsRepository extends JpaRepository<Friendships, Long> {
	Friendships findByUser(User user);
}
