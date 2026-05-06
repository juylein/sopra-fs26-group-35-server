package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizSendDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public QuizGetDTO createQuiz(
            @PathVariable Long userId,
            @RequestBody QuizPostDTO quizPostDTO) {

        Quiz quiz = quizService.createQuiz(userId, quizPostDTO);
        List<QuizQuestion> questions = quizService.getQuestionsForQuiz(quiz.getId());

        QuizGetDTO quizGetDTO = DTOMapper.INSTANCE.convertQuizEntityToQuizGetDTO(quiz);
        quizGetDTO.setQuestions(
                questions.stream()
                        .map(DTOMapper.INSTANCE::convertQuizQuestionEntityToQuizQuestionDTO)
                        .toList()
        );
        return quizGetDTO;
    }

    @PostMapping("/{quizId}/send")
    @ResponseStatus(HttpStatus.OK)
    public void sendQuizToFriends(
            @PathVariable Long userId,
            @PathVariable Long quizId,
            @RequestBody QuizSendDTO quizSendDTO) {

        quizService.sendQuizToFriends(userId, quizId, quizSendDTO.getFriendIds());
    }
}