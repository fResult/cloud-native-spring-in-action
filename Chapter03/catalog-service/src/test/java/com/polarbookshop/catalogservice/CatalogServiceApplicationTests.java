package com.polarbookshop.catalogservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.polarbookshop.catalogservice.domain.Book;
import java.math.BigDecimal;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {
  private WebTestClient webTestClient;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    webTestClient =
        MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
  }

  @Test
  void whenPostRequestThenBookCreated() {
    // Given
    val expectedBook = new Book("1231231231", "Title", "Author", BigDecimal.TEN);

    // When
    val response = webTestClient.post().uri("/books").bodyValue(expectedBook).exchange();

    // Then
    response.expectStatus().isCreated().expectBody(Book.class).value(actualBook -> {
      assertNotNull(actualBook);
      assertEquals(expectedBook.isbn(), actualBook.isbn());
    });
  }
}
