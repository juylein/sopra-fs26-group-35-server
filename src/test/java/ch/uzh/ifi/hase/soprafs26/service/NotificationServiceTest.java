package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.Notifications;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }

    @Test
    void createNotification_validInput_returnsNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Notifications saved = new Notifications();
        saved.setId(10L);
        saved.setRecipient(testUser);
        saved.setType(NotificationType.FRIEND_REQUEST);
        saved.setMessage("You have a new friend request");
        saved.setReferenceId(5L);
        saved.setRead(false);
        saved.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any())).thenReturn(saved);

        Notifications result = notificationService.createNotification(
                1L, NotificationType.FRIEND_REQUEST, "You have a new friend request", 5L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(NotificationType.FRIEND_REQUEST, result.getType());
        assertFalse(result.isRead());
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void createNotification_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                notificationService.createNotification(99L, NotificationType.FRIEND_REQUEST, "msg", null));
    }

    @Test
    void getNotifications_validUser_returnsList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Notifications n1 = new Notifications();
        n1.setId(1L);
        n1.setRecipient(testUser);
        n1.setMessage("msg1");
        n1.setType(NotificationType.FRIEND_ACTIVITY);
        n1.setRead(false);
        n1.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n1));

        List<Notifications> result = notificationService.getNotifications(1L);

        assertEquals(1, result.size());
        assertEquals("msg1", result.get(0).getMessage());
    }

    @Test
    void getNotifications_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.getNotifications(99L));
    }

    @Test
    void getUnreadCount_validUser_returnsCount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(3L, count);
    }

    @Test
    void getUnreadCount_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.getUnreadCount(99L));
    }

    @Test
    void markAllRead_validUser_setsAllToRead() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Notifications unread = new Notifications();
        unread.setId(1L);
        unread.setRead(false);
        unread.setRecipient(testUser);
        unread.setMessage("unread");
        unread.setType(NotificationType.FRIEND_REQUEST);
        unread.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(unread));

        notificationService.markAllRead(1L);

        assertTrue(unread.isRead());
        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    void markAllRead_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.markAllRead(99L));
    }

    @Test
    void markAllRead_alreadyReadNotifications_remainsRead() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Notifications alreadyRead = new Notifications();
        alreadyRead.setId(2L);
        alreadyRead.setRead(true);
        alreadyRead.setRecipient(testUser);
        alreadyRead.setMessage("already read");
        alreadyRead.setType(NotificationType.QUIZ_CHALLENGE);
        alreadyRead.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(alreadyRead));

        notificationService.markAllRead(1L);

        assertTrue(alreadyRead.isRead());
    }
}
