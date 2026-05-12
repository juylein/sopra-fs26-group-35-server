package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.repository.LeaderboardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionParticipantRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.ActivitiesService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfBookGetDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ShelfBookRepository shelfbookRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final NotificationService notificationService;
    private final ActivitiesService activitiesService;

    @Autowired
    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          ShelfBookRepository shelfbookRepository,
                          SessionParticipantRepository sessionParticipantRepository,
                          LibraryService libraryService,
                          LeaderboardRepository leaderboardRepository,
                          NotificationService notificationService,
                          ActivitiesService activitiesService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.shelfbookRepository = shelfbookRepository;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.notificationService = notificationService;
        this.activitiesService = activitiesService;
    }

    public void sendSessionNotification(Long sessionId, Long senderId, List<Long> participantIds)
    {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));
    
        for (Long participantId : participantIds) {
            notificationService.createNotification(
                participantId,
                NotificationType.SHARED_SESSION,
                String.format("%s wants to start a shared session... Hurry up!", sender.getUsername()),
                null
            );

            notificationService.sendSessionInvite(sessionId, senderId, participantId);
        }
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

        for (ShelfBook book : books) {
            if (book.getShelf().getShared()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reading sessions can only use books from individual shelves, not shared shelves");
            }
        }

        Session newSession = new Session();
        sessionRepository.save(newSession);
        sessionRepository.flush();
        
        for (int i = 0; i < participants.size(); i++) {
            SessionParticipant sp = new SessionParticipant();
            sp.setSession(newSession);
            sp.setJoinedAt(LocalDateTime.now()); 
            sp.setUser(participants.get(i));
            sp.setShelfBook(books.get(i));

            sessionParticipantRepository.save(sp);
        }

        sessionParticipantRepository.flush();
        return newSession;
    }

    public Session startReadingSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setStartTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public Session endReadingSession(Long sessionId){
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        return session;
    }

    public void joinSession(Long sessionId, Long userId, Long shelfBookId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ShelfBook shelfBook = shelfbookRepository.findById(shelfBookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShelfBook not found"));

        SessionParticipant sp = new SessionParticipant();
        sp.setSession(session);
        sp.setJoinedAt(LocalDateTime.now()); 
        sp.setUser(user);
        sp.setShelfBook(shelfBook);
        sessionParticipantRepository.save(sp);
        sessionParticipantRepository.flush();

        ShelfBookGetDTO shelfBookModel = new ShelfBookGetDTO();
        BookGetDTO bookModel = new BookGetDTO();
        bookModel.setName(shelfBook.getBook().getName());
        bookModel.setId(shelfBook.getBook().getId());
        bookModel.setPages(shelfBook.getBook().getPages());

        shelfBookModel.setId(shelfBookId);
        shelfBookModel.setPagesRead(shelfBook.getPagesRead());
        shelfBookModel.setBook(bookModel);
        shelfBook.setStatus(BookStatus.READING);
        activitiesService.addActivity(user, BookStatus.READING, shelfBook.getBook());

        List<Long> participantIds = session.getParticipants()
            .stream()
            .filter(p -> !p.getUser().getId().equals(userId))
            .map(x -> x.getUser().getId()).toList();

        for (Long recipientId : participantIds) {
            this.notificationService.sendSessionJoin(sessionId, userId, recipientId, shelfBookModel);
        }
    }

    public void changeNumberOfPagesSession(Long sessionId, Long userId, Long numberOfPages) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
        SessionParticipant participant = sessionParticipantRepository.findBySessionAndUser(session, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

        participant.setPagesRead(numberOfPages);
        sessionParticipantRepository.save(participant);
        sessionParticipantRepository.flush();

        List<Long> participantIds = session.getParticipants()
            .stream()
            .filter(p -> !p.getUser().getId().equals(userId))
            .map(x -> x.getUser().getId()).toList();

        for (Long recipientId : participantIds) {
            this.notificationService.sendSessionChangePage(sessionId, userId, recipientId, numberOfPages);
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // runs daily at 03:00
    public void deleteOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Session> oldSessions = sessionRepository.findByEndTimeBefore(cutoff);
        sessionRepository.deleteAll(oldSessions);
    }

    public void leaveSession(Long sessionId, Long userId, Long shelfBookId, Long pagesRead) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SessionParticipant sp = sessionParticipantRepository.findBySessionAndUser(session, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found in session"));

        ShelfBook shelfBook = shelfbookRepository.findById(shelfBookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf book not found"));

        sp.setLeftAt(LocalDateTime.now());
        sp.setPagesRead(pagesRead);
        sessionParticipantRepository.save(sp);

        shelfBook.setPagesRead(pagesRead);
        shelfbookRepository.save(shelfBook);
        shelfbookRepository.flush();

        List<Long> participantIds = session.getParticipants()
            .stream()
            .filter(p -> !p.getUser().getId().equals(userId))
            .map(x -> x.getUser().getId()).toList();

        for (Long recipientId : participantIds) {
            this.notificationService.sendSessionQuit(sessionId, userId, recipientId);
        }

        long minutesRead = sp.getReadingTime().toMinutes();
        long points = Math.round((pagesRead * 0.1) + (minutesRead * 0.05));

        Leaderboard leaderboard = leaderboardRepository.findByUser(user);
        if (leaderboard == null) {
            leaderboard = new Leaderboard();
            leaderboard.setUser(user);
            leaderboard.setTotalPoints();
        }
        leaderboard.addReadingPoints(points);
        leaderboardRepository.save(leaderboard);
    }

    public Session getLatestSessionForUser(Long userId) {
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this user's sessions");
        }

        return sessionRepository.findLatestSessionForUser(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No completed sessions found for user " + userId));
    }

    public List<SessionParticipant> getSessionParticipants(Long sessionId)
    {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        return session.getParticipants();
    }
}
