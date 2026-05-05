package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;

import ch.uzh.ifi.hase.soprafs26.repository.SessionParticipantRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

import java.util.List;


/**
 * Stats Service
 * This class is the "worker" and responsible for all functionality related to
 * the user statistics
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class StatsService {

	private final Logger log = LoggerFactory.getLogger(StatsService.class);

	private final SessionParticipantRepository sessionParticipantRepository;
	private final ShelfBookRepository shelfBookRepository;
	private final UserRepository userRepository;
	private final LeaderboardRepository leaderboardRepository;

	public StatsService(
		@Qualifier("sessionParticipant") SessionParticipantRepository sessionParticipantRepository,
		@Qualifier("shelfbookRepository") ShelfBookRepository shelfBookRepository,
		@Qualifier("userRepository") UserRepository userRepository,
		@Qualifier("leaderboardRepository") LeaderboardRepository leaderboardRepository
	) {

		this.sessionParticipantRepository = sessionParticipantRepository;
		this.shelfBookRepository = shelfBookRepository;
		this.userRepository = userRepository;
		this.leaderboardRepository = leaderboardRepository;
	}

	public Long pagesRead(Long userId){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		List<SessionParticipant> total_sessions = sessionParticipantRepository.findByUser(user);

		return total_sessions.stream()
			.filter(sp -> sp.getPagesRead() != null)
			.mapToLong(SessionParticipant::getPagesRead)
			.sum();
	}

	public Long booksRead(Long userId){
		return shelfBookRepository.countByShelf_OwnerIdAndStatus(userId, BookStatus.FINISHED);
	}

	public Long totalPoints(Long userId){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Leaderboard leaderboard = leaderboardRepository.findByUser(user);
		return leaderboard != null ? leaderboard.getTotalPoints() : 0L;
	}

	public Long readingPoints(Long userId){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Leaderboard leaderboard = leaderboardRepository.findByUser(user);
		return leaderboard != null ? leaderboard.getReadingPoints() : 0L;
	}

	public Long quizzPoints(Long userId){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Leaderboard leaderboard = leaderboardRepository.findByUser(user);
		return leaderboard != null ? leaderboard.getQuizzPoints() : 0L;
	}
}
