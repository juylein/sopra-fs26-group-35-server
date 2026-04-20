package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ShelfBookGetDTO {
    private Long id;
    private BookGetDTO book;
    private Long pagesRead;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public BookGetDTO getBook()
    {
        return book;
    }

    public void setBook(BookGetDTO book)
    {
        this.book = book;
    }

    public Long getPagesRead()
    {
        return pagesRead;
    }

    public void setPagesRead(Long pagesRead)
    {
        this.pagesRead = pagesRead;
    }  

}