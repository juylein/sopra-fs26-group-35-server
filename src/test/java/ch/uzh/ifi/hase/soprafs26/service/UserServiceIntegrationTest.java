package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		userRepository.deleteAll();
	}

	@Test
	public void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

        // when
		User createdUser = userService.createUser(testUser);

		// then
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        userService.createUser(testUser);

		// attempt to create second user with same username
		User testUser2 = new User();

		// change the name but forget about the username
		testUser2.setName("testName2");
		testUser2.setUsername("testUsername");
        testUser2.setPassword("testPassword2");

		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

    @Test
    public void getUserById_validId_success() {
        User testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User createdUser = userService.createUser(testUser);

        User foundUser = userService.getUserById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getUsername(), foundUser.getUsername());
    }

    @Test
    public void getUserById_invalidId_throwsException() {
        assertThrows(ResponseStatusException.class, () -> userService.getUserById(999L));
    }

    @Test
    public void getUserByUsername_validUsername_success() {
        User testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        userService.createUser(testUser);

        User foundUser = userService.getUserByUsername("testUsername");

        assertEquals("testUsername", foundUser.getUsername());
    }

    @Test
    public void getUserByUsername_invalidUsername_throwsException() {
        assertThrows(ResponseStatusException.class,
                () -> userService.getUserByUsername("nonexistent"));
    }

    @Test
    public void loginUser_validCredentials_success() {
        User testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        userService.createUser(testUser);

        User loginInput = new User();
        loginInput.setUsername("testUsername");
        loginInput.setPassword("testPassword");

        User loggedIn = userService.loginUser(loginInput);

        assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
        assertNotNull(loggedIn.getToken());
    }

    @Test
    public void loginUser_wrongPassword_throwsException() {
        User testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        userService.createUser(testUser);

        User loginInput = new User();
        loginInput.setUsername("testUsername");
        loginInput.setPassword("wrongPassword");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }

    @Test
    public void loginUser_nonexistentUser_throwsException() {
        User loginInput = new User();
        loginInput.setUsername("ghost");
        loginInput.setPassword("password");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }
}
