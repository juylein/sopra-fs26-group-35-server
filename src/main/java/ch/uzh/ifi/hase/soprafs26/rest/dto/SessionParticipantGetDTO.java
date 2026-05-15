package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionParticipantGetDTO {

    private Long shelfBookId;
    private Long pagesRead;
    private UserGetDTO user;
    private BookGetDTO book;

    public Long getShelfBookId() {
        return shelfBookId;
    }

    public void setShelfBookId(Long shelfBookId) {
        this.shelfBookId = shelfBookId;
    }

    public Long getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Long pagesRead) {
        this.pagesRead = pagesRead;
    }

    public UserGetDTO getUser() {
        return user;
    }

    public void setUser(UserGetDTO user) {
        this.user = user;
    }

    public BookGetDTO getBook() {
        return book;
    }

    public void setBook(BookGetDTO book) {
        this.book = book;
    }
}