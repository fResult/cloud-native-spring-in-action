package com.polarbookshop.catalogservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.polarbookshop.catalogservice.common.config.DataConfiguration;
import java.math.BigDecimal;
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

@DataJdbcTest
@Testcontainers
@Import(DataConfiguration.class)
class BookRepositoryJdbcTest {
  @Container
  @ServiceConnection
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private JdbcAggregateTemplate jdbcAggregateTemplate;

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
}
