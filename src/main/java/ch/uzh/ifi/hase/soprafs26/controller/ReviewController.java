package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ReviewService;

@RestController
@RequestMapping("/users/{userId}/reviews/{reviewId}")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editReview(@PathVariable Long userId, @PathVariable Long reviewId, @RequestBody ReviewPostDTO reviewPostDTO) {
        reviewService.editReview(userId, reviewId, reviewPostDTO);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long userId, @PathVariable Long reviewId){
        reviewService.deleteReview(userId, reviewId);
    }
}
