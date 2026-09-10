package com.polarbookshop.catalogservice.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.polarbookshop.catalogservice.domain.Book;
import java.math.BigDecimal;
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
    val book = new Book("1234567890", "Title", "Author", BigDecimal.TEN);
    val jsonContent = json.write(book);
    assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(book.isbn());
    assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(book.title());
    assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo(book.author());
    assertThat(jsonContent).extractingJsonPathNumberValue("@.price").satisfies(price -> {
      Assertions.assertEquals(price.doubleValue(), book.price().doubleValue());
    });
  }

  @Test
  void testDeserialize() throws Exception {
    val book = new Book("1234567890", "Title", "Author", BigDecimal.TEN);
    val jsonString = """
        {
          "isbn": "1234567890",
          "title": "Title",
          "author": "Author",
          "price": 10
        }
        """;

    assertThat(json.parse(jsonString)).usingRecursiveComparison().isEqualTo(book);
  }
}
