package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfInvitation;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfInvitationRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private ShelfInvitationRepository shelfInvitationRepository;

    @Mock
    private NotificationService notificationService;

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

        // then verify activity is logged with the correct arguments
        verify(activitiesService, times(1)).addActivity(testUser, BookStatus.FINISHED, book);
    }

    @Test
    public void deleteBookfromShelf_returnsNotFound() {
        given(shelfRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> libraryService.deleteBookfromShelf(99L,"google-book-id", 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void deleteBookfromShelf_sharedShelf_memberCanDelete(){
        //Shared shelf and testUser is in the owners set
        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(1L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(Set.of(testUser));

        given(shelfRepository.findById(1L)).willReturn(Optional.of(sharedShelf));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfBookRepository.findByShelfIdAndBookId(1L, "google-book-id")).willReturn(Optional.of(shelfBook));

        assertDoesNotThrow(() -> libraryService.deleteBookfromShelf(1L, "google-book-id", 1L));
        verify(shelfBookRepository, times(1)).delete(shelfBook);
    }

    @Test
    public void deleteBookfromShelf_sharedShelf_memberForbidden403(){
        User notMember = new User();
        notMember.setId(99L);

        //Shared shelf
        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(1L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(Set.of(testUser));

        given(shelfRepository.findById(1L)).willReturn(Optional.of(sharedShelf));
        given(userRepository.findById(99L)).willReturn(Optional.of(notMember));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> libraryService.deleteBookfromShelf(1L, "google-book-id", 99L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(shelfBookRepository, never()).delete(any());
    }

    // ── inviteToShelf ────────────────────────────────────────────────────────

    @Test
    public void inviteToShelf_success_createsInvitationAndNotification() {
        User target = new User();
        target.setId(2L);
        target.setUsername("targetUser");

        User requester = spy(new User());
        requester.setId(1L);
        requester.setToken("valid-token");
        requester.setUsername("testUser");
        doReturn(Set.of(target)).when(requester).getFriends();

        shelf.setName("My Shelf");
        shelf.setOwner(requester);

        ShelfInvitation savedInvitation = new ShelfInvitation();
        savedInvitation.setId(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(shelfInvitationRepository.existsByShelfIdAndRecipientIdAndStatus(1L, 2L, "PENDING")).willReturn(false);
        given(shelfInvitationRepository.save(any(ShelfInvitation.class))).willReturn(savedInvitation);

        assertDoesNotThrow(() -> libraryService.inviteToShelf(1L, 1L, 2L));
        verify(shelfInvitationRepository).save(any(ShelfInvitation.class));
        verify(notificationService).createNotification(eq(2L), eq(NotificationType.SHELF_INVITATION), anyString(), eq(10L));
    }

    @Test
    public void inviteToShelf_statusShelf_throws400() {
        shelf.setName("To Read");
        shelf.setOwner(testUser);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.inviteToShelf(1L, 1L, 2L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(shelfInvitationRepository, never()).save(any());
    }

    @Test
    public void inviteToShelf_requesterNotOwnerOrMember_throws403() {
        User otherOwner = new User();
        otherOwner.setId(2L);

        shelf.setName("Some Shelf");
        shelf.setOwner(otherOwner);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.inviteToShelf(1L, 1L, 3L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void inviteToShelf_targetNotFriend_throws403() {
        User target = new User();
        target.setId(2L);

        shelf.setName("My Shelf");
        shelf.setOwner(testUser);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(userRepository.findById(2L)).willReturn(Optional.of(target));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.inviteToShelf(1L, 1L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void inviteToShelf_targetAlreadyMember_throws409() {
        User target = new User();
        target.setId(2L);
        target.setUsername("targetUser");

        User requester = spy(new User());
        requester.setId(1L);
        requester.setToken("valid-token");
        doReturn(Set.of(target)).when(requester).getFriends();

        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(1L);
        sharedShelf.setName("Shared Shelf");
        sharedShelf.setShared(true);
        sharedShelf.setOwners(new HashSet<>(Set.of(requester, target)));

        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(sharedShelf));
        given(userRepository.findById(2L)).willReturn(Optional.of(target));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.inviteToShelf(1L, 1L, 2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(shelfInvitationRepository, never()).save(any());
    }

    @Test
    public void inviteToShelf_duplicatePendingInvitation_throws409() {
        User target = new User();
        target.setId(2L);
        target.setUsername("targetUser");

        User requester = spy(new User());
        requester.setId(1L);
        requester.setToken("valid-token");
        doReturn(Set.of(target)).when(requester).getFriends();

        shelf.setName("My Shelf");
        shelf.setOwner(requester);

        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(shelfRepository.findById(1L)).willReturn(Optional.of(shelf));
        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(shelfInvitationRepository.existsByShelfIdAndRecipientIdAndStatus(1L, 2L, "PENDING")).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.inviteToShelf(1L, 1L, 2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(shelfInvitationRepository, never()).save(any());
    }

    // ── acceptShelfInvitation ────────────────────────────────────────────────

    @Test
    public void acceptShelfInvitation_convertsPrivateShelfToShared() {
        User originalOwner = new User();
        originalOwner.setId(3L);

        Shelf privateShelf = new Shelf();
        privateShelf.setId(2L);
        privateShelf.setShared(false);
        privateShelf.setOwner(originalOwner);
        privateShelf.setOwners(new HashSet<>());

        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(testUser);
        invitation.setShelf(privateShelf);
        invitation.setStatus("PENDING");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        assertDoesNotThrow(() -> libraryService.acceptShelfInvitation(1L, 5L));

        assertTrue(privateShelf.getShared());
        assertTrue(privateShelf.getOwners().contains(originalOwner));
        assertTrue(privateShelf.getOwners().contains(testUser));
        assertNull(privateShelf.getOwner());
        assertEquals("ACCEPTED", invitation.getStatus());
        verify(shelfRepository).save(privateShelf);
        verify(shelfInvitationRepository).save(invitation);
    }

    @Test
    public void acceptShelfInvitation_alreadySharedShelf_addsRecipient() {
        User existingMember = new User();
        existingMember.setId(3L);

        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(2L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(new HashSet<>(Set.of(existingMember)));

        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(testUser);
        invitation.setShelf(sharedShelf);
        invitation.setStatus("PENDING");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        assertDoesNotThrow(() -> libraryService.acceptShelfInvitation(1L, 5L));

        assertTrue(sharedShelf.getOwners().contains(testUser));
        assertEquals("ACCEPTED", invitation.getStatus());
        verify(shelfRepository).save(sharedShelf);
    }

    @Test
    public void acceptShelfInvitation_nonRecipient_throws403() {
        User realRecipient = new User();
        realRecipient.setId(99L);

        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(realRecipient);
        invitation.setStatus("PENDING");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.acceptShelfInvitation(1L, 5L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(shelfRepository, never()).save(any());
    }

    @Test
    public void acceptShelfInvitation_nonPendingStatus_throws409() {
        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(testUser);
        invitation.setStatus("ACCEPTED");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.acceptShelfInvitation(1L, 5L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(shelfRepository, never()).save(any());
    }

    // ── rejectShelfInvitation ────────────────────────────────────────────────

    @Test
    public void rejectShelfInvitation_success() {
        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(testUser);
        invitation.setStatus("PENDING");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        assertDoesNotThrow(() -> libraryService.rejectShelfInvitation(1L, 5L));

        assertEquals("REJECTED", invitation.getStatus());
        verify(shelfInvitationRepository).save(invitation);
    }

    @Test
    public void rejectShelfInvitation_nonRecipient_throws403() {
        User realRecipient = new User();
        realRecipient.setId(99L);

        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(realRecipient);
        invitation.setStatus("PENDING");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.rejectShelfInvitation(1L, 5L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void rejectShelfInvitation_nonPendingStatus_throws409() {
        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setId(5L);
        invitation.setRecipient(testUser);
        invitation.setStatus("REJECTED");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfInvitationRepository.findById(5L)).willReturn(Optional.of(invitation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.rejectShelfInvitation(1L, 5L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(shelfInvitationRepository, never()).save(any(ShelfInvitation.class));
    }

    // ── getSharedShelves ─────────────────────────────────────────────────────

    @Test
    public void getSharedShelves_returnsUserSharedShelves() {
        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(10L);
        sharedShelf.setName("Book Club");
        sharedShelf.setShared(true);
        testUser.getSharedShelves().add(sharedShelf);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        List<Shelf> result = libraryService.getSharedShelves(1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(sharedShelf));
    }

    // ── deleteShelf (shared) ─────────────────────────────────────────────────

    @Test
    public void deleteShelf_sharedShelf_memberCanDelete() {
        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(2L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(new HashSet<>(Set.of(testUser)));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(2L)).willReturn(Optional.of(sharedShelf));

        assertDoesNotThrow(() -> libraryService.deleteShelf(1L, 2L));
        verify(shelfRepository).delete(sharedShelf);
    }

    @Test
    public void deleteShelf_sharedShelf_nonMember_throws403() {
        User member = new User();
        member.setId(3L);

        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(2L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(new HashSet<>(Set.of(member)));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(2L)).willReturn(Optional.of(sharedShelf));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.deleteShelf(1L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(shelfRepository, never()).delete(any());
    }

    // ── renameShelf (shared) ─────────────────────────────────────────────────

    @Test
    public void renameShelf_sharedShelf_memberCanRename() {
        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(2L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(new HashSet<>(Set.of(testUser)));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(2L)).willReturn(Optional.of(sharedShelf));

        assertDoesNotThrow(() -> libraryService.renameShelf(1L, 2L, "New Name"));

        assertEquals("New Name", sharedShelf.getName());
        verify(shelfRepository).save(sharedShelf);
    }

    @Test
    public void renameShelf_sharedShelf_nonMember_throws403() {
        User member = new User();
        member.setId(3L);

        Shelf sharedShelf = new Shelf();
        sharedShelf.setId(2L);
        sharedShelf.setShared(true);
        sharedShelf.setOwner(null);
        sharedShelf.setOwners(new HashSet<>(Set.of(member)));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(shelfRepository.findById(2L)).willReturn(Optional.of(sharedShelf));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> libraryService.renameShelf(1L, 2L, "New Name"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(shelfRepository, never()).save(any());
    }
}