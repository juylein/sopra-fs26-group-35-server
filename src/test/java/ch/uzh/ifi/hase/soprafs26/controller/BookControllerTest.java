package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Qualifier;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Test
    @WithMockUser
    public void getBook_validId_returnsBook() throws Exception {
        Book book = new Book();
        book.setId("abc123");
        book.setName("Dune");
        book.setAuthors(List.of("Frank Herbert"));
        book.setPages(412L);
        book.setReleaseYear(1965);
        book.setGenre("Science Fiction");
        book.setDescription("A desert planet story.");

        given(bookService.getBook("abc123")).willReturn(book);

        mockMvc.perform(get("/books/abc123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("abc123")))
                .andExpect(jsonPath("$.name", is("Dune")))
                .andExpect(jsonPath("$.pages", is(412)))
                .andExpect(jsonPath("$.releaseYear", is(1965)))
                .andExpect(jsonPath("$.genre", is("Science Fiction")));
    }

    @Test
    @WithMockUser
    public void getBook_unknownId_returns404() throws Exception {
        given(bookService.getBook("nonexistent"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        mockMvc.perform(get("/books/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //tests for reviews

    @Test
    @WithMockUser
    public void getReviewsForBook_validId_returnsReviews() throws Exception {
        User user = new User();
        user.setUsername("testuser1");

        Book book = new Book();
        book.setId("abc123");

        Reviews review = new Reviews();
        review.setId(1L);
        review.setUser(user);
        review.setBook(book);
        review.setRating(5);
        review.setReview("Great book!");

        given(bookService.getReviewsforBook("abc123")).willReturn(List.of(review));

        mockMvc.perform(get("/books/abc123/reviews")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("testuser1")))
                .andExpect(jsonPath("$[0].rating", is(5)))
                .andExpect(jsonPath("$[0].review", is("Great book!")));
    }

    @Test
    @WithMockUser
    public void getReviewsForBook_noBook_returns404() throws Exception {
        given(bookService.getReviewsforBook("nonexistent"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        mockMvc.perform(get("/books/nonexistent/reviews")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getReviewsForBook_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/books/abc123/reviews")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}