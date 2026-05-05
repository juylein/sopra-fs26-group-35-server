package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewsRepository reviewsRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookService(BookRepository bookRepository, ReviewsRepository reviewsRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.reviewsRepository = reviewsRepository;
        this.userRepository = userRepository;
    }

    public Book getBook(String bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    public Double getAverageRating(Book book){
        List<Reviews> reviews = reviewsRepository.findByBook(book);
        Double averageRating = 0.0;
        if(reviews.isEmpty()) {return averageRating;}

        List<Integer> ratings = new ArrayList<>();
        for(int i = 0; i < reviews.size(); i++){
            Integer rating = reviews.get(i).getRating();
            ratings.add(rating);
        }
        for(int i = 0; i < ratings.size(); i++){
            averageRating += ratings.get(i);
        }
        averageRating /= ratings.size();
        return averageRating;
    }

    public List<Reviews> getReviewsforBook(String bookId){
        Book book = getBook(bookId);
        return reviewsRepository.findByBook(book);
    }


    public Reviews createReview(User user, String bookId, ReviewPostDTO reviewPostDTO){
        Book book = getBook(bookId);

        if (reviewsRepository.findByUserAndBook(user, book) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already reviewed this book");
        }

        Reviews review = new Reviews();
        review.setRating(reviewPostDTO.getRating());
        review.setReview(reviewPostDTO.getReview());
        review.setUser(user);
        review.setBook(book);
        review.setDateTime(LocalDateTime.now());

        return reviewsRepository.save(review);
    }
}