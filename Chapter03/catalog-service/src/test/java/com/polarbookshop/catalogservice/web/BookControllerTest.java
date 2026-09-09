package com.polarbookshop.catalogservice.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.BookService;
import java.time.Clock;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @MockitoBean
  private Clock clock;

  @Test
  void whenGetBookNotExistingThenSHouldReturn404() throws Exception {
    // Given
    val isbn = "7373731394";
    given(bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);
    given(clock.instant()).willReturn(Instant.now());

    // When
    val result = mockMvc.perform(get("/books/{isbn}", isbn));

    // Then
    result.andExpect(status().isNotFound());
  }
}
