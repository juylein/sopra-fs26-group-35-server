package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.Notifications;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void getNotifications_validUser_returnsList() throws Exception {
        User recipient = new User();
        recipient.setId(1L);
        recipient.setUsername("testUser");

        Notifications n = new Notifications();
        n.setId(10L);
        n.setRecipient(recipient);
        n.setType(NotificationType.FRIEND_REQUEST);
        n.setMessage("You have a friend request");
        n.setReferenceId(5L);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));

        given(notificationService.getNotifications(1L)).willReturn(List.of(n));

        mockMvc.perform(get("/users/1/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].type", is("FRIEND_REQUEST")))
                .andExpect(jsonPath("$[0].message", is("You have a friend request")))
                .andExpect(jsonPath("$[0].read", is(false)));
    }

    @Test
    @WithMockUser
    void getNotifications_userNotFound_returns404() throws Exception {
        given(notificationService.getNotifications(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/99/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUnreadCount_validUser_returnsCount() throws Exception {
        given(notificationService.getUnreadCount(1L)).willReturn(3L);

        mockMvc.perform(get("/users/1/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(3)));
    }

    @Test
    @WithMockUser
    void getUnreadCount_userNotFound_returns404() throws Exception {
        given(notificationService.getUnreadCount(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/99/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void markAllRead_validUser_returns204() throws Exception {
        doNothing().when(notificationService).markAllRead(1L);

        mockMvc.perform(put("/users/1/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void markAllRead_userNotFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(notificationService).markAllRead(99L);

        mockMvc.perform(put("/users/99/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotifications_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/users/1/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
