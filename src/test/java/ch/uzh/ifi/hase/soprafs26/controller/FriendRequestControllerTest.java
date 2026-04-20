package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.AuthTokenFilter;
import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.FriendService;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendRequestController.class)
@Import(SecurityConfig.class)
public class FriendRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(invocation -> {
            ((FilterChain) invocation.getArgument(2))
                    .doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(authTokenFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    @WithMockUser
    public void sendFriendRequest_validInput_returnsCreated() throws Exception {
        User requester = new User();
        requester.setId(1L);
        requester.setUsername("requester");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(10L);
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.PENDING);

        given(friendService.createFriendRequest(1L, 2L)).willReturn(friendRequest);

        mockMvc.perform(post("/users/1/friend-requests/2")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.requesterId", is(1)))
                .andExpect(jsonPath("$.recipientId", is(2)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser
    public void getIncomingFriendRequests_returnsList() throws Exception {
        User requester = new User();
        requester.setId(1L);
        requester.setUsername("requester");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(11L);
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.PENDING);

        given(friendService.getIncomingFriendRequests(2L)).willReturn(List.of(friendRequest));

        mockMvc.perform(get("/users/2/friend-requests/incoming")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(11)))
                .andExpect(jsonPath("$[0].recipientId", is(2)));
    }

    @Test
    @WithMockUser
    public void getSentRequests_returnsList() throws Exception {
        User requester = new User();
        requester.setId(1L);
        requester.setUsername("requester");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(12L);
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.PENDING);

        given(friendService.getSentFriendRequests(1L)).willReturn(List.of(friendRequest));

        mockMvc.perform(get("/users/1/friend-requests/sent")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(12)))
                .andExpect(jsonPath("$[0].requesterId", is(1)));
    }

    @Test
    @WithMockUser
    public void acceptFriendRequest_validRequest_returnsOk() throws Exception {
        User requester = new User();
        requester.setId(1L);
        requester.setUsername("requester");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(20L);
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);

        given(friendService.acceptFriendRequest(20L, 2L)).willReturn(friendRequest);

        mockMvc.perform(put("/friend-requests/20/accept")
                        .param("userId", "2")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.id", is(20)));
    }

    @Test
    @WithMockUser
    public void rejectFriendRequest_validRequest_returnsOk() throws Exception {
        User requester = new User();
        requester.setId(21L);
        requester.setUsername("requester");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(21L);
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.REJECTED);

        given(friendService.rejectFriendRequest(21L, 2L)).willReturn(friendRequest);

        mockMvc.perform(put("/friend-requests/21/reject")
                        .param("userId", "2")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.id", is(21)));
    }

    @Test
    @WithMockUser
    public void sendFriendRequest_missingUser_returns404() throws Exception {
        given(friendService.createFriendRequest(99L, 2L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/users/99/friend-requests/2")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
