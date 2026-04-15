package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;
import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.NotificationStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Internal Notifications Representation
 * This class composes the internal representation of the notifications and defines how
 * the notifications are stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "notifications")
public class Notifications implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "recipient_id", nullable = false)
	private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

	@Column(nullable = false)
	private String message;

    @Column(nullable = true)
    private Long referenceId;

    @Column(nullable = false)
    private boolean read = false;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public User getRecipient() {
		return recipient;
	}
	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
