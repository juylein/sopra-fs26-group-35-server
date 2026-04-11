 package ch.uzh.ifi.hase.soprafs26.service;

  import ch.uzh.ifi.hase.soprafs26.entity.Book;
  import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.http.HttpStatus;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  import org.springframework.web.server.ResponseStatusException;

  @Service
  @Transactional
  public class BookService {

      private final BookRepository bookRepository;

      @Autowired
      public BookService(BookRepository bookRepository) {
          this.bookRepository = bookRepository;
      }

      public Book getBook(String bookId) {
          return bookRepository.findById(bookId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
      }
  }