package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.NotificationEventType;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.Notifications;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.NotificationEvent;
import ch.uzh.ifi.hase.soprafs26.rest.dto.NotificationGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfBookGetDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Notification Service
 * This class is the "worker" and responsible for all functionality related to
 * notifications
 * (e.g., it creates, fetches, and marks as read). The result will be passed
 * back to the caller.
 */
@Service
@Transactional
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendSessionInvite(Long sessionId, Long senderId, Long recipientId)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", senderId);
        payload.put("to", recipientId);
        payload.put("sessionId", sessionId);
        NotificationEvent event = new NotificationEvent(NotificationEventType.SHARED_SESSION_START, payload);
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, event);
    }

    public void sendSessionJoin(Long sessionId, Long senderId, Long recipientId, ShelfBookGetDTO shelfBook)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", senderId);
        payload.put("to", recipientId);
        payload.put("sessionId", sessionId);
        payload.put("shelfBook", shelfBook);
        NotificationEvent event = new NotificationEvent(NotificationEventType.SHARED_SESSION_JOIN, payload);
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, event);
    }

    public void sendSessionQuit(Long sessionId, Long senderId, Long recipientId)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", senderId);
        payload.put("to", recipientId);
        payload.put("sessionId", sessionId);
        NotificationEvent event = new NotificationEvent(NotificationEventType.SHARED_SESSION_QUIT, payload);
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, event);
    }

    public void sendSessionChangePage(Long sessionId, Long senderId, Long recipientId, Long numberOfPages)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", senderId);
        payload.put("to", recipientId);
        payload.put("sessionId", sessionId);
        payload.put("numberOfPages", numberOfPages);
        NotificationEvent event = new NotificationEvent(NotificationEventType.SHARED_SESSION_PAGE, payload);
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, event);
    }

    public Notifications createNotification(Long recipientId, NotificationType type, String message, Long referenceId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + recipientId + " not found"
                ));

        Notifications notification = new Notifications();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);
        notificationRepository.flush();

        log.debug("Created Notification for User: {}", recipient);
        return notification;
    }

    public List<Notifications> getNotifications(Long userId) {
        checkIfUserExists(userId);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        checkIfUserExists(userId);
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        checkIfUserExists(userId);

        List<Notifications> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);

        for (Notifications notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        }

        notificationRepository.saveAll(notifications);
        notificationRepository.flush();

        log.debug("Marked all notifications as read for userId: {}", userId);
    }

    public void deleteNotification(Long userId, Long notificationId) {
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        notificationRepository.delete(notification);
    }

    /**
     * This is a helper method that will check whether a user with the given id
     * exists. The method will do nothing if the user is found and throw an error
     * otherwise.
     *
     * @param userId
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + userId + " not found"
                ));
    }
}