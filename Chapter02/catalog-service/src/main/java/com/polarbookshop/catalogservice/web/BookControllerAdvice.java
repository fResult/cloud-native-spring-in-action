package com.polarbookshop.catalogservice.web;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarbookshop.catalogservice.domain.BookAlreadyExistsException;
import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import io.vavr.collection.TreeMap;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class BookControllerAdvice {
  private final Clock clock;

  private ZonedDateTime getBkkTimestamp() {
    val bangkokZone = ZoneId.of("Asia/Bangkok");
    return clock.instant().atZone(bangkokZone);
  }

  @ExceptionHandler(BookNotFoundException.class)
  public ProblemDetail bookNotFoundHandler(BookNotFoundException ex) {
    val detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    detail.setProperty("timestamp", getBkkTimestamp());

    return detail;
  }

  @ExceptionHandler(BookAlreadyExistsException.class)
  public ProblemDetail bookAlreadyExistsHandler(BookAlreadyExistsException ex) {
    val detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    detail.setProperty("timestamp", getBkkTimestamp());

    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
    val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    val target = ex.getBindingResult().getTarget();

    val validationErrors = io.vavr.collection.List.ofAll(ex.getBindingResult().getFieldErrors())
        .foldLeft(TreeMap.<String, Object>empty(), accumulateValidationErrorWith(target));

    log.info("Invalid request body: {}", validationErrors);

    detail.setProperty("arguments", validationErrors);
    detail.setProperty("timestamp", getBkkTimestamp());

    return detail;
  }

  private BiFunction<TreeMap<String, Object>, FieldError, TreeMap<String, Object>>
      accumulateValidationErrorWith(@Nullable Object target) {
    return (map, error) -> {
      val fieldName = resolveJsonFieldName(target, error.getField());
      return map.put(fieldName, createErrorDetail(error.getDefaultMessage()));
    };
  }

  private String resolveJsonFieldName(@Nullable Object target, String fieldName) {
    if (target == null) return fieldName;

    val field = ReflectionUtils.findField(target.getClass(), fieldName);
    if (field != null) {
      val annotation = field.getAnnotation(JsonProperty.class);

      if (annotation != null && !annotation.value().isEmpty()) {
        return annotation.value();
      }
    }

    return fieldName;
  }

  private String createErrorDetail(@Nullable String message) {
    return requireNonNullElse(message, "");
  }
}
