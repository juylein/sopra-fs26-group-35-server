package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ShelfRepository shelfRepository;

	@Mock
	private LeaderboardRepository leaderboardRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setName("testName");
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
		Mockito.when(shelfRepository.save(Mockito.any())).thenReturn(new Shelf());
		Mockito.when(leaderboardRepository.save(Mockito.any())).thenReturn(new Leaderboard());

		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateName_throwsException() {
		// given -> a first user has already been created
		// userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		// userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

    @Test
    public void getUserById_validId_success() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User found = userService.getUserById(1L);

        assertEquals(testUser.getId(), found.getId());
        assertEquals(testUser.getUsername(), found.getUsername());
    }

    @Test
    public void getUserById_invalidId_throwsException() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUserById(999L));
    }

    @Test
    public void getUserByUsername_validUsername_success() {
        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

        User found = userService.getUserByUsername("testUsername");

        assertEquals("testUsername", found.getUsername());
    }

    @Test
    public void getUserByUsername_invalidUsername_throwsException() {
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> userService.getUserByUsername("nonexistent"));
    }

    @Test
    public void loginUser_validCredentials_success() {
        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(null);
        User createdUser = userService.createUser(testUser);

        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(createdUser);

        User loginInput = new User();
        loginInput.setUsername("testUsername");
        loginInput.setPassword("testPassword");

        User loggedIn = userService.loginUser(loginInput);

        assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
        assertNotNull(loggedIn.getToken());
    }

    @Test
    public void loginUser_wrongPassword_throwsException() {
        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(null);
        User createdUser = userService.createUser(testUser);
        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(createdUser);

        User loginInput = new User();
        loginInput.setUsername("testUsername");
        loginInput.setPassword("wrongPassword");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }

    @Test
    public void loginUser_nonexistentUser_throwsException() {
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        User loginInput = new User();
        loginInput.setUsername("ghost");
        loginInput.setPassword("password");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }
}
