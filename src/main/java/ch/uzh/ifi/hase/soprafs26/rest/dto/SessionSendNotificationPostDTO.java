package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class SessionSendNotificationPostDTO {
    private List<Long> participantIds;
    private Long shelfBookId;

    public Long getShelfBookId()
    {
        return shelfBookId;
    }

    public List<Long> getParticipantIds()
    {
        return participantIds;
    }
}
