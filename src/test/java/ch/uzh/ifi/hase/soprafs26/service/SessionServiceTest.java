package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionParticipantRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfBookGetDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShelfBookRepository shelfBookRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private LibraryService libraryService;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActivitiesService activitiesService;

    @InjectMocks
    private SessionService sessionService;

    private User user1;
    private User user2;
    private ShelfBook shelfBook1;
    private ShelfBook shelfBook2;

    @BeforeEach
    public void setup() {
        user1 = new User();
        user1.setId(1L);
        user1.setToken("token-1");

        user2 = new User();
        user2.setId(2L);
        user2.setToken("token-2");

        Book book1 = new Book();
        book1.setId("book-1");
        Book book2 = new Book();
        book2.setId("book-2");

        Shelf shelf = new Shelf();
        shelf.setId(10L);

        shelfBook1 = new ShelfBook();
        shelfBook1.setId(1L);
        shelfBook1.setBook(book1);
        shelfBook1.setShelf(shelf);
        shelfBook1.setStatus(BookStatus.READING);

        shelfBook2 = new ShelfBook();
        shelfBook2.setId(2L);
        shelfBook2.setBook(book2);
        shelfBook2.setShelf(shelf);
        shelfBook2.setStatus(BookStatus.READING);
    }

    // --- createReadingSession ---

    @Test
    public void createReadingSession_validInput_createsSessionWithParticipants() {
        Session savedSession = new Session();
        savedSession.setId(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(shelfBookRepository.findById(1L)).willReturn(Optional.of(shelfBook1));
        given(shelfBookRepository.findById(2L)).willReturn(Optional.of(shelfBook2));
        given(sessionRepository.save(any(Session.class))).willReturn(savedSession);

        Session result = sessionService.createReadingSession(List.of(1L, 2L), List.of(1L, 2L));

        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any(Session.class));
        verify(sessionParticipantRepository, times(2)).save(any(SessionParticipant.class));
    }

    @Test
    public void createReadingSession_mismatchedListSizes_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.createReadingSession(List.of(1L, 2L), List.of(1L)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void createReadingSession_userNotFound_throws404() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.createReadingSession(List.of(99L), List.of(1L)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void createReadingSession_shelfBookNotFound_throws404() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(shelfBookRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.createReadingSession(List.of(1L), List.of(99L)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- startReadingSession ---

    @Test
    public void startReadingSession_validSession_setsStartTime() {
        Session session = new Session();
        session.setId(10L);

        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(sessionRepository.save(any(Session.class))).willReturn(session);

        Session result = sessionService.startReadingSession(10L);

        assertNotNull(result.getStartTime());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    public void startReadingSession_sessionNotFound_throws404() {
        given(sessionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.startReadingSession(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- endReadingSession ---

    @Test
    public void endReadingSession_validSession_setsEndTime() {
        Session session = new Session();
        session.setId(10L);

        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));

        Session result = sessionService.endReadingSession(10L);

        assertNotNull(result.getEndTime());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    public void endReadingSession_sessionNotFound_throws404() {
        given(sessionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.endReadingSession(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- joinSession ---

    @Test
    public void joinSession_validInput_savesParticipant_updatesBook_sendsNotifications() {
    
        // given
        Session session = new Session();
        session.setId(10L);
    
        SessionParticipant existingParticipant = new SessionParticipant();
        existingParticipant.setUser(user2);
    
        session.setParticipants(List.of(existingParticipant));
    
        Book book = new Book();
        book.setId("book-1");
        book.setName("Harry Potter");
        book.setPages(500L);
    
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(50L);
        shelfBook.setBook(book);
        shelfBook.setPagesRead(20L);
        shelfBook.setStatus(BookStatus.UNREAD);
    
        given(sessionRepository.findById(10L))
                .willReturn(Optional.of(session));
    
        given(userRepository.findById(1L))
                .willReturn(Optional.of(user1));
    
        given(shelfBookRepository.findById(50L))
                .willReturn(Optional.of(shelfBook));
    
        // when
        sessionService.joinSession(10L, 1L, 50L);
    
        // then
        verify(sessionParticipantRepository, times(1))
                .save(any(SessionParticipant.class));
    
        verify(sessionParticipantRepository, times(1))
                .flush();
    
        assertEquals(BookStatus.READING, shelfBook.getStatus());
    
        verify(notificationService, times(1))
                .sendSessionJoin(
                        eq(10L),
                        eq(1L),
                        eq(2L),
                        any(ShelfBookGetDTO.class)
                );
    
        verifyNoInteractions(activitiesService);
    }

@Test
public void joinSession_sessionNotFound_throws404() {

    given(sessionRepository.findById(99L)).willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.joinSession(99L, 1L, 1L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

@Test
public void joinSession_userNotFound_throws404() {

    Session session = new Session();
    session.setId(10L);

    given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
    given(userRepository.findById(99L)).willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.joinSession(10L, 99L, 1L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

@Test
public void joinSession_shelfBookNotFound_throws404() {

    Session session = new Session();
    session.setId(10L);

    given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
    given(userRepository.findById(1L)).willReturn(Optional.of(user1));
    given(shelfBookRepository.findById(99L)).willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.joinSession(10L, 1L, 99L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

// --- UpdatePagesRead ---

@Test
public void changeNumberOfPagesSession_validInput_updatesPagesAndSendsNotifications() {

    // given
    Session session = new Session();
    session.setId(10L);

    SessionParticipant participant = new SessionParticipant();
    participant.setUser(user1);
    participant.setSession(session);

    SessionParticipant otherParticipant = new SessionParticipant();
    otherParticipant.setUser(user2);

    session.setParticipants(List.of(participant, otherParticipant));

    given(sessionRepository.findById(10L))
            .willReturn(Optional.of(session));

    given(userRepository.findById(1L))
            .willReturn(Optional.of(user1));

    given(sessionParticipantRepository.findBySessionAndUser(session, user1))
            .willReturn(Optional.of(participant));

    // when
    sessionService.changeNumberOfPagesSession(10L, 1L, 150L);

    // then
    assertEquals(150L, participant.getPagesRead());

    verify(sessionParticipantRepository, times(1))
            .save(participant);

    verify(sessionParticipantRepository, times(1))
            .flush();

    verify(notificationService, times(1))
            .sendSessionChangePage(
                    10L,
                    1L,
                    2L,
                    150L
            );
}

@Test
public void changeNumberOfPagesSession_sessionNotFound_throws404() {

    given(sessionRepository.findById(99L))
            .willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.changeNumberOfPagesSession(99L, 1L, 100L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

@Test
public void changeNumberOfPagesSession_userNotFound_throws404() {

    Session session = new Session();
    session.setId(10L);

    given(sessionRepository.findById(10L))
            .willReturn(Optional.of(session));

    given(userRepository.findById(99L))
            .willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.changeNumberOfPagesSession(10L, 99L, 100L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

@Test
public void changeNumberOfPagesSession_participantNotFound_throws404() {

    Session session = new Session();
    session.setId(10L);

    given(sessionRepository.findById(10L))
            .willReturn(Optional.of(session));

    given(userRepository.findById(1L))
            .willReturn(Optional.of(user1));

    given(sessionParticipantRepository.findBySessionAndUser(session, user1))
            .willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.changeNumberOfPagesSession(10L, 1L, 100L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

// -- deleteOldSession ---
@Test
public void deleteOldSessions_deletesSessionsOlderThan30Days() {

    // given
    Session oldSession1 = new Session();
    oldSession1.setId(1L);
    oldSession1.setEndTime(LocalDateTime.now().minusDays(40));

    Session oldSession2 = new Session();
    oldSession2.setId(2L);
    oldSession2.setEndTime(LocalDateTime.now().minusDays(31));

    List<Session> oldSessions = List.of(oldSession1, oldSession2);

    given(sessionRepository.findByEndTimeBefore(any(LocalDateTime.class)))
            .willReturn(oldSessions);

    // when
    sessionService.deleteOldSessions();

    // then
    verify(sessionRepository, times(1))
            .deleteAll(oldSessions);
}

// --- gerLatestSession ---

@Test
public void getLatestSessionForUser_validAccess_returnsSession() {

    // given
    Session session = new Session();
    session.setId(10L);

    user1.setToken("valid-token");

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    null,
                    "valid-token"
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    given(userRepository.findById(1L))
            .willReturn(Optional.of(user1));

    given(sessionRepository.findLatestSessionForUser(1L))
            .willReturn(Optional.of(session));

    // when
    Session result = sessionService.getLatestSessionForUser(1L);

    // then
    assertNotNull(result);
    assertEquals(10L, result.getId());

    verify(sessionRepository, times(1))
            .findLatestSessionForUser(1L);
}

@Test
public void getLatestSessionForUser_userNotFound_throws404() {

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    null,
                    "token"
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    given(userRepository.findById(99L))
            .willReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.getLatestSessionForUser(99L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

@Test
public void getLatestSessionForUser_wrongToken_throws403() {

    // given
    user1.setToken("correct-token");

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    null,
                    "wrong-token"
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    given(userRepository.findById(1L))
            .willReturn(Optional.of(user1));

    // when
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.getLatestSessionForUser(1L));

    // then
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
}

    // --- leaveSession ---

    @Test
    public void leaveSession_validInput_setsLeftAtAndAwardsPoints() {
        Session session = new Session();
        session.setId(10L);
    
        SessionParticipant sp = new SessionParticipant();
        sp.setUser(user1);
        sp.setSession(session);
        sp.setJoinedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
    
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(50L);
        shelfBook.setBook(new Book());
    
        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(sessionParticipantRepository.findBySessionAndUser(session, user1))
                .willReturn(Optional.of(sp));
    
        given(shelfBookRepository.findById(50L))
                .willReturn(Optional.of(shelfBook));
    
        given(leaderboardRepository.findByUser(user1))
                .willReturn(null);
    
        sessionService.leaveSession(10L, 1L, 50L, 100L);
    
        assertNotNull(sp.getLeftAt());
        assertEquals(100L, sp.getPagesRead());
    
        verify(leaderboardRepository, times(1))
                .save(any(Leaderboard.class));
    }

    @Test
    public void leaveSession_existingLeaderboard_addsToExistingPoints() {
        Session session = new Session();
        session.setId(10L);
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(50L);
        SessionParticipant sp = new SessionParticipant();
        sp.setUser(user1);
        sp.setSession(session);
        sp.setJoinedAt(LocalDateTime.now().minusMinutes(40));

        Leaderboard existingLeaderboard = new Leaderboard();
        existingLeaderboard.setUser(user1);
        existingLeaderboard.setTotalPoints();
        existingLeaderboard.addReadingPoints(10L);

        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(shelfBookRepository.findById(50L)).willReturn(Optional.of(shelfBook));
        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(sessionParticipantRepository.findBySessionAndUser(session, user1)).willReturn(Optional.of(sp));
        given(leaderboardRepository.findByUser(user1)).willReturn(existingLeaderboard);

        sessionService.leaveSession(10L, 1L, 50L, 100L);

        // points from session: round((100 * 0.1) + (40 * 0.05)) = round(10 + 2) = 12
        // total reading points: 10 (existing) + 12 (new) = 22
        assertEquals(22L, existingLeaderboard.getReadingPoints());
        verify(leaderboardRepository, times(1)).save(existingLeaderboard);
    }

    @Test
    public void leaveSession_sessionNotFound_throws404() {
        given(sessionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.leaveSession(99L, 1L, 10L,10L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void leaveSession_userNotFound_throws404() {
        Session session = new Session();
        session.setId(10L);

        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.leaveSession(10L, 99L, 10L, 10L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void leaveSession_participantNotInSession_throws404() {
        Session session = new Session();
        session.setId(10L);

        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(sessionParticipantRepository.findBySessionAndUser(session, user1)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.leaveSession(10L, 1L, 10L,10L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
