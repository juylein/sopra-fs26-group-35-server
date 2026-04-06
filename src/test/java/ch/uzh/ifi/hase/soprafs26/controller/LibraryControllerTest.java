package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;

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
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.beans.Transient;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("To Read");
        shelf.setShared(false);

        given(libraryService.getLibrary(1L)).willReturn(List.of(shelf));

        mockMvc.perform(get("/users/1/library/shelves")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("To Read")))
                .andExpect(jsonPath("$[0].shared", is(false)));
    }

    @Test
    public void getLibrary_invalidAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/users/1/library/shelves")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getLibrary_invalidToken_returns401() throws Exception {
        given(libraryService.getLibrary(1L))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(get("/users/1/library/shelves")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getLibrary_wrongUserId_returns403() throws Exception {
        given(libraryService.getLibrary(99L))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(get("/users/99/library/shelves")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void addShelf_validInput_returnsCreated() throws Exception {
        Shelf shelf = new Shelf();
        shelf.setId(2L);
        shelf.setName("Favorites");
        shelf.setShared(false);

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        given(libraryService.addShelf(1L, "Favorites")).willReturn(shelf);

        mockMvc.perform(post("/users/1/library/shelves")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(shelfPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Favorites")))
                .andExpect(jsonPath("$.shared", is(false)));
    }

    @Test
    public void addShelf_invalidAuthentication_returns401() throws Exception {
        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        mockMvc.perform(post("/users/1/library/shelves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(shelfPostDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addShelf_invalidToken_returns401() throws Exception {
        given(libraryService.addShelf(1L, "Favorites"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        mockMvc.perform(post("/users/1/library/shelves")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(shelfPostDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addShelf_wrongUserId_returns403() throws Exception {
        given(libraryService.addShelf(99L, "Favorites"))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ShelfPostDTO shelfPostDTO = new ShelfPostDTO();
        shelfPostDTO.setName("Favorites");

        mockMvc.perform(post("/users/99/library/shelves")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(shelfPostDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void addBookToShelf_validInput_returnsCreated() throws Exception {
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

        given(libraryService.addBookToShelf(Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
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
        given(libraryService.addBookToShelf(Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .header("Authorization", "bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new BookPostDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void addBookToShelf_wrongUser_returns403() throws Exception {
        given(libraryService.addBookToShelf(Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(post("/users/1/library/shelves/1/books")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new BookPostDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void updateBookStatus_validInput_returnsNoContent() throws Exception {
        // given
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setStatus(BookStatus.FINISHED);

        given(libraryService.updateBookStatus(1L, "google-test-id", BookStatus.FINISHED))
                .willReturn(shelfBook);

        // when/then
        mockMvc.perform(put("/users/1/library/shelves/1/books/2")
                        .param("status", "FINISHED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void updateBookStatus_shelfNotFound_returnsNotFound() throws Exception {
        // given
        given(libraryService.updateBookStatus(99L, "2", BookStatus.FINISHED))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        // when/then
        mockMvc.perform(put("/users/1/library/shelves/99/books/2")
                        .param("status", "FINISHED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}