package com.polarbookshop.catalogservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;

import io.vavr.control.Option;
import java.math.BigDecimal;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  @Test
  void whenBookToCreateAlreadyExistsThenThrows() {
    // Given
    val bookIsbn = "1234561232";
    val bookToCreate =
        Book.of(bookIsbn, "Title", "Author", BigDecimal.valueOf(9.90), "Polarsophia");
    given(bookRepository.existsByIsbn(bookIsbn)).willReturn(true);

    // When
    final Executable executable = () -> bookService.addBookToCatalog(bookToCreate);

    // Then
    val exception = assertThrowsExactly(BookAlreadyExistsException.class, executable);
    assertEquals(
        exception.getMessage(), "A book with ISBN %s is already exists.".formatted(bookIsbn));
  }

  @Test
  void whenBookToReadDoesNotExistsThenThrows() {
    // Given
    val bookIsbn = "1234561232";
    given(bookRepository.findByIsbn(bookIsbn)).willReturn(Option.none());

    // When
    final Executable executable = () -> bookService.viewBookDetails(bookIsbn);

    // Then
    val exception = assertThrowsExactly(BookNotFoundException.class, executable);
    assertEquals(
        exception.getMessage(), "The book with ISBN %s was not found.".formatted(bookIsbn));
  }
}
