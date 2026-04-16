package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Notifications;
import ch.uzh.ifi.hase.soprafs26.rest.dto.NotificationGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/users/{userId}/notifications")
    @ResponseStatus(HttpStatus.OK)
    public List<NotificationGetDTO> getNotifications(@PathVariable Long userId) {
        List<Notifications> notifications = notificationService.getNotifications(userId);
        return notifications.stream()
                .map(DTOMapper.INSTANCE::convertNotificationEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/notifications/unread-count")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Map.of("unreadCount", count);
    }

    @PutMapping("/users/{userId}/notifications/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@PathVariable Long userId) {
        notificationService.markAllRead(userId);
    }
}