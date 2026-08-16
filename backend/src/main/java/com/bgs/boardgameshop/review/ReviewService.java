package com.bgs.boardgameshop.review;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import com.bgs.boardgameshop.review.dto.CreateReviewRequest;
import com.bgs.boardgameshop.review.dto.ReviewResponse;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Un avis n'est autorise que pour un jeu reellement achete (commande PAYEE,
 * EXPEDIEE ou LIVREE contenant ce jeu), et une seule fois par utilisateur.
 */
@Service
public class ReviewService {

    private static final List<OrderStatus> PAID_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);

    private final ReviewRepository reviewRepository;
    private final OrderLineRepository orderLineRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderLineRepository orderLineRepository,
            GameRepository gameRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.orderLineRepository = orderLineRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForGame(Long gameId) {
        return reviewRepository.findByGame_IdOrderByCreatedAtDesc(gameId).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getRecentReviews() {
        return reviewRepository.findTop6ByOrderByCreatedAtDesc().stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canReview(Long gameId, Long userId) {
        boolean purchased = orderLineRepository.existsPurchase(userId, gameId, PAID_STATUSES);
        boolean alreadyReviewed = reviewRepository.existsByUser_IdAndGame_Id(userId, gameId);
        return purchased && !alreadyReviewed;
    }

    @Transactional
    public ReviewResponse createReview(Long gameId, Long userId, CreateReviewRequest request) {
        if (!orderLineRepository.existsPurchase(userId, gameId, PAID_STATUSES)) {
            throw new ReviewNotAllowedException("Vous devez avoir achete ce jeu pour laisser un avis");
        }
        if (reviewRepository.existsByUser_IdAndGame_Id(userId, gameId)) {
            throw new ReviewNotAllowedException("Vous avez deja laisse un avis pour ce jeu");
        }

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + userId));

        Review review = Review.builder()
                .game(game)
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .createdAt(Instant.now())
                .build();
        Review saved = reviewRepository.save(review);

        recomputeGameRating(game);

        return ReviewResponse.fromEntity(saved);
    }

    private void recomputeGameRating(Game game) {
        List<Review> reviews = reviewRepository.findByGame_IdOrderByCreatedAtDesc(game.getId());
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        game.setReviewsAverage(average);
        game.setReviewsCount(reviews.size());
        gameRepository.save(game);
    }
}
