package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LibraryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookGetDTO;
import java.util.List;                        
import java.util.ArrayList;
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

    public ShelfGetDTO addShelf(User user, String name){
        Shelf shelf = new Shelf();
        shelf.setName(name);
        shelf.setOwner(user);
        shelf.setShared(false);
        Shelf saved = shelfRepository.save(shelf);
    
        ShelfGetDTO dto = new ShelfGetDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setShared(saved.getShared());
        dto.setBooks(new ArrayList<>());
        return dto;
    }

    public LibraryDTO getLibrary(User user) {
        List<ShelfGetDTO> shelfDTOs = new ArrayList<>();

        for (Shelf shelf : user.getShelves()) {
        
            ShelfGetDTO shelfDTO = new ShelfGetDTO();
            shelfDTO.setId(shelf.getId());
            shelfDTO.setName(shelf.getName());
            shelfDTO.setShared(shelf.getShared());

            List<BookGetDTO> bookDTOs = new ArrayList<>();
            for (Book book : shelf.getBooks()) {
                BookGetDTO bookDTO = new BookGetDTO();
                bookDTO.setId(book.getId());
                bookDTO.setName(book.getName());
                bookDTO.setAuthors(book.getAuthors());
                bookDTO.setPages(book.getPages());
                bookDTO.setReleaseYear(book.getReleaseYear());
                bookDTO.setGenre(book.getGenre());
                bookDTO.setDescription(book.getDescription());
                bookDTOs.add(bookDTO);
            }
            shelfDTO.setBooks(bookDTOs);
            shelfDTOs.add(shelfDTO);
        }

        LibraryDTO libraryDTO = new LibraryDTO();
        libraryDTO.setShelves(shelfDTOs);
        return libraryDTO;
    }

    public ShelfGetDTO addBookToShelf(User user, Long shelfId, BookPostDTO bookPostDTO) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (!shelf.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Reuse existing book if already in DB -> otherwise create it
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
        shelfRepository.save(shelf);

        ShelfGetDTO dto = new ShelfGetDTO();
        dto.setId(shelf.getId());
        dto.setName(shelf.getName());
        dto.setShared(shelf.getShared());

        List<BookGetDTO> bookDTOs = new ArrayList<>();
        for (Book b : shelf.getBooks()) {
            BookGetDTO bookDTO = new BookGetDTO();
            bookDTO.setId(b.getId());
            bookDTO.setName(b.getName());
            bookDTO.setAuthors(b.getAuthors());
            bookDTO.setPages(b.getPages());
            bookDTO.setReleaseYear(b.getReleaseYear());
            bookDTO.setGenre(b.getGenre());
            bookDTO.setDescription(b.getDescription());
            bookDTOs.add(bookDTO);
        }
        dto.setBooks(bookDTOs);
        return dto;
    }
}