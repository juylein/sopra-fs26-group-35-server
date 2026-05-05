package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.StatsService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@Import(SecurityConfig.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void getPagesRead_validUser_returnsValue() throws Exception {
        given(statsService.pagesRead(1L)).willReturn(142L);

        mockMvc.perform(get("/users/1/pagesRead").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagesRead", is(142)));
    }

    @Test
    @WithMockUser
    void getPagesRead_userNotFound_returns404() throws Exception {
        given(statsService.pagesRead(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/99/pagesRead").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPagesRead_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/users/1/pagesRead").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getBooksRead_validUser_returnsValue() throws Exception {
        given(statsService.booksRead(1L)).willReturn(5L);

        mockMvc.perform(get("/users/1/booksRead").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booksRead", is(5)));
    }

    @Test
    @WithMockUser
    void getBooksRead_userNotFound_returns404() throws Exception {
        given(statsService.booksRead(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/99/booksRead").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getTotalPoints_validUser_returnsValue() throws Exception {
        given(statsService.totalPoints(1L)).willReturn(200L);

        mockMvc.perform(get("/users/1/totalPoints").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(200)));
    }

    @Test
    @WithMockUser
    void getTotalPoints_noLeaderboardEntry_returnsZero() throws Exception {
        given(statsService.totalPoints(1L)).willReturn(0L);

        mockMvc.perform(get("/users/1/totalPoints").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(0)));
    }

    @Test
    @WithMockUser
    void getTotalReadingPoints_validUser_returnsValue() throws Exception {
        given(statsService.readingPoints(1L)).willReturn(150L);

        mockMvc.perform(get("/users/1/totalReadingPoints").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReadingPoints", is(150)));
    }

    @Test
    @WithMockUser
    void getTotalQuizzPoints_validUser_returnsValue() throws Exception {
        given(statsService.quizzPoints(1L)).willReturn(50L);

        mockMvc.perform(get("/users/1/totalQuizzPoints").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuizzPoints", is(50)));
    }
}
