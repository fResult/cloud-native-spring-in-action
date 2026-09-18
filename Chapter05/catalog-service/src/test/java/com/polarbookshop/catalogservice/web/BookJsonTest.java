package com.polarbookshop.catalogservice.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.polarbookshop.catalogservice.domain.Book;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class BookJsonTest {
  @Autowired
  private JacksonTester<Book> json;

  @Test
  void testSerialize() throws Exception {
    val now = Instant.now();
    val book = new Book(42L, "1234567890", "Title", "Author", BigDecimal.TEN, now, now, 21);
    val jsonContent = json.write(book);

    assertNotNull(book.id(), "Book ID must not be null");
    assertThat(jsonContent)
        .extractingJsonPathNumberValue("@.id")
        .isEqualTo(book.id().intValue());
    assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(book.isbn());
    assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(book.title());
    assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo(book.author());
    assertThat(jsonContent).extractingJsonPathNumberValue("@.price").satisfies(price -> {
      Assertions.assertEquals(price.doubleValue(), book.price().doubleValue());
    });
    assertThat(jsonContent).extractingJsonPathNumberValue("@.version").isEqualTo(book.version());
  }

  @Test
  void testDeserialize() throws Exception {
    val now = Instant.now();
    val book = new Book(42L, "1234567890", "Title", "Author", BigDecimal.TEN, now, now, 21);
    val jsonString = """
        {
          "id": 42,
          "isbn": "1234567890",
          "title": "Title",
          "author": "Author",
          "price": 10,
          "createdDate": "%s",
          "lastModifiedDate": "%s",
          "version": 21
        }
        """.formatted(now, now);

    assertThat(json.parse(jsonString)).usingRecursiveComparison().isEqualTo(book);
  }
}
