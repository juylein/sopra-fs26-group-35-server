package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewsRepository reviewsRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testBook = new Book();
        testBook.setId("book123");
        testBook.setName("Test Book");
    }

    @Test
    void getBook_validId_returnsBook() {
        when(bookRepository.findById("book123")).thenReturn(Optional.of(testBook));

        Book result = bookService.getBook("book123");

        assertNotNull(result);
        assertEquals("book123", result.getId());
        assertEquals("Test Book", result.getName());
    }

    @Test
    void getBook_invalidId_throwsException() {
        when(bookRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> bookService.getBook("unknown"));
    }

    @Test
    void getAverageRating_noReviews_returnsZero() {
        when(reviewsRepository.findByBook(testBook)).thenReturn(List.of());

        Double result = bookService.getAverageRating(testBook);

        assertEquals(0.0, result);
    }

    @Test
    void getAverageRating_singleReview_returnsRating() {
        Reviews review = new Reviews();
        review.setRating(4);
        review.setReview("Good book");
        review.setBook(testBook);

        when(reviewsRepository.findByBook(testBook)).thenReturn(List.of(review));

        Double result = bookService.getAverageRating(testBook);

        assertEquals(4.0, result);
    }

    @Test
    void getAverageRating_multipleReviews_returnsCorrectAverage() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        Reviews r1 = new Reviews();
        r1.setRating(4);
        r1.setReview("Good");
        r1.setUser(user1);
        r1.setBook(testBook);

        Reviews r2 = new Reviews();
        r2.setRating(2);
        r2.setReview("Okay");
        r2.setUser(user2);
        r2.setBook(testBook);

        when(reviewsRepository.findByBook(testBook)).thenReturn(List.of(r1, r2));

        Double result = bookService.getAverageRating(testBook);

        assertEquals(3.0, result);
    }

    @Test
    void getReviewsForBook_validId_returnsReviews() {
        when(bookRepository.findById("book123")).thenReturn(Optional.of(testBook));

        Reviews review = new Reviews();
        review.setId(1L);
        review.setRating(5);
        review.setReview("Excellent!");
        review.setBook(testBook);

        when(reviewsRepository.findByBook(testBook)).thenReturn(List.of(review));

        List<Reviews> result = bookService.getReviewsforBook("book123");

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
    }

    @Test
    void getReviewsForBook_bookNotFound_throwsException() {
        when(bookRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> bookService.getReviewsforBook("unknown"));
    }

    @Test
    void getReviewsForBook_noReviews_returnsEmptyList() {
        when(bookRepository.findById("book123")).thenReturn(Optional.of(testBook));
        when(reviewsRepository.findByBook(testBook)).thenReturn(List.of());

        List<Reviews> result = bookService.getReviewsforBook("book123");

        assertTrue(result.isEmpty());
    }
}
