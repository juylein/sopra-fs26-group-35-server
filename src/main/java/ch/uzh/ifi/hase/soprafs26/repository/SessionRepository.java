package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByEndTimeBefore(LocalDateTime cutoff);

    @Query("""
    SELECT s FROM Session s
    JOIN s.participants p
    WHERE p.user.id = :userId
    AND s.end_time IS NOT NULL
    ORDER BY s.end_time DESC
    LIMIT 1
""")
    Optional<Session> findLatestSessionForUser(@Param("userId") Long userId);
}
