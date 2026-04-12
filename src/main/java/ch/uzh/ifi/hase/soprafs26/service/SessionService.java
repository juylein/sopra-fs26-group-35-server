package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Session createSession(Long hostId, String bookId) {
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Session session = new Session();
        session.setHost(host);
        session.setBook(book);
        session.setStartTime(LocalDateTime.now());

        return sessionRepository.save(session);
    }
}
