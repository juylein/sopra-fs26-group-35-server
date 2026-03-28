 package ch.uzh.ifi.hase.soprafs26.rest.dto;

 import java.util.List;

 public class ShelfGetDTO {
     private Long id;
     private String name;
     private boolean shared;
     private List<BookGetDTO> books;

     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }

     public String getName() { return name; }
     public void setName(String name) { this.name = name; }

     public boolean isShared() { return shared; }
     public void setShared(boolean shared) { this.shared = shared; }
     
     public List<BookGetDTO> getBooks() { return books; }
     public void setBooks(List<BookGetDTO> books) { this.books = books; }
 }