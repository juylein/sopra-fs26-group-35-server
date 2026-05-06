package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/users/{userId}/leaderboard/quiz")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserStatsGetDTO> getQuizLeaderboardForUser(@PathVariable Long userId) {
        List<Leaderboard> leaderboards = leaderboardService.getQuizLeaderboardForUser(userId);
        List<UserStatsGetDTO> dtos = new ArrayList<>();
        for (Leaderboard lb : leaderboards) {
            dtos.add(DTOMapper.INSTANCE.convertToUserStatsGetDTO(lb.getUser(), lb));
        }
        return dtos;
    }
}