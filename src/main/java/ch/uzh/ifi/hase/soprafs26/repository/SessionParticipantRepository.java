package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;

import java.util.List;
import java.util.Optional;

@Repository("sessionParticipant")
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    List<SessionParticipant> findBySession(Session session);
    List<SessionParticipant> findByUser(User user);
    Optional<SessionParticipant> findBySessionAndUser(Session session, User user);
}
