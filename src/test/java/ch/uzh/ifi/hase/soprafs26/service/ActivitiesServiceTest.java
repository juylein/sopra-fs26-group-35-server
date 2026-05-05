package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ActivitiesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendshipsRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ActivitiesServiceTest {

    @Mock
    private ActivitiesRepository activitiesRepository;

    @Mock
    private FriendshipsRepository friendshipsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ActivitiesService activitiesService;

    private User testUser;
    private User friendUser;
    private Book testBook;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        friendUser = new User();
        friendUser.setId(2L);
        friendUser.setUsername("friendUser");

        testBook = new Book();
        testBook.setId("book123");
        testBook.setName("Test Book");
    }

    @Test
    void getAllActivities_includesSelfAndFriends() {
        Friendships friendship = new Friendships();
        friendship.setUserA(testUser);
        friendship.setUserB(friendUser);

        Activities a1 = new Activities();
        a1.setUser(testUser);
        a1.setBook(testBook);
        a1.setActions("started reading");

        Activities a2 = new Activities();
        a2.setUser(friendUser);
        a2.setBook(testBook);
        a2.setActions("finished reading");

        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of(friendship));
        when(activitiesRepository.findAllByUserIn(any())).thenReturn(List.of(a1, a2));

        List<Activities> result = activitiesService.getAllActivities(testUser);

        assertEquals(2, result.size());
    }

    @Test
    void getAllActivities_noFriends_returnsSelfOnly() {
        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of());

        Activities a = new Activities();
        a.setUser(testUser);
        a.setBook(testBook);
        a.setActions("started reading");

        when(activitiesRepository.findAllByUserIn(List.of(testUser))).thenReturn(List.of(a));

        List<Activities> result = activitiesService.getAllActivities(testUser);

        assertEquals(1, result.size());
    }

    @Test
    void getActivitiesByFriend_isFriend_returnsActivities() {
        Friendships friendship = new Friendships();
        friendship.setUserA(testUser);
        friendship.setUserB(friendUser);

        Activities a = new Activities();
        a.setUser(friendUser);
        a.setBook(testBook);
        a.setActions("finished reading");

        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of(friendship));
        when(userService.getUserById(2L)).thenReturn(friendUser);
        when(activitiesRepository.findAllByUser(friendUser)).thenReturn(List.of(a));

        List<Activities> result = activitiesService.getActivitiesByFriend(testUser, 2L);

        assertEquals(1, result.size());
        assertEquals("finished reading", result.get(0).getActions());
    }

    @Test
    void getActivitiesByFriend_notFriend_throwsException() {
        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> activitiesService.getActivitiesByFriend(testUser, 2L));
    }

    @Test
    void addActivity_statusFinished_setsCorrectAction() {
        when(activitiesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Activities result = activitiesService.addActivity(testUser, BookStatus.FINISHED, testBook);

        assertNotNull(result);
        assertEquals("finished reading", result.getActions());
        assertEquals(testUser, result.getUser());
        assertEquals(testBook, result.getBook());
    }

    @Test
    void addActivity_statusReading_setsCorrectAction() {
        when(activitiesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Activities result = activitiesService.addActivity(testUser, BookStatus.READING, testBook);

        assertNotNull(result);
        assertEquals("started reading", result.getActions());
    }

    @Test
    void addActivity_statusUnread_returnsNull() {
        Activities result = activitiesService.addActivity(testUser, BookStatus.UNREAD, testBook);

        assertNull(result);
        verify(activitiesRepository, never()).save(any());
    }
}
