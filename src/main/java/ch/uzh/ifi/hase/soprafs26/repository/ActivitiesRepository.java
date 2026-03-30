package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("activitiesRepository")
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {
	Activities findByUser(User user);
}
