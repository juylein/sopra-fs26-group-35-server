package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizSendDTO {
    private List<Long> friendIds;

    public List<Long> getFriendIds() { return friendIds; }
    public void setFriendIds(List<Long> friendIds) { this.friendIds = friendIds; }
}
