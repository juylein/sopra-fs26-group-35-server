package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.ReviewService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

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
    public void editReview_validInput_returns204() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setToken("token");

        ReviewPostDTO reviewPostDTO = new ReviewPostDTO();
        reviewPostDTO.setRating(4);
        reviewPostDTO.setReview("Updated review");

        given(userRepository.findByToken("token")).willReturn(user);

        mockMvc.perform(put("/users/1/reviews/1")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(reviewPostDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void editReview_reviewNotFound_returns404() throws Exception {
        User user = new User();
        user.setToken("token");

        ReviewPostDTO reviewPostDTO = new ReviewPostDTO();
        reviewPostDTO.setRating(4);
        reviewPostDTO.setReview("Updated review");

        given(userRepository.findByToken("token")).willReturn(user);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"))
                .when(reviewService).editReview(any(), any(), any());

        mockMvc.perform(put("/users/1/reviews/99")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(reviewPostDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void editReview_notOwner_returns403() throws Exception {
        User user = new User();
        user.setToken("token");

        ReviewPostDTO reviewPostDTO = new ReviewPostDTO();
        reviewPostDTO.setRating(4);
        reviewPostDTO.setReview("Updated review");

        given(userRepository.findByToken("token")).willReturn(user);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this review"))
                .when(reviewService).editReview(any(), any(), any());

        mockMvc.perform(put("/users/2/reviews/1")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(reviewPostDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void editReview_noAuth_returns401() throws Exception {
        ReviewPostDTO reviewPostDTO = new ReviewPostDTO();
        reviewPostDTO.setRating(4);
        reviewPostDTO.setReview("Updated review");

        mockMvc.perform(put("/users/1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(reviewPostDTO)))
                .andExpect(status().isUnauthorized());
    }
}