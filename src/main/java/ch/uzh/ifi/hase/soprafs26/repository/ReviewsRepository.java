package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Book;

@Repository("reviewsRepository")
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
	Reviews findByUser(User user);
	Reviews findByBook(Book book);
}
