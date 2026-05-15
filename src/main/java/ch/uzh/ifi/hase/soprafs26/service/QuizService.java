package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FriendshipsRepository friendshipsRepository;
    private final QuizResultRepository quizResultRepository;

    public QuizService(QuizRepository quizRepository,
                       QuizQuestionRepository quizQuestionRepository,
                       UserRepository userRepository,
                       NotificationService notificationService,
                       FriendshipsRepository friendshipsRepository,
                       QuizResultRepository quizResultRepository) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.friendshipsRepository = friendshipsRepository;
        this.quizResultRepository = quizResultRepository;
    }

    private User getAuthenticatedUser(Long userId) {
        String currentUserToken = (String) SecurityContextHolder.getContext()
                .getAuthentication().getCredentials();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id " + userId + " not found"));
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to perform this action for this user");
        }
        return user;
    }

    public Quiz createQuiz(Long userId, QuizPostDTO quizPostDTO) {
        User creator = getAuthenticatedUser(userId);

        Quiz quiz = new Quiz();
        quiz.setTitle(quizPostDTO.getTitle());
        quiz.setDifficulty(quizPostDTO.getDifficulty());
        quiz.setBookId(quizPostDTO.getBookId());
        quiz.setCreatedBy(creator);
        quiz = quizRepository.save(quiz);

        if (quizPostDTO.getQuestions() != null) {
            for (QuizQuestionDTO qDto : quizPostDTO.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setQuestionText(qDto.getQuestionText());
                question.setOption1(qDto.getOption1());
                question.setOption2(qDto.getOption2());
                question.setOption3(qDto.getOption3());
                question.setOption4(qDto.getOption4());
                question.setCorrectOption(qDto.getCorrectOption());
                quizQuestionRepository.save(question);
            }
        }

        return quiz;
    }

    public void sendQuizToFriends(Long userId, Long quizId, List<Long> friendIds) {
        User sender = getAuthenticatedUser(userId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz with id " + quizId + " not found"));

        if (friendIds == null || friendIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "friendIds must not be empty");
        }

        for (Long friendId : friendIds) {
            User friend = userRepository.findById(friendId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with id " + friendId + " not found"));

            boolean areFriends =
                    friendshipsRepository.findByUserA_Id(userId).stream()
                            .anyMatch(f -> f.getUserB().getId().equals(friendId))
                            ||
                            friendshipsRepository.findByUserB_Id(userId).stream()
                                    .anyMatch(f -> f.getUserA().getId().equals(friendId));

            if (!areFriends) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User " + friendId + " is not a friend of user " + userId);
            }

            notificationService.createNotification(
                    friendId,
                    NotificationType.QUIZ_CHALLENGE,
                    sender.getUsername() + " sent you a quiz: " + quiz.getTitle(),
                    quiz.getId()
            );

            QuizResult result = new QuizResult();
            result.setQuiz(quiz);
            result.setUser(friend);
            result.setScoreTotal(quizQuestionRepository.findAllByQuiz_Id(quizId).size());
            result.setScoreGot(null); // pending
            result.setAccepted(false);
            result.setCompleted(false);
            quizResultRepository.save(result);
        }
    }

    @Transactional(readOnly = true)
    public Quiz getQuizById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz with id " + quizId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuestionsForQuiz(Long quizId) {
        return quizQuestionRepository.findAllByQuiz_Id(quizId);
    }

    @Transactional(readOnly = true)
    public Optional<MyQuizSummaryDTO> getLatestQuizForUser(Long userId) {
        getAuthenticatedUser(userId);

        Optional<Quiz> latestOpt = quizRepository
                .findTopByCreatedBy_IdOrderByCreatedAtDesc(userId);

        if (latestOpt.isEmpty()) {
            return Optional.empty(); // ← no quiz yet, not an error
        }

        Quiz latest = latestOpt.get();
        List<QuizQuestion> questions = quizQuestionRepository.findAllByQuiz_Id(latest.getId());
        List<QuizResult> results = quizResultRepository.findAllByQuiz_Id(latest.getId());

        MyQuizSummaryDTO dto = new MyQuizSummaryDTO();
        dto.setId(latest.getId());
        dto.setTitle(latest.getTitle());
        dto.setDifficulty(latest.getDifficulty());
        dto.setBookId(latest.getBookId());
        dto.setCreatedAt(latest.getCreatedAt());
        dto.setQuestionCount(questions.size());
        dto.setResults(
                results.stream().map(res -> {
                    QuizResultEntryDTO r = new QuizResultEntryDTO();
                    r.setUserId(res.getUser().getId());
                    r.setUsername(res.getUser().getUsername());
                    r.setScoreGot(res.getScoreGot());
                    r.setScoreTotal(res.getScoreTotal());
                    r.setPending(Boolean.FALSE.equals(res.getCompleted()) || res.getScoreGot() == null);
                    return r;
                }).toList()
        );

        return Optional.of(dto);
    }

    @Transactional
    public QuizResultEntryDTO submitQuiz(Long userId, Long quizId, List<Integer> answers) {
        getAuthenticatedUser(userId);

        QuizResult result = quizResultRepository
                .findAllByUser_IdAndQuiz_Id(userId, quizId)
                .stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You have not been sent this quiz"));

        if (Boolean.TRUE.equals(result.getCompleted())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already completed");
        }

        List<QuizQuestion> questions = quizQuestionRepository.findAllByQuiz_Id(quizId);

        int score = 0;
        for (int i = 0; i < Math.min(answers.size(), questions.size()); i++) {
            if (answers.get(i).equals(questions.get(i).getCorrectOption())) {
                score++;
            }
        }

        result.setScoreGot(score);
        result.setScoreTotal(questions.size());
        result.setCompleted(true);
        quizResultRepository.save(result);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getLeaderboard() != null) {
            user.getLeaderboard().addQuizPoints((long) score);
        }

        QuizResultEntryDTO dto = new QuizResultEntryDTO();
        dto.setUserId(userId);
        dto.setScoreGot(score);
        dto.setScoreTotal(questions.size());
        dto.setPending(false);
        return dto;
    }

    @Transactional
    public void acceptQuiz(Long userId, Long quizId) {
        getAuthenticatedUser(userId);

        QuizResult result = quizResultRepository
                .findAllByUser_IdAndQuiz_Id(userId, quizId)
                .stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No quiz challenge found for this user"));

        result.setAccepted(true);
        quizResultRepository.save(result);
    }

    @Transactional(readOnly = true)
    public QuizTakeDTO getQuizForTaking(Long userId, Long quizId) {
        getAuthenticatedUser(userId);

        quizResultRepository
                .findAllByUser_IdAndQuiz_Id(userId, quizId)
                .stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You have not been sent this quiz"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found"));

        List<QuizQuestion> questions = quizQuestionRepository.findAllByQuiz_Id(quizId);

        QuizTakeDTO dto = new QuizTakeDTO();
        dto.setQuizId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDifficulty(quiz.getDifficulty());
        dto.setQuestions(questions.stream().map(q -> {
            QuizTakeQuestionDTO qDto = new QuizTakeQuestionDTO();
            qDto.setId(q.getId());
            qDto.setQuestionText(q.getQuestionText());
            qDto.setOption1(q.getOption1());
            qDto.setOption2(q.getOption2());
            qDto.setOption3(q.getOption3());
            qDto.setOption4(q.getOption4());
            return qDto;
        }).toList());

        return dto;
    }
}