package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;

import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivitiesGetDTO;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;
import ch.uzh.ifi.hase.soprafs26.service.ActivitiesService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;
	private final LeaderboardService leaderboardService;
	private final ActivitiesService activitiesService;

	UserController(UserService userService, 
				   LeaderboardService leaderboardService,
				   ActivitiesService activitiesService
				) {
		this.userService = userService;
		this.leaderboardService = leaderboardService;
		this.activitiesService = activitiesService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedInUser = userService.loginUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
    }

	@PutMapping("/users/{userId}/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logoutUser(@PathVariable("userId") Long userId)
	{
		userService.logoutUser(userId);
	}


    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserById(@PathVariable("userId") Long id) {
        User user = userService.getUser(id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

	@GetMapping("/users/{userId}/statistics")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserStatsGetDTO getUserStats(@PathVariable Long userId) {
		User user = userService.getUserById(userId);
		Leaderboard leaderboard = leaderboardService.getLeaderboardByUser(user);

		return DTOMapper.INSTANCE.convertToUserStatsGetDTO(user, leaderboard);
	}

	@GetMapping("/users/search")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> searchUsers(@RequestParam String query) {
		List<User> users = userService.searchUsers(query);
		List<UserGetDTO> userGetDTOs = new ArrayList<>();
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@GetMapping("/users/leaderboard")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserStatsGetDTO> getTopUsers(@RequestParam(required = false) Long top){
		List<Leaderboard> leaderboards = leaderboardService.getLeaderboards(top);
		List<UserStatsGetDTO> dtos = new ArrayList<>();

		for (Leaderboard lb: leaderboards){
			dtos.add(DTOMapper.INSTANCE.convertToUserStatsGetDTO(lb.getUser(), lb));
		}
		return dtos;
	}

	@GetMapping("/users/{userId}/activities")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<ActivitiesGetDTO> getActivities(@PathVariable Long userId){
		User user = userService.getUserById(userId);
		List<Activities> activities = activitiesService.getAllActivities(user);
		List<ActivitiesGetDTO> activitiesGetDTOs = new ArrayList<>();

		for (Activities activity : activities){
			activitiesGetDTOs.add(DTOMapper.INSTANCE.convertActivitiesEntityToGetDTO(activity));
		}
		return activitiesGetDTOs;
	}

	@GetMapping("/users/{userId}/activities/{friendId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<ActivitiesGetDTO> getActivitiesByFriend(@PathVariable Long friendId,@PathVariable Long userId){
		User user = userService.getUserById(userId);
		List<Activities> activities = activitiesService.getActivitiesByFriend(user, friendId);
		List<ActivitiesGetDTO> activitiesGetDTOs = new ArrayList<>();

		for (Activities activity : activities){
			activitiesGetDTOs.add(DTOMapper.INSTANCE.convertActivitiesEntityToGetDTO(activity));
		}
		return activitiesGetDTOs;
	}

	
	@PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
	public void updateUser(
        @PathVariable Long id,
        @RequestBody UserPostDTO dto
	) {
    userService.update(
            id,
            dto.getPassword(),
            dto.getBio(),
            dto.getGenres()
    );
	}
}


