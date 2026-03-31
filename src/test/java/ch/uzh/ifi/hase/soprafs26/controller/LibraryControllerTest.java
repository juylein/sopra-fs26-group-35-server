package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.LibraryService;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@Import(SecurityConfig.class)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

    private User mockUser(Long id, String token) {
        User user = new User();
        user.setId(id);
        user.setUsername("testUser");
        user.setToken(token);
        user.setStatus(UserStatus.ONLINE);
        return user;
    }

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
    public void getLibrary_validToken_returnsLibrary() throws Exception {
        User user = mockUser(1L, "valid-token");

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("To Read");
        shelf.setShared(false);

        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(libraryService.getLibrary(user)).willReturn(List.of(shelf));

        MockHttpServletRequestBuilder getRequest = get("/users/1/library")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("To Read")))
                .andExpect(jsonPath("$[0].shared", is(false)));
    }

    @Test
    public void getLibrary_invalidAuthentication_returns401() throws Exception {
        MockHttpServletRequestBuilder getRequest = get("/users/1/library")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getLibrary_invalidToken_returns401() throws Exception {
        given(userRepository.findByToken("invalid-token")).willReturn(null);

        MockHttpServletRequestBuilder getRequest = get("/users/1/library")
                .header("Authorization", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getLibrary_wrongUserId_returns403() throws Exception {
        User user = mockUser(1L, "valid-token");
        given(userRepository.findByToken("valid-token")).willReturn(user);

        MockHttpServletRequestBuilder getRequest = get("/users/99/library")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void addShelf_validInput_returnsCreated() throws Exception {
        User user = mockUser(1L, "valid-token");

        Shelf shelf = new Shelf();
        shelf.setId(2L);
        shelf.setName("Favorites");
        shelf.setShared(false);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(libraryService.addShelf(user, "Favorites")).willReturn(shelf);

        MockHttpServletRequestBuilder postRequest = post("/users/1/library/shelves")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Favorites")))
                .andExpect(jsonPath("$.shared", is(false)));
    }

    @Test
    public void addShelf_invalidAuthentication_returns401() throws Exception {
        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        MockHttpServletRequestBuilder postRequest = post("/users/1/library/shelves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addShelf_invalidToken_returns401() throws Exception {
        given(userRepository.findByToken("invalid-token")).willReturn(null);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        MockHttpServletRequestBuilder postRequest = post("/users/1/library/shelves")
                .header("Authorization", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addShelf_wrongUserId_returns403() throws Exception {
        User user = mockUser(1L, "valid-token");
        given(userRepository.findByToken("valid-token")).willReturn(user);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        MockHttpServletRequestBuilder postRequest = post("/users/99/library/shelves")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(shelfPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void addBookToShelf_validInput_returnsCreated() throws Exception {
        User user = mockUser(1L, "valid-token");

        BookPostDTO bookPostDTO = new BookPostDTO();
        bookPostDTO.setGoogleId("abc123");
        bookPostDTO.setName("Dune");
        bookPostDTO.setAuthors(List.of("Frank Herbert"));
        bookPostDTO.setPages(412L);
        bookPostDTO.setReleaseYear(1965);
        bookPostDTO.setGenre("Science Fiction");
        bookPostDTO.setDescription("Description");

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("My Shelf");
        shelf.setShared(false);

        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(libraryService.addBookToShelf(Mockito.eq(user), Mockito.eq(1L), Mockito.any()))
                .willReturn(shelf);

        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("My Shelf")));
    }

    @Test
    public void addBookToShelf_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new BookPostDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addBookToShelf_invalidToken_returns401() throws Exception {
        given(userRepository.findByToken("bad-token")).willReturn(null);

        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .header("Authorization", "bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new BookPostDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addBookToShelf_wrongUser_returns403() throws Exception {
        User user = mockUser(2L, "valid-token");
        given(userRepository.findByToken("valid-token")).willReturn(user);

        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new BookPostDTO())))
                .andExpect(status().isForbidden());
    }
}