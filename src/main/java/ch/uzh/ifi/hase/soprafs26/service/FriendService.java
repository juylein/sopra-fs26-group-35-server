package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendshipsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FriendService {

    private final Logger log = LoggerFactory.getLogger(FriendService.class);

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public FriendService(
            FriendRequestRepository friendRequestRepository,
            FriendshipsRepository friendshipsRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            UserService userService
    ) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipsRepository = friendshipsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    public FriendRequest createFriendRequest(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        User requester = userService.getUser(requesterId);
        User recipient = userService.getUser(recipientId);

        if (areFriends(requester, recipient)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users are already friends");
        }

        if (hasPendingRequestBetween(requesterId, recipientId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A friend request already exists between these users");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequester(requester);
        friendRequest.setRecipient(recipient);
        friendRequest.setStatus(FriendRequestStatus.PENDING);
        friendRequest.setCreatedAt(LocalDateTime.now());

        friendRequest = friendRequestRepository.save(friendRequest);
        friendRequestRepository.flush();

        notificationService.createNotification(
                recipientId,
                NotificationType.FRIEND_REQUEST,
                String.format("%s sent you a friend request", requester.getUsername()),
                friendRequest.getId()
        );

        log.debug("Created friend request {} from {} to {}", friendRequest.getId(), requesterId, recipientId);
        return friendRequest;
    }

    public FriendRequest acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!friendRequest.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can accept this friend request");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending friend requests can be accepted");
        }

        Friendships friendship = new Friendships();
        friendship.setUserA(friendRequest.getRequester());
        friendship.setUserB(friendRequest.getRecipient());
        friendship.setSince(LocalDateTime.now());

        friendshipsRepository.save(friendship);

        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequest.setResolvedAt(LocalDateTime.now());
        friendRequestRepository.save(friendRequest);
        friendRequestRepository.flush();

        notificationService.createNotification(
                friendRequest.getRequester().getId(),
                NotificationType.FRIEND_REQUEST,
                String.format("%s accepted your friend request", friendRequest.getRecipient().getUsername()),
                friendRequest.getId()
        );

        log.debug("Accepted friend request {}", requestId);
        return friendRequest;
    }

    public FriendRequest rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!friendRequest.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can reject this friend request");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending friend requests can be rejected");
        }

        friendRequest.setStatus(FriendRequestStatus.REJECTED);
        friendRequest.setResolvedAt(LocalDateTime.now());
        friendRequestRepository.save(friendRequest);
        friendRequestRepository.flush();

        log.debug("Rejected friend request {}", requestId);
        return friendRequest;
    }

    public List<FriendRequest> getIncomingFriendRequests(Long userId) {
        userService.getUser(userId);
        return friendRequestRepository.findByRecipient_IdAndStatus(userId, FriendRequestStatus.PENDING);
    }

    public List<FriendRequest> getSentFriendRequests(Long userId) {
        userService.getUser(userId);
        return friendRequestRepository.findByRequester_Id(userId);
    }

    private boolean areFriends(User a, User b) {
    return friendshipsRepository.findByUserA_Id(a.getId()).stream()
            .anyMatch(f -> f.getUserB().getId().equals(b.getId()))
        || friendshipsRepository.findByUserA_Id(b.getId()).stream()
            .anyMatch(f -> f.getUserB().getId().equals(a.getId()));
    }

    private boolean hasPendingRequestBetween(Long requesterId, Long recipientId) {
        Optional<FriendRequest> direct = friendRequestRepository
                .findByRequester_IdAndRecipient_Id(requesterId, recipientId);

        if (direct.isPresent() && direct.get().getStatus() == FriendRequestStatus.PENDING) {
            return true;
        }

        Optional<FriendRequest> reverse = friendRequestRepository
                .findByRequester_IdAndRecipient_Id(recipientId, requesterId);

        return reverse.isPresent() && reverse.get().getStatus() == FriendRequestStatus.PENDING;
    }

    public List<User> getFriends(Long userId) {
        List<Friendships> asUserA = friendshipsRepository.findByUserA_Id(userId);
        List<Friendships> asUserB = friendshipsRepository.findByUserB_Id(userId);

        List<User> friends = new ArrayList<>();

        for (Friendships f : asUserA) {
            friends.add(f.getUserB());
        }
        for (Friendships f : asUserB) {
            friends.add(f.getUserA());
        }

        return friends;
    }
}
