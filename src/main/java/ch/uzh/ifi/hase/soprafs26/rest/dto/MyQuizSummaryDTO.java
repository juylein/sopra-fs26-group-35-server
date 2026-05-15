package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MyQuizSummaryDTO { //	Latest quiz summary for creator

    private Long id;
    private String title;
    private String difficulty;
    private Long bookId;
    private LocalDateTime createdAt;
    private int questionCount;
    private List<QuizResultEntryDTO> results;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public List<QuizResultEntryDTO> getResults() { return results; }
    public void setResults(List<QuizResultEntryDTO> results) { this.results = results; }
}