package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionParticipantPostDTO {

	private Long userId;
	private Long shelfBookId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getShelfBookId() {
		return shelfBookId;
	}

	public void setShelfBookId(Long shelfBookId) {
		this.shelfBookId = shelfBookId;
	}
}
