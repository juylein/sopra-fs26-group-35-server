package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.SecurityConfig;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private LeaderboardService leaderboardService;

    @MockitoBean
    @Qualifier("userRepository")
    private UserRepository userRepository;

	@Test
    @WithMockUser
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

    @Test
    @WithMockUser
    public void getUser_validId_returnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setStatus(UserStatus.ONLINE);

        given(userService.getUser(1L)).willReturn(user);

        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void getUser_invalidAuthentication_returns401() throws Exception {
        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getUser_invalidId_returns404() throws Exception {
        given(userService.getUser(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        MockHttpServletRequestBuilder getRequest = get("/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
	public void getUserStats_validId_ReturnOK() throws Exception {
		Long userId = 1L;

    	User user = new User();
    	user.setId(userId);
    	user.setName("Test User");
    	user.setUsername("testUsername");

    	// Suppose user has 5 books read and 100 pages read
    	// You can mock methods in User if needed
    	// Here we can just assume getBooksRead() and getPagesRead() are called by mapper

    	Leaderboard leaderboard = new Leaderboard();
		leaderboard.setTotalPoints();
		leaderboard.addReadingPoints(50L);
		leaderboard.addQuizzPoints(30L);

    	UserStatsGetDTO userStatsDTO = new UserStatsGetDTO();
    	userStatsDTO.setBooksRead(user.getBooksRead()); 
    	userStatsDTO.setPagesRead(user.getPagesRead()); 
		userStatsDTO.setNumFriends(user.getNumFriends());
		userStatsDTO.setTotalPoints(leaderboard.getTotalPoints());

    	userStatsDTO.setTotalPoints(leaderboard.getTotalPoints());

		// mock services
    	given(userService.getUserById(userId)).willReturn(user);
    	given(leaderboardService.getLeaderboardByUser(user)).willReturn(leaderboard);

		MockHttpServletRequestBuilder getRequest = get("/users/{userId}/statistics", userId)
            .contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
            .andExpect(jsonPath("$.booksRead", is(0)))
			.andExpect(jsonPath("$.pagesRead", is(0)))
			.andExpect(jsonPath("$.numFriends", is(0)))
			.andExpect(jsonPath("$.totalPoints", is(80)));
	}

	@Test
    @WithMockUser
	public void getTopUsers_ReturnOK() throws Exception {
		//mocking 5 users
		List <Leaderboard> mockLeaderboards = new ArrayList<>();
		
		for (int i = 1; i <= 5; i++){
			User user = new User();
			user.setId((long) i);
			user.setUsername("user"+ i);

			Leaderboard lb = new Leaderboard();
			lb.setId((long) i);
			lb.setUser(user);
			lb.setTotalPoints();
			lb.addQuizzPoints(10L*i);

			mockLeaderboards.add(lb);
		}

		//mock service
		given(leaderboardService.getLeaderboards(Mockito.any())).willReturn(mockLeaderboards);

		//request
		mockMvc.perform(get("/users/leaderboard?top=5")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[0].username", is("user1")))
				.andExpect(jsonPath("$[2].username", is("user3")));

	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}