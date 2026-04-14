package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
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

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionGetDTO createReadingSession(
            @PathVariable Long userId,
            @RequestBody List<SessionParticipantPostDTO> participantDTOs) {
        List<Long> userIds = participantDTOs.stream().map(SessionParticipantPostDTO::getUserId).toList();
        List<Long> shelfBookIds = participantDTOs.stream().map(SessionParticipantPostDTO::getShelfBookId).toList();
        Session session = sessionService.createReadingSession(userIds, shelfBookIds);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @PutMapping("/sessions/{sessionId}/started")
    @ResponseStatus(HttpStatus.OK)
    public SessionGetDTO startReadingSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        Session session = sessionService.startReadingSession(sessionId);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }

    @PutMapping("/sessions/{sessionId}/ended")
    @ResponseStatus(HttpStatus.OK)
    public SessionGetDTO endReadingSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        Session session = sessionService.endReadingSession(sessionId);
        return DTOMapper.INSTANCE.convertSessionToGetDTO(session);
    }
}
