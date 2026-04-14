package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionParticipantRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ShelfBookRepository shelfbookRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final LibraryService libraryService;


    @Autowired
    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          ShelfBookRepository shelfbookRepository,
                          SessionParticipantRepository sessionParticipantRepository,
                          LibraryService libraryService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.shelfbookRepository = shelfbookRepository;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.libraryService = libraryService;
    }

    public Session createReadingSession(List<Long> userIds, List<Long> shelfBookIds) {
        if (userIds.size() != shelfBookIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each participant must have exactly one book");
        }

        List<User> participants = userIds.stream()
            .map(id -> userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .toList();

        List<ShelfBook> books = shelfBookIds.stream()
            .map(id -> shelfbookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShelfBook not found")))
            .toList();

        Session newSession = new Session();
        sessionRepository.save(newSession);

        for (int i = 0; i < participants.size(); i++) {
            SessionParticipant sp = new SessionParticipant();
            sp.setSession(newSession);
            sp.setUser(participants.get(i));
            sp.setShelfBook(books.get(i));
            sessionParticipantRepository.save(sp);
        }
        return newSession;
    }

    public Session startReadingSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setStartTime(LocalDateTime.now());

        sessionParticipantRepository.findBySession(session).forEach(sp -> {
            sp.setJoinedAt(LocalDateTime.now());
            sessionParticipantRepository.save(sp);
            ShelfBook shelfBook = sp.getShelfBook();
            if (shelfBook.getStatus() != BookStatus.READING) {
                libraryService.updateBookStatus(shelfBook.getShelf().getId(), shelfBook.getBook().getId(), BookStatus.READING);
            }
        });

        return sessionRepository.save(session);
    }

    public Session endReadingSession(Long sessionId){
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        return session;
    }
}
