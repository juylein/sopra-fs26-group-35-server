package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
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
import static org.mockito.BDDMockito.given;
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

        mockMvc.perform(post("/users/1/sessions/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of(dto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    public void createReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/users/1/sessions/sessions")
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

        mockMvc.perform(post("/users/1/sessions/sessions")
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

        mockMvc.perform(post("/users/1/sessions/sessions")
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

        mockMvc.perform(put("/users/1/sessions/sessions/10/started")
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

        mockMvc.perform(put("/users/1/sessions/sessions/99/started")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(put("/users/1/sessions/sessions/10/started")
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

        mockMvc.perform(put("/users/1/sessions/sessions/10/ended")
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

        mockMvc.perform(put("/users/1/sessions/sessions/99/ended")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void endReadingSession_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(put("/users/1/sessions/sessions/10/ended")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
