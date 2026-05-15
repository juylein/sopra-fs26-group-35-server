package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizTakeDTO { //Take quiz response (no correctOption)
    private Long quizId;
    private String title;
    private String difficulty;
    private List<QuizTakeQuestionDTO> questions;

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<QuizTakeQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuizTakeQuestionDTO> questions) { this.questions = questions; }
}