package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.LibraryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    public void getLibrary_validToken_returnsLibrary() throws Exception {
        // given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setToken("valid-token");
        user.setStatus(UserStatus.ONLINE);

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("To Read");
        shelf.setShared(false);

        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(libraryService.getLibrary(user)).willReturn(List.of(shelf));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/library", userId)
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("To Read")))
                .andExpect(jsonPath("$[0].shared", is(false)));
    }

    @Test
    public void getLibrary_invalidToken_returnsUnauthorized() throws Exception {
        // given
        Long userId = 1L;
        given(userRepository.findByToken("invalid-token")).willReturn(null);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/library", userId)
                .header("Authorization", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getLibrary_wrongUserId_returnsForbidden() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("valid-token");

        given(userRepository.findByToken("valid-token")).willReturn(user);

        // when 
        MockHttpServletRequestBuilder getRequest = get("/users/99/library")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void addShelf_validInput_returnsCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setToken("valid-token");
        user.setStatus(UserStatus.ONLINE);

        Shelf shelf = new Shelf();
        shelf.setId(2L);
        shelf.setName("Favorites");
        shelf.setShared(false);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(libraryService.addShelf(user, "Favorites")).willReturn(shelf);

        // when
        MockHttpServletRequestBuilder postRequest = post("/users/1/library/shelves")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Favorites")))
                .andExpect(jsonPath("$.shared", is(false)));
    }

    @Test
    public void addShelf_invalidToken_returnsUnauthorized() throws Exception {
        // given
        given(userRepository.findByToken("invalid-token")).willReturn(null);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        // when
        MockHttpServletRequestBuilder postRequest = post("/users/1/library/shelves")
                .header("Authorization", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void addShelf_wrongUserId_returnsForbidden() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("valid-token");

        given(userRepository.findByToken("valid-token")).willReturn(user);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        // when 
        MockHttpServletRequestBuilder postRequest = post("/users/99/library/shelves")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
