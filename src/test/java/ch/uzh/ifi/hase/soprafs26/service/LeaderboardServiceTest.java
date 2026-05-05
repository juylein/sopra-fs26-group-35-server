package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private User userA;
    private User userB;
    private Leaderboard entryA;
    private Leaderboard entryB;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userA = new User();
        userA.setId(1L);
        userA.setUsername("userA");

        userB = new User();
        userB.setId(2L);
        userB.setUsername("userB");

        entryA = new Leaderboard();
        entryA.setId(1L);
        entryA.setUser(userA);
        entryA.setTotalPoints();
        entryA.addReadingPoints(100L);

        entryB = new Leaderboard();
        entryB.setId(2L);
        entryB.setUser(userB);
        entryB.setTotalPoints();
        entryB.addReadingPoints(50L);
    }

    @Test
    void getLeaderboards_noLimit_returnsAll() {
        when(leaderboardRepository.findAll()).thenReturn(List.of(entryA, entryB));

        List<Leaderboard> result = leaderboardService.getLeaderboards(null);

        assertEquals(2, result.size());
        verify(leaderboardRepository, times(1)).findAll();
        verify(leaderboardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getLeaderboards_withLimit_returnsTopN() {
        when(leaderboardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entryA)));

        List<Leaderboard> result = leaderboardService.getLeaderboards(1L);

        assertEquals(1, result.size());
        assertEquals(userA, result.get(0).getUser());
        verify(leaderboardRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getLeaderboards_emptyBoard_returnsEmptyList() {
        when(leaderboardRepository.findAll()).thenReturn(List.of());

        List<Leaderboard> result = leaderboardService.getLeaderboards(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLeaderboardByUser_validUser_returnsEntry() {
        when(leaderboardRepository.findByUser(userA)).thenReturn(entryA);

        Leaderboard result = leaderboardService.getLeaderboardByUser(userA);

        assertNotNull(result);
        assertEquals(userA, result.getUser());
        assertEquals(100L, result.getTotalPoints());
    }

    @Test
    void getLeaderboardByUser_noEntry_returnsNull() {
        when(leaderboardRepository.findByUser(userA)).thenReturn(null);

        Leaderboard result = leaderboardService.getLeaderboardByUser(userA);

        assertNull(result);
    }
}
