package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("shelfRepository")
public interface ShelfRepository extends JpaRepository<Shelf, Long> {
	Shelf findByName(String name);

	Shelf findByOwner(User owner);
}
