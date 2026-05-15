package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/latest")
    @ResponseBody
    public ResponseEntity<MyQuizSummaryDTO> getLatestQuizForUser(@PathVariable Long userId) {
        return quizService.getLatestQuizForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{quizId}/submit")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public QuizResultEntryDTO submitQuiz(
            @PathVariable Long userId,
            @PathVariable Long quizId,
            @RequestBody QuizSubmitDTO quizSubmitDTO) {
        return quizService.submitQuiz(userId, quizId, quizSubmitDTO.getAnswers());
    }

    @PutMapping("/{quizId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptQuiz(
            @PathVariable Long userId,
            @PathVariable Long quizId) {
        quizService.acceptQuiz(userId, quizId);
    }

    @GetMapping("/{quizId}/take")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public QuizTakeDTO getQuizForTaking(
            @PathVariable Long userId,
            @PathVariable Long quizId) {
        return quizService.getQuizForTaking(userId, quizId);
    }
}