package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BookGetDTO getBook(@PathVariable String bookId) {
        Book book = bookService.getBook(bookId);
        BookGetDTO bookGetDTO = DTOMapper.INSTANCE.convertBookEntityToGetDTO(book);
        bookGetDTO.setAverageRating(bookService.getAverageRating(book));
        return bookGetDTO;
    }

    @GetMapping("/books/{bookId}/reviews")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ReviewGetDTO> getReviewsForBook(@PathVariable String bookId){
        return DTOMapper.INSTANCE.convertReviewEntitiesToGetDTOs(bookService.getReviewsforBook(bookId));
    }

}