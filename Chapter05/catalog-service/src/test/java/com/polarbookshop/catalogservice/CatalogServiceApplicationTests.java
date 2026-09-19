package com.polarbookshop.catalogservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.polarbookshop.catalogservice.domain.Book;
import java.math.BigDecimal;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {
  private WebTestClient webTestClient;

  @Container
  @ServiceConnection
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    webTestClient =
        MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
  }

  private ResponseSpec postBookCreationFor(Book book) {
    return webTestClient.post().uri("/books").bodyValue(book).exchange();
  }

  private ResponseSpec getBookRetrievingFor(String isbn) {
    return webTestClient.get().uri("/books/{isbn}", isbn).exchange();
  }

  @Test
  void whenGetRequestWithIdThenBookReturned() {
    // Given
    val bookIsbn = "1231231230";
    val bookToCreate = Book.of(bookIsbn, "Title", "Author", BigDecimal.valueOf(9.90));

    // When
    val createdBookResponse = postBookCreationFor(bookToCreate);
    val bookResponse = getBookRetrievingFor(bookIsbn);

    // Then
    val expectedCreatedBook = createdBookResponse
        .expectStatus()
        .isCreated()
        .expectBody(Book.class)
        .value(Assertions::assertNotNull)
        .returnResult()
        .getResponseBody();
    assertNotNull(expectedCreatedBook);

    bookResponse.expectStatus().isOk().expectBody(Book.class).value(actualBook -> {
      assertNotNull(actualBook);
      assertEquals(expectedCreatedBook.isbn(), actualBook.isbn());
    });
  }

  @Test
  void whenPostRequestThenBookCreated() {
    // Given
    val expectedBook = Book.of("1231231231", "Title", "Author", BigDecimal.TEN);

    // When
    val response = postBookCreationFor(expectedBook);

    // Then
    response.expectStatus().isCreated().expectBody(Book.class).value(actualBook -> {
      assertNotNull(actualBook);
      assertEquals(expectedBook.isbn(), actualBook.isbn());
    });
  }
}
