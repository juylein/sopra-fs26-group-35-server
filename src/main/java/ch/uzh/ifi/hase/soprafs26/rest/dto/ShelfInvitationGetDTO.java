package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class ShelfInvitationGetDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private Long shelfId;
    private String shelfName;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }

    public String getShelfName() { return shelfName; }
    public void setShelfName(String shelfName) { this.shelfName = shelfName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
