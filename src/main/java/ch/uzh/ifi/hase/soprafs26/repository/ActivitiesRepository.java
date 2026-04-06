package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.util.List;

@Repository("activitiesRepository")
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {
	List<Activities> findAllByUser(User user);
	List<Activities> findAllByUserIn(List<User> users); 

}
