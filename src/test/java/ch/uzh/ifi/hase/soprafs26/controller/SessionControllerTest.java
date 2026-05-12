package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionJoinPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionLeavePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionReadPagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionSendNotificationPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@Import(SecurityConfig.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }

    // --- POST /users/{userId}/sessions/sessions ---

    @Test
    @WithMockUser
    public void createReadingSession_validInput_returnsCreated() throws Exception {
        Session session = new Session();
        session.setId(10L);

        SessionParticipantPostDTO dto = new SessionParticipantPostDTO();
        dto.setUserId(1L);
        dto.setShelfBookId(1L);

        given(sessionService.createReadingSession(Mockito.any(), Mockito.any())).willReturn(session);

        mockMvc.perform(post("/users/1/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of(dto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    public void createReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/users/1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void createReadingSession_userNotFound_returns404() throws Exception {
        SessionParticipantPostDTO dto = new SessionParticipantPostDTO();
        dto.setUserId(99L);
        dto.setShelfBookId(1L);

        given(sessionService.createReadingSession(Mockito.any(), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/users/1/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of(dto))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void createReadingSession_mismatchedParticipants_returns400() throws Exception {
        SessionParticipantPostDTO dto = new SessionParticipantPostDTO();
        dto.setUserId(1L);
        dto.setShelfBookId(1L);

        given(sessionService.createReadingSession(Mockito.any(), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each participant must have exactly one book"));

        mockMvc.perform(post("/users/1/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of(dto))))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /users/{userId}/sessions/sessions/{sessionId}/started ---

    @Test
    @WithMockUser
    public void startReadingSession_validInput_returnsOk() throws Exception {
        Session session = new Session();
        session.setId(10L);

        given(sessionService.startReadingSession(10L)).willReturn(session);

        mockMvc.perform(put("/users/1/sessions/10/started")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    @WithMockUser
    public void startReadingSession_sessionNotFound_returns404() throws Exception {
        given(sessionService.startReadingSession(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        mockMvc.perform(put("/users/1/sessions/99/started")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(put("/users/1/sessions/10/started")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT /users/{userId}/sessions/sessions/{sessionId}/ended ---

    @Test
    @WithMockUser
    public void endReadingSession_validInput_returnsOk() throws Exception {
        Session session = new Session();
        session.setId(10L);

        given(sessionService.endReadingSession(10L)).willReturn(session);

        mockMvc.perform(put("/users/1/sessions/10/ended")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    @WithMockUser
    public void endReadingSession_sessionNotFound_returns404() throws Exception {
        given(sessionService.endReadingSession(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        mockMvc.perform(put("/users/1/sessions/99/ended")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void endReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(put("/users/1/sessions/10/ended")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    //PUT ...joined
    @Test
    @WithMockUser
    public void joinSession_validInput_returnsNoContent() throws Exception {
        SessionJoinPutDTO sessionJoinPutDTO = new SessionJoinPutDTO();
        sessionJoinPutDTO.setShelfBookId(1L);

        mockMvc.perform(put("/users/1/sessions/1/joined")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionJoinPutDTO)))
                .andExpect(status().isNoContent());
    }

    //PUT ...left
    @Test
    @WithMockUser
    public void leaveSession_validInput_returnsNoContent() throws Exception {
        SessionLeavePostDTO sessionLeavePostDTO = new SessionLeavePostDTO();
        sessionLeavePostDTO.setShelfBookId(1L);
        sessionLeavePostDTO.setPagesRead(50L);

        mockMvc.perform(put("/users/1/sessions/1/left")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionLeavePostDTO)))
                .andExpect(status().isNoContent());
        }

    //GET ...latest
    @Test
    @WithMockUser
    public void getLatestSession_validUser_returnsSession() throws Exception {
        User user = new User();
        user.setId(1L);

        Book book = new Book();
        book.setId("abc123");
        book.setName("Dune");

        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(1L);
        shelfBook.setBook(book);
        shelfBook.setPagesRead(50L);

        SessionParticipant participant = new SessionParticipant();
        participant.setUser(user);
        participant.setShelfBook(shelfBook);
        participant.setPagesRead(50L);

        Session session = new Session();
        session.setId(10L);
        session.setParticipants(List.of(participant));

        given(sessionService.getLatestSessionForUser(1L)).willReturn(session);

        mockMvc.perform(get("/users/1/sessions/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.bookTitle", is("Dune")))
                .andExpect(jsonPath("$.shelfBookId", is(1)))
                .andExpect(jsonPath("$.pagesRead", is(50)));;
        }

    //PUT ...readPage
    @Test
    @WithMockUser
    public void changeNumberOfPagesSession_validInput_returnsCreated() throws Exception {
        SessionReadPagePutDTO sessionReadPagePutDTO = new SessionReadPagePutDTO();
        sessionReadPagePutDTO.setNumberOfPages(100L);

        mockMvc.perform(put("/users/1/sessions/1/readPage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionReadPagePutDTO)))
                .andExpect(status().isCreated());
        }

    //POST ...invitations
    @Test
    @WithMockUser
    public void sendSessionInvitations_validInput_returnsCreated() throws Exception {
        Session session = new Session();
        session.setId(1L);

        SessionSendNotificationPostDTO sessionSendNotificationPostDTO = new SessionSendNotificationPostDTO();
        sessionSendNotificationPostDTO.setShelfBookId(1L);
        sessionSendNotificationPostDTO.setParticipantIds(List.of(2L));

        given(sessionService.createReadingSession(Mockito.any(), Mockito.any())).willReturn(session);

        mockMvc.perform(post("/users/1/sessions/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionSendNotificationPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
        }

    //GET ...participants
    @Test
    @WithMockUser
    public void getSessionParticipants_validSession_returnsParticipants() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Book book = new Book();
        book.setId("abc123");
        book.setName("Dune");

        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(1L);
        shelfBook.setBook(book);
        shelfBook.setPagesRead(50L);

        SessionParticipant participant = new SessionParticipant();
        participant.setUser(user);
        participant.setShelfBook(shelfBook);

        given(sessionService.getSessionParticipants(1L)).willReturn(List.of(participant));

        mockMvc.perform(get("/users/1/sessions/1/participants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].shelfBookId", is(1)))
                .andExpect(jsonPath("$[0].pagesRead", is(50)))
                .andExpect(jsonPath("$[0].user.username", is("testuser")))
                .andExpect(jsonPath("$[0].book.name", is("Dune")));
        }
}
