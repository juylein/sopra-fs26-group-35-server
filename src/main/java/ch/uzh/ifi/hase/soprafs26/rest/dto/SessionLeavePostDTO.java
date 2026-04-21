package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionLeavePostDTO {

    private Long shelfBookId;
    private Long pagesRead;

    public Long getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Long pagesRead) {
        this.pagesRead = pagesRead;
    }

    public Long getShelfBookId()
    {
        return this.shelfBookId;
    }

    public void setShelfBookId(Long shelfBookId)
    {
        this.shelfBookId = shelfBookId;
    }
}
