package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findAllByQuiz_Id(Long quizId);

    List<QuizResult> findAllByQuiz_CreatedBy_IdOrderByQuiz_CreatedAtDesc(Long userId);

    List<QuizResult> findAllByUser_IdAndQuiz_Id(Long userId, Long quizId);
}