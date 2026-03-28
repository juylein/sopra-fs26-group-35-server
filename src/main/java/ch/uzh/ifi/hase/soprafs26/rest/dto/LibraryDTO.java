 package ch.uzh.ifi.hase.soprafs26.rest.dto;

 import java.util.List;

 public class LibraryDTO {
     private List<ShelfGetDTO> shelves;

     public List<ShelfGetDTO> getShelves() { return shelves; }
     public void setShelves(List<ShelfGetDTO> shelves) { this.shelves = shelves; }
 }