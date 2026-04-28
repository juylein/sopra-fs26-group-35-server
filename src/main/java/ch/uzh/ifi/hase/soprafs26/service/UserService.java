package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final ShelfRepository shelfRepository;
	private final LeaderboardRepository leaderboardRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserService(
		@Qualifier("userRepository") UserRepository userRepository,
		@Qualifier("shelfRepository") ShelfRepository shelfRepository,
		@Qualifier("leaderboardRepository") LeaderboardRepository leaderboardRepository	
	) {
		this.userRepository = userRepository;
		this.shelfRepository = shelfRepository;
		this.leaderboardRepository = leaderboardRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUser(Long id) {
        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

	public User getUserByUsername(String username) {
    	User user = userRepository.findByUsername(username);  // if returns Optional<User>
		if (user == null){
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with username" + username + "not found");
		}
		return user;
		}

	public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, 
            "User with id " + id + " not found" // Added spaces for readability
        ));
		}

    public User createUser(User newUser) {
        String baseErrorMessage = "The %s provided is empty. Therefore, the user could not be created!";
        if (newUser.getPassword().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage, "password", "is"));
        }
        if (newUser.getUsername().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage, "username", "is"));
        }

        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);

        String hashedPassword = this.passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        checkIfUserExists(newUser);
        newUser = userRepository.save(newUser);
        userRepository.flush();

		//create compulsory "read" shelf to keep track of read books for stats
		Shelf readShelf = new Shelf();
		readShelf.setName("Read");
		readShelf.setShared(false);
		readShelf.setOwner(newUser);

		shelfRepository.save(readShelf);

        //auto-create To Read shelf for new users
        Shelf toReadShelf = new Shelf();
        toReadShelf.setName("To Read");
        toReadShelf.setOwner(newUser);
        shelfRepository.save(toReadShelf);

        //auto-create Recent Readings shelf for new users
        Shelf recentReadingsShelf = new Shelf();
        recentReadingsShelf.setName("Recent Readings");
        recentReadingsShelf.setOwner(newUser);
        shelfRepository.save(recentReadingsShelf);

		//create "leaderboard" instance for every new User and set values to 0
		Leaderboard leaderboard = new Leaderboard();
		leaderboard.setUser(newUser);
		leaderboard.addReadingPoints(0L);
		leaderboard.addQuizzPoints(0L);

		leaderboardRepository.save(leaderboard);

		newUser.setLeaderboard(leaderboard);

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format(baseErrorMessage, "username", "is"));
        }
    }

    public User loginUser (User userInput) {
        User userByUsername = userRepository.findByUsername(userInput.getUsername());

        if (userByUsername == null || !passwordEncoder.matches(userInput.getPassword(), userByUsername.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        userByUsername.setStatus(UserStatus.ONLINE);
        userByUsername.setToken(UUID.randomUUID().toString());
        userRepository.flush();
        return userByUsername;
    }

    public void logoutUser(Long id) 
	{	
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = optionalUser.get();
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to logout from this user profile");
        }
		user.setStatus(UserStatus.OFFLINE);
        user.setToken(null);
        
		log.debug("Logout for: {}", user);
        userRepository.flush();
	}


    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query must not be empty");
        }
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    public void update(Long id, String newPassword, String newBio, List<String> newGenres) {
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = optionalUser.get();
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to change this user profile");
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            String hashedPassword = this.passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
        }
        if (newBio != null) {
            user.setBio(newBio);
        }
        if (newGenres != null) {
            user.setGenres(newGenres);
        }
        userRepository.save(user);
    }
}
