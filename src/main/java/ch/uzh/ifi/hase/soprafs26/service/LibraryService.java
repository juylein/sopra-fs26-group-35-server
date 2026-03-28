  package ch.uzh.ifi.hase.soprafs26.service;

  import ch.uzh.ifi.hase.soprafs26.entity.User; 
  import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
  import ch.uzh.ifi.hase.soprafs26.entity.Book;
  import ch.uzh.ifi.hase.soprafs26.rest.dto.LibraryDTO;
  import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfGetDTO;
  import ch.uzh.ifi.hase.soprafs26.rest.dto.BookGetDTO;
  import java.util.List;                        
  import java.util.ArrayList;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {

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