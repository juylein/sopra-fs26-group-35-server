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
import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;

import ch.uzh.ifi.hase.soprafs26.repository.ActivitiesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendshipsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import ch.uzh.ifi.hase.soprafs26.service.UserService;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;


/**
 * Activities Service
 * This class is the "worker" and responsible for all functionality related to
 * the activities
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class ActivitiesService {

	private final Logger log = LoggerFactory.getLogger(ActivitiesService.class);

	private final ActivitiesRepository activitiesRepository;
	private final FriendshipsRepository friendshipsRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	public ActivitiesService(
		@Qualifier("activitiesRepository") ActivitiesRepository activitiesRepository,
		@Qualifier("friendshipsRepository") FriendshipsRepository friendshipsRepository,
		@Qualifier("userRepository") UserRepository userRepository,
		@Qualifier("userService") UserService userService
	) {

		this.activitiesRepository = activitiesRepository;
		this.friendshipsRepository = friendshipsRepository;
		this.userRepository = userRepository;
		this.userService = userService;
	}

	public List<Activities> getAllActivities(User user) {
		List<Friendships> friendships = friendshipsRepository.findByUserA_Id(user.getId());
		List<User> friends = friendships.stream()
			.map(f-> {if (f.getUserA().equals(user)){
				return f.getUserB();
			} else {
				return f.getUserA();
			}
		}).toList();
		friends.add(user); 

		return activitiesRepository.findAllByUserIn(friends);
	}

	public List<Activities> getActivitiesByFriend(User user, Long friendId) {
		List<Friendships> friendships = friendshipsRepository.findByUserA_Id(user.getId());
		boolean isFriend = friendships.stream()
		.anyMatch(f ->
			f.getUserA().getId().equals(friendId) || f.getUserB().getId().equals(friendId));
		
		if (!isFriend){
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "There is no friendship between" + user.getUsername() + "and user with id" + friendId);
		}
		User friend = userService.getUserById(friendId);

		return activitiesRepository.findAllByUser(friend);
	}

	public Activities addActivity(User user, BookStatus status, Book book){
		Activities newActivity = new Activities();
		newActivity.setUser(user);
		newActivity.setBook(book);
		newActivity.setDateTime(LocalDateTime.now());

		if (status == BookStatus.FINISHED){
			newActivity.setActions("finished reading");
		}
		else if (status == BookStatus.READING){
			newActivity.setActions("started reading");
		}
		else {
			return null;
		}

		activitiesRepository.save(newActivity);

		return newActivity;
	}
}
