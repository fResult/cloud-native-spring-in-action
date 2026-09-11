package com.polarbookshop.catalogservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import io.vavr.collection.HashSet;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BookValidationTests {
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
      validator = validatorFactory.getValidator();
    }
  }

  @Test
  void whenAllFieldsCorrectThenValidationSucceeds() {
    // Given
    val book = new Book("1234567890", "Title", "Author", BigDecimal.TEN);

    // When
    val violations = HashSet.ofAll(validator.validate(book));

    // Then
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenIsbnDefinedButIncorrectThenValidationFails() {
    // Given
    val expectedViolationSize = 1;
    val expectedErrorMessage = "The ISBN format must be valid.";
    val book = new Book("a234567890", "Title", "Author", BigDecimal.TEN);

    // When
    val violations = HashSet.ofAll(validator.validate(book));

    // Then
    assertEquals(expectedViolationSize, violations.size());
    assertEquals(expectedErrorMessage, violations.iterator().next().getMessage());
  }
}
