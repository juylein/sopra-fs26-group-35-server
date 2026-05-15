package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionReadPagePutDTO {
    private Long numberOfPages;

    public Long getNumberOfPages()
    {
        return numberOfPages;
    }

    public void setNumberOfPages(Long numberOfPages) 
    {
      this.numberOfPages = numberOfPages;
    }
}
