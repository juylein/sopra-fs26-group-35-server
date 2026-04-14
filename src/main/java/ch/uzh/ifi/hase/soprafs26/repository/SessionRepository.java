package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByEndTimeBefore(LocalDateTime cutoff);
}
