package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;
import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

public class ShelfBookPutDTO {
    private BookStatus status;

    public BookStatus getStatus(){ return status; }
    public void setStatus(BookStatus status) { this.status = status; } 
}