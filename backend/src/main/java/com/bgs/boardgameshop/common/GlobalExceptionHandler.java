package com.bgs.boardgameshop.common;

import com.bgs.boardgameshop.favorite.ShareLinkNotFoundException;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.TagAlreadyExistsException;
import com.bgs.boardgameshop.game.TagNotFoundException;
import com.bgs.boardgameshop.order.InsufficientStockException;
import com.bgs.boardgameshop.order.InvalidOrderStatusTransitionException;
import com.bgs.boardgameshop.order.InvoiceNotAvailableException;
import com.bgs.boardgameshop.order.OrderNotFoundException;
import com.bgs.boardgameshop.payment.OrderNotPayableException;
import com.bgs.boardgameshop.review.ReviewNotAllowedException;
import com.bgs.boardgameshop.security.TooManyLoginAttemptsException;
import com.bgs.boardgameshop.user.EmailAlreadyUsedException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            GameNotFoundException.class,
            OrderNotFoundException.class,
            TagNotFoundException.class,
            ShareLinkNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ApiError> handleTooManyLoginAttempts(TooManyLoginAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiError.of(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden", "Accès refusé"));
    }

    @ExceptionHandler(OrderNotPayableException.class)
    public ResponseEntity<ApiError> handleOrderNotPayable(OrderNotPayableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage()));
    }

    @ExceptionHandler({
            TagAlreadyExistsException.class,
            InvalidOrderStatusTransitionException.class,
            ReviewNotAllowedException.class,
            InvoiceNotAvailableException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ApiError> handleStripeException(StripeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.of(HttpStatus.BAD_GATEWAY.value(), "Bad Gateway",
                        "Le service de paiement est indisponible, réessayez plus tard"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        "Données invalides", details));
    }
}
