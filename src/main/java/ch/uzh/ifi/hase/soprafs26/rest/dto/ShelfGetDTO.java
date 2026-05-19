 package ch.uzh.ifi.hase.soprafs26.rest.dto;

 import java.util.List;

 public class ShelfGetDTO {
     private Long id;
     private String name;
     private boolean shared;
     private Long ownerId;
     private List<Long> memberIds;
     private List<String> memberUsernames;
     private List<ShelfBookGetDTO> shelfBooks;

     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }

     public String getName() { return name; }
     public void setName(String name) { this.name = name; }

     public boolean isShared() { return shared; }
     public void setShared(boolean shared) { this.shared = shared; }

     public Long getOwnerId() { return ownerId; }
     public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

     public List<Long> getMemberIds() { return memberIds; }
     public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }

     public List<String> getMemberUsernames() { return memberUsernames; }
     public void setMemberUsernames(List<String> memberUsernames) { this.memberUsernames = memberUsernames; }

     public List<ShelfBookGetDTO> getShelfBooks() { return shelfBooks; }
     public void setShelfBooks(List<ShelfBookGetDTO> shelfBooks) { this.shelfBooks = shelfBooks; }
 }