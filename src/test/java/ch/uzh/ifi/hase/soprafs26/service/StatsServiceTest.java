package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionParticipantRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SessionParticipantRepository sessionParticipantRepository;
    @Mock private ShelfBookRepository shelfBookRepository;
    @Mock private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private StatsService statsService;

    private User testUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }

    // --- pagesRead ---

    @Test
    void pagesRead_validUser_returnsSumOfPages() {
        SessionParticipant sp1 = new SessionParticipant();
        sp1.setPagesRead(30L);
        SessionParticipant sp2 = new SessionParticipant();
        sp2.setPagesRead(70L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionParticipantRepository.findByUser(testUser)).thenReturn(List.of(sp1, sp2));

        assertEquals(100L, statsService.pagesRead(1L));
    }

    @Test
    void pagesRead_someSessionsNullPages_skipsNulls() {
        SessionParticipant sp1 = new SessionParticipant();
        sp1.setPagesRead(50L);
        SessionParticipant sp2 = new SessionParticipant();
        sp2.setPagesRead(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionParticipantRepository.findByUser(testUser)).thenReturn(List.of(sp1, sp2));

        assertEquals(50L, statsService.pagesRead(1L));
    }

    @Test
    void pagesRead_noSessions_returnsZero() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionParticipantRepository.findByUser(testUser)).thenReturn(List.of());

        assertEquals(0L, statsService.pagesRead(1L));
    }

    @Test
    void pagesRead_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> statsService.pagesRead(99L));
    }

    // --- booksRead ---

    @Test
    void booksRead_validUser_returnsCount() {
        when(shelfBookRepository.countByShelf_OwnerIdAndStatus(1L, BookStatus.FINISHED)).thenReturn(3L);

        assertEquals(3L, statsService.booksRead(1L));
    }

    @Test
    void booksRead_noFinishedBooks_returnsZero() {
        when(shelfBookRepository.countByShelf_OwnerIdAndStatus(1L, BookStatus.FINISHED)).thenReturn(0L);

        assertEquals(0L, statsService.booksRead(1L));
    }

    // --- totalPoints ---

    @Test
    void totalPoints_validUser_returnsPoints() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setTotalPoints();
        leaderboard.addReadingPoints(80L);
        leaderboard.addQuizzPoints(20L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(leaderboard);

        assertEquals(100L, statsService.totalPoints(1L));
    }

    @Test
    void totalPoints_noLeaderboardEntry_returnsZero() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(null);

        assertEquals(0L, statsService.totalPoints(1L));
    }

    @Test
    void totalPoints_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> statsService.totalPoints(99L));
    }

    // --- readingPoints ---

    @Test
    void readingPoints_validUser_returnsPoints() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setTotalPoints();
        leaderboard.addReadingPoints(60L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(leaderboard);

        assertEquals(60L, statsService.readingPoints(1L));
    }

    @Test
    void readingPoints_noLeaderboardEntry_returnsZero() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(null);

        assertEquals(0L, statsService.readingPoints(1L));
    }

    // --- quizzPoints ---

    @Test
    void quizzPoints_validUser_returnsPoints() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setTotalPoints();
        leaderboard.addQuizzPoints(40L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(leaderboard);

        assertEquals(40L, statsService.quizzPoints(1L));
    }

    @Test
    void quizzPoints_noLeaderboardEntry_returnsZero() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(leaderboardRepository.findByUser(testUser)).thenReturn(null);

        assertEquals(0L, statsService.quizzPoints(1L));
    }
}
