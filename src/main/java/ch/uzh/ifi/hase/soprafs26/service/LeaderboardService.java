package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;

import java.util.List;
import java.util.UUID;


/**
 * Leaderboard Service
 * This class is the "worker" and responsible for all functionality related to
 * the leaderboard
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class LeaderboardService {

	private final Logger log = LoggerFactory.getLogger(LeaderboardService.class);

	private final LeaderboardRepository leaderboardRepository;

	public LeaderboardService(@Qualifier("leaderboardRepository") LeaderboardRepository leaderboardRepository) {
		this.leaderboardRepository = leaderboardRepository;
	}

	public List<Leaderboard> getLeaderboards(Long top) {
		Sort sort = Sort.by(Sort.Direction.DESC,"totalPoints");
		if (top != null){
		Pageable topLimit = PageRequest.of(0, top.intValue(), sort);
		return leaderboardRepository.findAll(topLimit).getContent();
		}
		else return this.leaderboardRepository.findAll(sort);
	}

	public Leaderboard getLeaderboardByUser(User user) {
		return this.leaderboardRepository.findByUser(user);
	}
}
