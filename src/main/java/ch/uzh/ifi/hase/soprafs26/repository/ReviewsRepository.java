package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Book;

import java.util.List;


@Repository("reviewsRepository")
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
	List<Reviews> findByUser(User user);
	List<Reviews> findByBook(Book book);
	Reviews findByUserAndBook(User user, Book book);
}
