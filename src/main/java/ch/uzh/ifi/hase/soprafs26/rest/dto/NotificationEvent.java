package ch.uzh.ifi.hase.soprafs26.rest.dto;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationEventType;

public class NotificationEvent {
    private NotificationEventType type;
    private Object payload;

    public NotificationEvent(NotificationEventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public NotificationEventType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}