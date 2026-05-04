package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ShelfBookRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShelfBookRepository shelfBookRepository;

    private User testUser;
    private Book testBook;
    private Shelf testShelf;
    private ShelfBook testShelfBook;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername("User");
        testUser.setName("User");
        testUser.setToken("token");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setPassword("password");
        entityManager.persist(testUser);

        testBook = new Book();
        testBook.setId("1");
        testBook.setName("Bridgerton");
        entityManager.persist(testBook);

        testShelf = new Shelf();
        testShelf.setName("To Read");
        testShelf.setOwner(testUser);
        testShelf.setShared(false);
        testShelf.setBooks(new ArrayList<>());
        entityManager.persist(testShelf);

        testShelfBook = new ShelfBook();
        testShelfBook.setShelf(testShelf);
        testShelfBook.setBook(testBook);
        testShelfBook.setStatus(BookStatus.UNREAD);
        entityManager.persist(testShelfBook);

        entityManager.flush();
    }

    //is there a ShelfBook on shelf X containing book Y?
    @Test
    public void findByShelfIdAndBookId_validIds_returnsShelfBook() {
        Optional<ShelfBook> result = shelfBookRepository
                .findByShelfIdAndBookId(testShelf.getId(), "1");

        assertTrue(result.isPresent());
        assertEquals(testShelf.getId(), result.get().getShelf().getId());
        assertEquals("1", result.get().getBook().getId());
        assertEquals(BookStatus.UNREAD, result.get().getStatus());
    }

    //Asks for a book with an ID that does not exist in the database.
    @Test
    public void findByShelfIdAndBookId_wrongBookId_returnsEmpty() {
        Optional<ShelfBook> result = shelfBookRepository
                .findByShelfIdAndBookId(testShelf.getId(), "non-existent-book");

        assertTrue(result.isEmpty());
    }

    //Does this user have this book on any shelf named 'To Read', 'Recent Readings', or 'Read'?
    @Test
    public void findByOwnerIdAndBookIdAndShelfNameIn_validInput_returnsShelfBook() {
        List<String> statusShelfNames = List.of("To Read", "Recent Readings", "Read");

        Optional<ShelfBook> result = shelfBookRepository
                .findByShelf_OwnerIdAndBookIdAndShelf_NameIn(
                        testUser.getId(), "1", statusShelfNames);

        assertTrue(result.isPresent());
        assertEquals("To Read", result.get().getShelf().getName());
        assertEquals("1", result.get().getBook().getId());
    }

    //Verifies that findByShelf_OwnerIdAndBookIdAndShelf_NameIn returns empty when the shelf name is not in the given list, even if the owner and book match.
    @Test
    public void findByOwnerIdAndBookIdAndShelfNameIn_shelfNameNotInList_returnsEmpty() {
        List<String> otherShelfNames = List.of("Favorites", "Wishlist");

        Optional<ShelfBook> result = shelfBookRepository
                .findByShelf_OwnerIdAndBookIdAndShelf_NameIn(
                        testUser.getId(), "1", otherShelfNames);

        assertTrue(result.isEmpty());
    }
}