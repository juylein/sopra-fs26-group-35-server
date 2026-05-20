package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.ShelfInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelfInvitationRepository extends JpaRepository<ShelfInvitation, Long> {
    List<ShelfInvitation> findByRecipientIdAndStatus(Long recipientId, String status);
    boolean existsByShelfIdAndRecipientIdAndStatus(Long shelfId, Long recipientId, String status);
    void deleteByShelfId(Long shelfId);
}
