package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionLeavePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


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
            @PathVariable Long sessionId) {
        sessionService.joinSession(sessionId, userId);
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
    public ResponseEntity<SessionGetDTO> getLatestSession(@PathVariable Long userId) {
        Session session = sessionService.getLatestSessionForUser(userId);

        if (session == null) return ResponseEntity.noContent().build();

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

        return ResponseEntity.ok(sessionGetDTO);
    }
}
