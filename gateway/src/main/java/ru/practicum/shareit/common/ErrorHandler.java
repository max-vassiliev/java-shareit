package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.exception.BookingStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        List<String> details = new ArrayList<>();
        for (ObjectError error : exception.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        log.warn("400: {}", details);
        return new ErrorResponse("400 - Validation Error", details.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        List<String> errors = new ArrayList<>(violations.size());
        for (ConstraintViolation<?> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        log.warn("400: " + errors);

        return new ErrorResponse("400 - Validation Error", String.join(",", errors));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingStatusException(final BookingStatusException exception) {
        log.warn("400: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalException(final Throwable exception) {
        log.warn("500: {} | {}", exception.getMessage(), exception.getStackTrace());
        return new ErrorResponse("500 - Internal Server Error", exception.getMessage());
    }
}
