package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.ReviewsRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Reviews;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {
    private final ReviewsRepository reviewsRepository;

    @Autowired
    public ReviewService(ReviewsRepository reviewsRepository){
        this.reviewsRepository = reviewsRepository;
    }

    public void editReview(Long userId, Long reviewId, ReviewPostDTO reviewPostDTO){
        //get user from security context
        User user = (User) SecurityContextHolder.getContext()
              .getAuthentication()
              .getPrincipal();
        //to check whether the user passed in the path is the authenticated one
        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this review");
        } 
        
        Reviews review = reviewsRepository.findById(reviewId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        //check whether the user which wrote the review is trying to edit it
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this review");
        }
        
        review.setRating(reviewPostDTO.getRating());
        review.setReview(reviewPostDTO.getReview());
        reviewsRepository.save(review);
    }

    public void deleteReview(Long userId, Long reviewId){
        User user = (User) SecurityContextHolder.getContext()
              .getAuthentication()
              .getPrincipal();

        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this review");
        } 

        Reviews review = reviewsRepository.findById(reviewId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this review");
        }

        reviewsRepository.delete(review);
    }

    public Reviews getReview(Long userId, Long reviewId){
        User user = (User) SecurityContextHolder.getContext()
              .getAuthentication()
              .getPrincipal();

        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this review");
        } 

        Reviews review = reviewsRepository.findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        return review;
    }

}
