package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewsRepository reviewsRepository;

    @Autowired
    public BookService(BookRepository bookRepository, ReviewsRepository reviewsRepository) {
        this.bookRepository = bookRepository;
        this.reviewsRepository = reviewsRepository;
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
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));;
        return reviewsRepository.findByBook(book);
    }
}