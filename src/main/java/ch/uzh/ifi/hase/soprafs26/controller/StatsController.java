package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.StatsService;

import java.util.Map;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * StatsService and finally return the result.
 */
@RestController
public class StatsController {

	private final StatsService statsService;

	StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/users/{userId}/pagesRead")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Long> getTotalPages(@PathVariable Long userId){
		return Map.of("pagesRead", statsService.pagesRead(userId));
	}

	@GetMapping("/users/{userId}/booksRead")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Long> getTotalBooks(@PathVariable Long userId){
		return Map.of("booksRead", statsService.booksRead(userId));
	}

	@GetMapping("/users/{userId}/totalPoints")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Long> getTotalPoints(@PathVariable Long userId){
		return Map.of("totalPoints", statsService.totalPoints(userId));
	}

	@GetMapping("/users/{userId}/totalReadingPoints")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Long> getTotalReadingPoints(@PathVariable Long userId){
		return Map.of("totalReadingPoints", statsService.readingPoints(userId));
	}

	@GetMapping("/users/{userId}/totalQuizzPoints")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Long> getTotalQuizzPoints(@PathVariable Long userId){
		return Map.of("totalQuizzPoints", statsService.quizzPoints(userId));
	}
}


