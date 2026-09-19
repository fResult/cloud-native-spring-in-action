package com.polarbookshop.catalogservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.polarbookshop.catalogservice.common.config.DataConfiguration;
import io.vavr.collection.List;
import java.math.BigDecimal;
import java.util.function.Predicate;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJdbcTest
@Testcontainers
@Import(DataConfiguration.class)
class BookRepositoryJdbcTest {
  @Container
  @ServiceConnection
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private JdbcAggregateTemplate jdbcAggregateTemplate;

  @Test
  void findAllBooks() {
    // Given
    val book1 = Book.of("1234561235", "Title", "Author", BigDecimal.valueOf(12.90));
    val book2 = Book.of("1234561236", "Another Title", "Author", BigDecimal.valueOf(12.90));
    jdbcAggregateTemplate.insertAll(List.of(book1, book2));

    // When
    val actualBooks = bookRepository.findAll();
    //    val actualBookIsbns = actualBooks.map(Book::isbn);

    // Then
    assertThat(actualBooks.filter(isBook1OrBook2(book1.isbn(), book2.isbn()))).hasSize(2);
  }

  private Predicate<Book> isBook1OrBook2(String isbn1, String isbn2) {
    return book -> book.isbn().equals(isbn1) || book.isbn().equals(isbn2);
  }

  @Test
  void findBookByIsbnWhenExisting() {
    // Given
    val bookIsbn = "1234561237";
    val book = Book.of(bookIsbn, "Title", "Author", BigDecimal.valueOf(12.90));
    jdbcAggregateTemplate.insert(book);

    // When
    val actualBook = bookRepository.findByIsbn(bookIsbn);

    // Then
    assertTrue(actualBook.isDefined());
    assertEquals(book.isbn(), actualBook.get().isbn());
  }

  @Test
  void findBookByIsbnWhenNotExisting() {
    // When
    val existing = bookRepository.existsByIsbn("1234561240");

    // Then
    assertFalse(existing);
  }

  @Test
  void findBookByIdWhenExisting() {
    // Given
    val bookId = 42L;
    val bookToCreate =
        new Book(bookId, "1234567890", "Title", "Author", BigDecimal.valueOf(12.90), null, null, 0);
    jdbcAggregateTemplate.insert(bookToCreate);

    // When
    val existing = bookRepository.findBookById(bookId);

    // Then
    assertTrue(existing.isDefined());
    assertEquals(bookId, existing.get().id());
  }

  @Test
  void findBookByIdWhenNotExisting() {
    // When
    val existing = bookRepository.findBookById(42L);

    // Then
    assertTrue(existing.isEmpty());
  }

  @Test
  void deleteByIsbn() {
    // Given
    val bookIsbn = "1234561241";
    val bookToCreate = Book.of(bookIsbn, "Title", "Author", BigDecimal.valueOf(12.90));
    val persistedBook = jdbcAggregateTemplate.insert(bookToCreate);

    // When
    bookRepository.deleteByIsbn(bookIsbn);

    // Then
    val maybeBook = bookRepository.findByIsbn(bookIsbn);
    assertTrue(maybeBook.isEmpty());
  }
}
