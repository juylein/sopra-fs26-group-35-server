package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import java.util.List;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {

    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;

    @Autowired
    public LibraryService(ShelfRepository shelfRepository,
                          BookRepository bookRepository) {
        this.shelfRepository = shelfRepository;
        this.bookRepository = bookRepository;
    }

    public Shelf addShelf(User user, String name) {
        Shelf shelf = new Shelf();
        shelf.setName(name);
        shelf.setOwner(user);
        shelf.setShared(false);
        return shelfRepository.save(shelf);
    }

    public List<Shelf> getLibrary(User user) {
        return user.getShelves();
    }

    public Shelf addBookToShelf(User user, Long shelfId, BookPostDTO bookPostDTO) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (!shelf.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Reuse existing book if already in DB, otherwise create it
        Book book = bookRepository.findById(bookPostDTO.getGoogleId()).orElseGet(() -> {
            Book b = new Book();
            b.setId(bookPostDTO.getGoogleId());
            b.setName(bookPostDTO.getName());
            b.setAuthors(bookPostDTO.getAuthors());
            b.setPages(bookPostDTO.getPages());
            b.setReleaseYear(bookPostDTO.getReleaseYear());
            b.setGenre(bookPostDTO.getGenre());
            b.setDescription(bookPostDTO.getDescription());
            return bookRepository.save(b);
        });

        shelf.addBook(book);
        return shelfRepository.save(shelf);
    }
}