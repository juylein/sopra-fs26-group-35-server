package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("friendRequestRepository")
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    Optional<FriendRequest> findByRequester_IdAndRecipient_Id(Long requesterId, Long recipientId);
    List<FriendRequest> findByRecipient_IdAndStatus(Long recipientId, FriendRequestStatus status);
    List<FriendRequest> findByRequester_Id(Long requesterId);
}
