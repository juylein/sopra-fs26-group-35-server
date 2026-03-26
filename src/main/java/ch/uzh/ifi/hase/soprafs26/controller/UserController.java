package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;

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

	UserController(UserService userService, LeaderboardService leaderboardService) {
		this.userService = userService;
		this.leaderboardService = leaderboardService;
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

	@GetMapping("/users/{userId}/statistics")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserStatsGetDTO getUserStats(@PathVariable Long userId) {
		User user = userService.getUserById(userId);
		Leaderboard leaderboard = leaderboardService.getLeaderboardByUser(user);

		return DTOMapper.INSTANCE.convertToUserStatsGetDTO(user, leaderboard);
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

}
