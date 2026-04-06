package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import java.util.List;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;
import org.mapstruct.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {

    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LibraryService(
            ShelfRepository shelfRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {
        this.shelfRepository = shelfRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Long userId) {
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id " + userId + " not found"));
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to perform this action for this user");
        }
        return user;
    }

    public List<Shelf> getLibrary(Long userId) {
        User user = getAuthenticatedUser(userId);
        return user.getShelves();
    }

    public Shelf addShelf(Long userId, String name) {
        User user = getAuthenticatedUser(userId);

        Shelf shelf = new Shelf();
        shelf.setName(name);
        shelf.setOwner(user);
        shelf.setShared(false);

        return shelfRepository.save(shelf);
    }

    public Shelf addBookToShelf(Long userId, Long shelfId, BookPostDTO bookPostDTO) {
        User user = getAuthenticatedUser(userId);

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (!shelf.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

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