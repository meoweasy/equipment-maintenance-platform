package com.example.equipment.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RequiredFieldException.class, InvalidIdException.class,
            BlankFieldException.class, ValueTooSmallException.class,
            ValueTooLargeException.class, InvalidFieldValueException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(EtagRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handlePreconditionRequired(EtagRequiredException exception,
                                                                       HttpServletRequest request) {
        return build(HttpStatus.PRECONDITION_REQUIRED, exception.getMessage(), request);
    }

    @ExceptionHandler({EtagMismatchException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> handlePreconditionFailed(RuntimeException exception,
                                                                     HttpServletRequest request) {
        String message = exception instanceof EtagMismatchException
                ? exception.getMessage()
                : "Resource was changed by another request";
        return build(HttpStatus.PRECONDITION_FAILED, message, request);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception,
                                                           HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({ResourceAlreadyExistsException.class, StatusChangeNotAllowedException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception,
                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                             HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getParameterName() + " is required", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getName() + " has an invalid value", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Request body is invalid", request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()
        ));
    }
}
