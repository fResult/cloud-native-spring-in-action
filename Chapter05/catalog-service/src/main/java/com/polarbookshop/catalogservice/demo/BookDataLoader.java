package com.polarbookshop.catalogservice.demo;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookRepository;
import io.vavr.collection.List;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("testdata")
@RequiredArgsConstructor
public class BookDataLoader {
  private final BookRepository bookRepository;

  @EventListener(ApplicationReadyEvent.class)
  public void loadBooks() {
    val book1 =
        Book.of("1234567891", "Northern Lights", "Lyra Silverstar", BigDecimal.valueOf(9.90));
    val book2 = Book.of("1234567892", "Polar Journey", "Iorek Polason", BigDecimal.valueOf(12.90));
    List.of(book1, book2).forEach(bookRepository::save);
  }
}
