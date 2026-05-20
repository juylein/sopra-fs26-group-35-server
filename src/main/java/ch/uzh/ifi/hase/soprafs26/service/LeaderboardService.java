package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendshipsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;
    private final FriendshipsRepository friendshipsRepository;

    @Autowired
    public LeaderboardService(LeaderboardRepository leaderboardRepository,
                              UserRepository userRepository,
                              FriendshipsRepository friendshipsRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.userRepository = userRepository;
        this.friendshipsRepository = friendshipsRepository;
    }

    private User getAuthenticatedUser(Long userId) {
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id " + userId + " not found"));
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to perform this action for this user");
        }
        return user;
    }

    public List<Leaderboard> getLeaderboards(Long top) {
        Sort sort = Sort.by(Sort.Direction.DESC, "totalPoints");
        if (top != null) {
            Pageable topLimit = PageRequest.of(0, top.intValue(), sort);
            return leaderboardRepository.findAll(topLimit).getContent();
        }
        return leaderboardRepository.findAll(Sort.unsorted());
    }

    public Leaderboard getLeaderboardByUser(User user) {
        return leaderboardRepository.findByUser(user);
    }

    public List<Leaderboard> getQuizLeaderboardForUser(Long userId) {
        User user = getAuthenticatedUser(userId);

        Set<User> group = new HashSet<>();
        group.add(user);
        friendshipsRepository.findByUserA_Id(userId)
                .forEach(f -> group.add(f.getUserB()));
        friendshipsRepository.findByUserB_Id(userId)
                .forEach(f -> group.add(f.getUserA()));

        List<Leaderboard> result = new ArrayList<>();
        for (User u : group) {
            Leaderboard lb = leaderboardRepository.findByUser(u);
            if (lb != null) {
                result.add(lb);
            }
        }

        result.sort(Comparator.comparingLong(
                lb -> -((lb.getQuizPoints() != null) ? lb.getQuizPoints() : 0L)
        ));

        return result;
    }
}