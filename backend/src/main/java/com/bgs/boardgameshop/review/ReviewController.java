package com.bgs.boardgameshop.review;

import com.bgs.boardgameshop.review.dto.CreateReviewRequest;
import com.bgs.boardgameshop.review.dto.ReviewResponse;
import com.bgs.boardgameshop.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> getReviews(@PathVariable Long gameId) {
        return reviewService.getReviewsForGame(gameId);
    }

    @GetMapping("/can-review")
    public boolean canReview(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        return reviewService.canReview(gameId, principal.getUser().getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        return reviewService.createReview(gameId, principal.getUser().getId(), request);
    }
}
