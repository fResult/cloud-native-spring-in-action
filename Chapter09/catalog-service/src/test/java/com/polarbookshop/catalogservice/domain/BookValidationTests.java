package com.polarbookshop.catalogservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.collection.HashSet;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookValidationTests {
  @Nullable
  private Validator validator;

  @BeforeAll
  void setUp() {
    try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
      validator = validatorFactory.getValidator();
    }
  }

  @Test
  void whenAllFieldsCorrectThenValidationSucceeds() {
    // Given
    val book = Book.of("1234567890", "Title", "Author", BigDecimal.TEN, "Polarsophia");

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
    val book = Book.of("a234567890", "Title", "Author", BigDecimal.TEN, "Polarsophia");

    // When
    val violations = HashSet.ofAll(validator.validate(book));

    // Then
    assertEquals(expectedViolationSize, violations.size());
    assertEquals(expectedErrorMessage, violations.iterator().next().getMessage());
  }
}
