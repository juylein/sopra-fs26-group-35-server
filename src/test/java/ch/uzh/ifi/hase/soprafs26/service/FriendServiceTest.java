package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Friendships;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendshipsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipsRepository friendshipsRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FriendService friendService;

    private User requester;
    private User recipient;

    @BeforeEach
    public void setup() {
        requester = new User();
        requester.setId(1L);
        requester.setUsername("requester");

        recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");
    }

    @Test
    public void createFriendRequest_validRequest_success() {
        when(userService.getUser(1L)).thenReturn(requester);
        when(userService.getUser(2L)).thenReturn(recipient);
        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of());
        when(friendshipsRepository.findByUserA_Id(2L)).thenReturn(List.of());
        when(friendRequestRepository.findByRequester_IdAndRecipient_Id(1L, 2L))
                .thenReturn(Optional.empty());
        when(friendRequestRepository.findByRequester_IdAndRecipient_Id(2L, 1L))
                .thenReturn(Optional.empty());

        FriendRequest saved = new FriendRequest();
        saved.setId(100L);
        saved.setRequester(requester);
        saved.setRecipient(recipient);
        saved.setStatus(FriendRequestStatus.PENDING);
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(saved);

        FriendRequest result = friendService.createFriendRequest(1L, 2L);

        assertEquals(100L, result.getId());
        assertEquals(requester.getId(), result.getRequester().getId());
        assertEquals(recipient.getId(), result.getRecipient().getId());
        assertEquals(FriendRequestStatus.PENDING, result.getStatus());

        verify(notificationService, times(1)).createNotification(
                eq(2L),
                eq(NotificationType.FRIEND_REQUEST),
                contains("sent you a friend request"),
                eq(100L)
        );
    }

    @Test
    public void createFriendRequest_sameUser_throwsBadRequest() {
        assertThrows(ResponseStatusException.class, () -> friendService.createFriendRequest(1L, 1L));
    }

    @Test
    public void createFriendRequest_alreadyFriends_throwsConflict() {
        when(userService.getUser(1L)).thenReturn(requester);
        when(userService.getUser(2L)).thenReturn(recipient);

        Friendships friendship = new Friendships();
        friendship.setUserA(requester);
        friendship.setUserB(recipient);
        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of(friendship));

        assertThrows(ResponseStatusException.class, () -> friendService.createFriendRequest(1L, 2L));
    }

    @Test
    public void createFriendRequest_pendingRequestExists_throwsConflict() {
        when(userService.getUser(1L)).thenReturn(requester);
        when(userService.getUser(2L)).thenReturn(recipient);

        FriendRequest pending = new FriendRequest();
        pending.setId(200L);
        pending.setRequester(requester);
        pending.setRecipient(recipient);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findByRequester_IdAndRecipient_Id(1L, 2L))
                .thenReturn(Optional.of(pending));

        assertThrows(ResponseStatusException.class, () -> friendService.createFriendRequest(1L, 2L));
    }

    @Test
    public void acceptFriendRequest_validRequest_success() {
        FriendRequest pending = new FriendRequest();
        pending.setId(300L);
        pending.setRequester(requester);
        pending.setRecipient(recipient);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(300L)).thenReturn(Optional.of(pending));
        when(friendshipsRepository.save(any(Friendships.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendRequest result = friendService.acceptFriendRequest(300L, 2L);

        assertEquals(FriendRequestStatus.ACCEPTED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        verify(friendshipsRepository, times(1)).save(any(Friendships.class));
        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq(NotificationType.FRIEND_REQUEST),
                contains("accepted your friend request"),
                eq(300L)
        );
    }

    @Test
    public void acceptFriendRequest_wrongRecipient_throwsForbidden() {
        FriendRequest pending = new FriendRequest();
        pending.setId(301L);
        pending.setRequester(requester);
        pending.setRecipient(recipient);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(301L)).thenReturn(Optional.of(pending));

        assertThrows(ResponseStatusException.class, () -> friendService.acceptFriendRequest(301L, 5L));
    }

    @Test
    public void rejectFriendRequest_validRequest_success() {
        FriendRequest pending = new FriendRequest();
        pending.setId(400L);
        pending.setRequester(requester);
        pending.setRecipient(recipient);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(400L)).thenReturn(Optional.of(pending));

        FriendRequest result = friendService.rejectFriendRequest(400L, 2L);

        assertEquals(FriendRequestStatus.REJECTED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
    }

    @Test
    public void getIncomingFriendRequests_callsRepository() {
        when(userService.getUser(2L)).thenReturn(recipient);
        when(friendRequestRepository.findByRecipient_IdAndStatus(2L, FriendRequestStatus.PENDING))
                .thenReturn(List.of());

        List<FriendRequest> result = friendService.getIncomingFriendRequests(2L);

        assertNotNull(result);
        verify(friendRequestRepository, times(1)).findByRecipient_IdAndStatus(2L, FriendRequestStatus.PENDING);
    }

    @Test
    public void getSentFriendRequests_callsRepository() {
        when(userService.getUser(1L)).thenReturn(requester);
        when(friendRequestRepository.findByRequester_Id(1L)).thenReturn(List.of());

        List<FriendRequest> result = friendService.getSentFriendRequests(1L);

        assertNotNull(result);
        verify(friendRequestRepository, times(1)).findByRequester_Id(1L);
    }

    @Test
    public void getFriends_callsRepository(){

        Friendships friendship = new Friendships();
        friendship.setUserA(requester);
        friendship.setUserB(recipient);
        when(friendshipsRepository.findByUserA_Id(1L)).thenReturn(List.of(friendship));

        List<Friendships> friends = friendService.getFriends(1L);

        assertNotNull(friends);
    }
}