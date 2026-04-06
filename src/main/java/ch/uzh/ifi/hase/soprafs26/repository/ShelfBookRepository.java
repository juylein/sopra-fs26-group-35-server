package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.Book;

import java.util.Optional;

@Repository("shelfbookRepository")
public interface ShelfBookRepository extends JpaRepository<ShelfBook, Long> {
	Optional<ShelfBook> findByShelfIdAndBookId(Long shelfId, String bookId);
}
