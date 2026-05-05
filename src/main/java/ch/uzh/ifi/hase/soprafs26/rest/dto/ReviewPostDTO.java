package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ReviewPostDTO{
    
    private Integer rating;
    private String review;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}
