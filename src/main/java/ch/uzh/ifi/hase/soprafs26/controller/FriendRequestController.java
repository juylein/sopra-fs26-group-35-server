package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendshipGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import java.util.List;
import java.util.ArrayList;

@RestController
public class FriendRequestController {

    private final FriendService friendService;

    FriendRequestController(FriendService friendService) {
        this.friendService = friendService;
    }
    @PostMapping("/users/{recipientId}/friend-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestGetDTO sendFriendRequest(
            @PathVariable Long recipientId,
            Authentication authentication
    ) {
        User requester = (User) authentication.getPrincipal();
    
        FriendRequest friendRequest =
                friendService.createFriendRequest(requester.getId(), recipientId);
    
        return DTOMapper.INSTANCE.convertFriendRequestToGetDTO(friendRequest);
    }

    @GetMapping("/users/{userId}/friend-requests/incoming")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestGetDTO> getIncomingFriendRequests(@PathVariable Long userId) {
        List<FriendRequest> requests = friendService.getIncomingFriendRequests(userId);
        List<FriendRequestGetDTO> friendGetDTOs = new ArrayList<>();

        for (FriendRequest request : requests){
            friendGetDTOs.add(DTOMapper.INSTANCE.convertFriendRequestToGetDTO(request));
        }
        return friendGetDTOs;
    }

    @GetMapping("/users/{userId}/friend-requests/sent")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestGetDTO> getSentRequests(@PathVariable Long userId) {

        List<FriendRequest> requests = friendService.getSentFriendRequests(userId);
        List<FriendRequestGetDTO> friendGetDTOs = new ArrayList<>();

        for (FriendRequest request : requests){
            friendGetDTOs.add(DTOMapper.INSTANCE.convertFriendRequestToGetDTO(request));
        }
        return friendGetDTOs;
    }

    @PutMapping("/friend-requests/{requestId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public FriendRequestGetDTO acceptFriendRequest(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
    
        FriendRequest result = friendService.acceptFriendRequest(requestId, user.getId());
        return DTOMapper.INSTANCE.convertFriendRequestToGetDTO(result);
    }

    @PutMapping("/friend-requests/{requestId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public FriendRequestGetDTO rejectFriendRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId
    ) {
        FriendRequest rejectFriendRequest = friendService.rejectFriendRequest(requestId, userId);
        return DTOMapper.INSTANCE.convertFriendRequestToGetDTO(rejectFriendRequest);
    }

}
