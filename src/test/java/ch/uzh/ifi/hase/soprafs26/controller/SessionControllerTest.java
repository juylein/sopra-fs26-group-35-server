package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @WithMockUser
    public void createSession_validInput_returnsCreated() throws Exception {
        User host = new User();
        host.setId(1L);

        Book book = new Book();
        book.setId("abc123");

        Session session = new Session();
        session.setId(1L);
        session.setHost(host);
        session.setBook(book);
        session.setStartTime(LocalDateTime.now());

        SessionPostDTO sessionPostDTO = new SessionPostDTO();
        sessionPostDTO.setBookId("abc123");

        given(sessionService.createSession(1L, "abc123")).willReturn(session);

        mockMvc.perform(post("/users/1/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.hostId", is(1)))
                .andExpect(jsonPath("$.bookId", is("abc123")));
    }


    @Test
    @WithMockUser
    public void createSession_userNotFound_returns404() throws Exception {
        SessionPostDTO sessionPostDTO = new SessionPostDTO();
        sessionPostDTO.setBookId("abc123");

        given(sessionService.createSession(99L, "abc123"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/users/99/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionPostDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void createSession_forbidden_returns403() throws Exception {
        SessionPostDTO sessionPostDTO = new SessionPostDTO();
        sessionPostDTO.setBookId("abc123");

        given(sessionService.createSession(2L, "abc123"))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have permission"));

        mockMvc.perform(post("/users/2/sessions")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sessionPostDTO)))
                .andExpect(status().isForbidden());
    }
    
}
