package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;

import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;

import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ShelfBookRepository shelfBookRepository;

    @Mock
    private ActivitiesService activitiesService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LibraryService libraryService;

    private User testUser;
    private Shelf shelf;
    private Book book;
    private ShelfBook shelfBook;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("valid-token");
        testUser.setStatus(UserStatus.ONLINE);

        book = new Book();
        book.setId("google_test_id");

        shelf = new Shelf();
        shelf.setId(1L);
        shelf.setOwner(testUser);

        shelfBook = new ShelfBook();
        shelfBook.setShelf(shelf);
        shelfBook.setBook(book);
        shelfBook.setStatus(BookStatus.READING);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("testUser", "valid-token", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    public void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getLibrary_validUser_returnsShelves() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        List<Shelf> result = libraryService.getLibrary(1L);

        assertEquals(testUser.getShelves(), result);
    }

    @Test
    public void getLibrary_userNotFound_throws404() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> libraryService.getLibrary(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void getLibrary_wrongToken_throws403() {
        UsernamePasswordAuthenticationToken wrongAuth =
                new UsernamePasswordAuthenticationToken("testUser", "wrong-token", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(wrongAuth);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> libraryService.getLibrary(1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void addShelf_validInput_createsShelf() {
        Shelf savedShelf = new Shelf();
        savedShelf.setName("Favorites");
        savedShelf.setOwner(testUser);
        savedShelf.setShared(false);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.save(any(Shelf.class))).willReturn(savedShelf);

        Shelf result = libraryService.addShelf(1L, "Favorites");

        assertNotNull(result);
        assertEquals("Favorites", result.getName());
        assertEquals(testUser, result.getOwner());
        verify(shelfRepository, times(1)).save(any(Shelf.class));
    }

    @Test
    public void addBookToShelf_newBook_createsAndAddsBook() {
        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setOwner(testUser);

        shelf.setBooks(new ArrayList<>());

        BookPostDTO dto = new BookPostDTO();
        dto.setGoogleId("abc123");
        dto.setName("Dune");
        dto.setAuthors(List.of("Frank Herbert"));
        dto.setPages(412L);
        dto.setReleaseYear(1965);
        dto.setGenre("Science Fiction");
        dto.setDescription("Description");

        Book newBook = new Book();
        newBook.setId("abc123");
        newBook.setName("Dune");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(bookRepository.findById("abc123")).willReturn(Optional.empty());
        given(bookRepository.save(any(Book.class))).willReturn(newBook);
        given(shelfRepository.save(shelf)).willReturn(shelf);

        Shelf result = libraryService.addBookToShelf(1L, 1L, dto);

        assertNotNull(result);
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(shelfRepository, times(1)).save(shelf);
    }

    @Test
    public void addBookToShelf_existingBook_reusesBook() {
        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setOwner(testUser);

        shelf.setBooks(new ArrayList<>());
    
        BookPostDTO dto = new BookPostDTO();
        dto.setGoogleId("abc123");
    
        Book existingBook = new Book();
        existingBook.setId("abc123");
    
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(bookRepository.findById("abc123")).willReturn(Optional.of(existingBook));
        given(shelfRepository.save(shelf)).willReturn(shelf);
    
        libraryService.addBookToShelf(1L, 1L, dto);
    
        verify(bookRepository, never()).save(any(Book.class));
        verify(shelfRepository, times(1)).save(shelf);
    }

    @Test
    public void addBookToShelf_shelfNotFound_throws404() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> libraryService.addBookToShelf(1L, 99L, new BookPostDTO()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void addBookToShelf_shelfOwnedByOtherUser_throws403() {
        User otherUser = new User();
        otherUser.setId(2L);

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setOwner(otherUser);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> libraryService.addBookToShelf(1L, 1L, new BookPostDTO()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void updateBookStatus_validInput_StatusUpdated(){
        // given
        Shelf readShelf = new Shelf();
        readShelf.setId(2L);
        readShelf.setName("Read");
        readShelf.setOwner(testUser);
        readShelf.setBooks(new ArrayList<>());
        testUser.setShelves(new ArrayList<>());
        testUser.getShelves().add(readShelf);
    
        shelf.setBooks(new ArrayList<>());
    
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
    
        given(shelfBookRepository.findByShelfIdAndBookId(1L, "google_test_id"))
                .willReturn(Optional.of(shelfBook));
    
        given(shelfBookRepository.findByShelf_OwnerIdAndBookIdAndShelf_NameIn(any(), any(), any()))
                .willReturn(Optional.empty());
    
        // when
        ShelfBook result = libraryService.updateBookStatus(
                1L, "google_test_id", BookStatus.FINISHED);
    
        // then
        assertEquals(BookStatus.FINISHED, result.getStatus());
        verify(shelfBookRepository, times(1)).save(shelfBook);
        verify(activitiesService, times(1))
                .addActivity(testUser, BookStatus.FINISHED, book);
    }

    @Test
    public void updateBookStatus_shelfNotFound_throwsNotFound() {
        // given
        given(shelfRepository.findById(99L)).willReturn(Optional.empty());

        // when/then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                libraryService.updateBookStatus(99L, "google_test_id", BookStatus.FINISHED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(shelfBookRepository, never()).save(any());
        verify(activitiesService, never()).addActivity(any(), any(), any());
    }

    @Test
    public void updateBookStatus_bookNotOnShelf_throwsNotFound() {
        // given
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(shelfBookRepository.findByShelfIdAndBookId(1L, "google_test_id"))
                .willReturn(Optional.empty());

        // when/then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                libraryService.updateBookStatus(1L, "google_test_id", BookStatus.FINISHED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(shelfBookRepository, never()).save(any());
        verify(activitiesService, never()).addActivity(any(), any(), any());
    }

    @Test
    public void updateBookStatus_activityLogged_afterStatusChange() {
        // given
        Shelf readShelf = new Shelf();
        readShelf.setId(2L);
        readShelf.setName("Read");
        readShelf.setOwner(testUser);
        readShelf.setBooks(new ArrayList<>()); 
    
        testUser.setShelves(new ArrayList<>());
        testUser.getShelves().add(readShelf);
    
        shelf.setBooks(new ArrayList<>());
    
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
    
        given(shelfBookRepository.findByShelfIdAndBookId(1L, "google_test_id"))
                .willReturn(Optional.of(shelfBook));
    
        given(shelfBookRepository.findByShelf_OwnerIdAndBookIdAndShelf_NameIn(any(), any(), any()))
                .willReturn(Optional.empty());
    
        // when
        libraryService.updateBookStatus(1L, "google_test_id", BookStatus.FINISHED);
    
        // then
        verify(activitiesService, times(1))
                .addActivity(testUser, BookStatus.FINISHED, book);
    }
}