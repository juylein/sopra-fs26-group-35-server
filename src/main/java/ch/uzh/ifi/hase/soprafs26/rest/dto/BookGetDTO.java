 package ch.uzh.ifi.hase.soprafs26.rest.dto;

 import java.util.List;

 public class BookGetDTO {
     private String id;
     private String name;
     private List<String> authors;
     private Long pages;
     private Integer releaseYear;
     private String genre;
     private String description;

     public String getId() { return id; }
     public void setId(String id) { this.id = id; }

     public String getName() { return name; }
     public void setName(String name) { this.name = name; }

     public List<String> getAuthors() { return authors; }
     public void setAuthors(List<String> authors) { this.authors = authors; }

     public Long getPages() { return pages; }
     public void setPages(Long pages) { this.pages = pages; }

     public Integer getReleaseYear() { return releaseYear; }
     public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

     public String getGenre() { return genre; }
     public void setGenre(String genre) { this.genre = genre; }
     
     public String getDescription() { return description; }
     public void setDescription(String description) { this.description = description; }
 }