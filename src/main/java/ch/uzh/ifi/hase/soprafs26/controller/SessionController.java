package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionJoinPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionLeavePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionReadPagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionSendNotificationPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/users/{userId}/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionGetDTO createReadingSession(
            @PathVariable Long userId,
            @RequestBody List<SessionParticipantPostDTO> participantDTOs) {
        List<Long> userIds = participantDTOs.stream().map(SessionParticipantPostDTO::getUserId).toList();
        List<Long> shelfBookIds = participantDTOs.stream().map(SessionParticipantPostDTO::getShelfBookId).toList();
        Session session = sessionService.createReadingSession(userIds, shelfBookIds);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @PutMapping("/{sessionId}/started")
    @ResponseStatus(HttpStatus.OK)
    public SessionGetDTO startReadingSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        Session session = sessionService.startReadingSession(sessionId);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @PutMapping("/{sessionId}/ended")
    @ResponseStatus(HttpStatus.OK)
    public SessionGetDTO endReadingSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        Session session = sessionService.endReadingSession(sessionId);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @PutMapping("/{sessionId}/joined")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId,
            @RequestBody SessionJoinPutDTO request) {
        sessionService.joinSession(sessionId, userId, request.getShelfBookId());
    }

    @PutMapping("/{sessionId}/left")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId,
            @RequestBody SessionLeavePostDTO dto) {
        sessionService.leaveSession(sessionId, userId, dto.getShelfBookId(), dto.getPagesRead());
    }

    @GetMapping("/latest")
    @ResponseStatus(HttpStatus.OK)
    public SessionGetDTO getLatestSession(@PathVariable Long userId) {
        Session session = sessionService.getLatestSessionForUser(userId);
        SessionGetDTO sessionGetDTO = DTOMapper.INSTANCE.convertSessionToGetDTO(session);

        session.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .ifPresent(p -> {
                    sessionGetDTO.setBookTitle(p.getShelfBook().getBook().getName());
                    sessionGetDTO.setCoverUrl(p.getShelfBook().getBook().getCoverUrl());
                    sessionGetDTO.setShelfBookId(p.getShelfBook().getId());
                    sessionGetDTO.setPagesRead(p.getPagesRead());
                });

        return sessionGetDTO;
    }

    @PutMapping("/{sessionId}/readPage")
    @ResponseStatus(HttpStatus.CREATED)
    public void changeNumberOfPagesSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId,
            @RequestBody SessionReadPagePutDTO request) {
        sessionService.changeNumberOfPagesSession(sessionId, userId, request.getNumberOfPages());
    }

    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionGetDTO sendSessionInvitations(
            @PathVariable Long userId,
            @RequestBody SessionSendNotificationPostDTO request) {
        Session session = sessionService.createReadingSession(List.of(userId), List.of(request.getShelfBookId()));
        sessionService.sendSessionNotification(session.getId(), userId, request.getParticipantIds());
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @GetMapping("/{sessionId}/participants")
    @ResponseStatus(HttpStatus.OK)
    public List<SessionParticipantGetDTO> getSessionParticipants(@PathVariable Long userId, @PathVariable Long sessionId) {
        List<SessionParticipant> participants = sessionService.getSessionParticipants(sessionId);
    
        List<SessionParticipantGetDTO> model = participants.stream()
                .map(participant -> {
                    SessionParticipantGetDTO dto = new SessionParticipantGetDTO();
                    dto.setShelfBookId(participant.getShelfBook().getId());
                    dto.setPagesRead(participant.getShelfBook().getPagesRead()); // TODO
                    dto.setUser(DTOMapper.INSTANCE.convertEntityToUserGetDTO(participant.getUser()));
                    dto.setBook(DTOMapper.INSTANCE.convertBookEntityToGetDTO(participant.getShelfBook().getBook()));
                    return dto;
                })
                .toList();
    
        return model;
    }
}
