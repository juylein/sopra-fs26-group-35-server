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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {

    private final ShelfRepository shelfRepository;

    @Autowired
    public LibraryService(ShelfRepository shelfRepository){
        this.shelfRepository = shelfRepository;
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
        // exclude the "Read" shelf — only used for stats
            if ("Read".equals(shelf.getName())) continue;

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
}