package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizPostDTO { 	//Create quiz request

    private String title;
    private String difficulty;
    private String bookId;
    private List<QuizQuestionDTO> questions;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public List<QuizQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionDTO> questions) { this.questions = questions; }
}