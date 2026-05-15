package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class QuizResultEntryDTO {
    private Long userId;
    private String username;
    private Integer scoreGot;
    private Integer scoreTotal;
    private boolean pending;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getScoreGot() { return scoreGot; }
    public void setScoreGot(Integer scoreGot) { this.scoreGot = scoreGot; }

    public Integer getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(Integer scoreTotal) { this.scoreTotal = scoreTotal; }

    public boolean isPending() { return pending; }
    public void setPending(boolean pending) { this.pending = pending; }
}