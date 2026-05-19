package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ReceivedQuizResultDTO {
    private Long quizId;
    private String quizTitle;
    private String difficulty;
    private Integer scoreGot;
    private Integer scoreTotal;
    private boolean pending;
    private String bookTitle;
    private String bookCoverUrl;

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getScoreGot() { return scoreGot; }
    public void setScoreGot(Integer scoreGot) { this.scoreGot = scoreGot; }

    public Integer getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(Integer scoreTotal) { this.scoreTotal = scoreTotal; }

    public boolean isPending() { return pending; }
    public void setPending(boolean pending) { this.pending = pending; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookCoverUrl() { return bookCoverUrl; }
    public void setBookCoverUrl(String bookCoverUrl) { this.bookCoverUrl = bookCoverUrl; }
}