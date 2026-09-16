package com.polarbookshop.catalogservice.persistence;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookRepository;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookRepository implements BookRepository {
  private final AtomicReference<Map<String, Book>> booksRef =
      new AtomicReference<>(HashMap.empty());

  @Override
  public List<Book> findAll() {
    return booksRef.get().values().toList();
  }

  @Override
  public Option<Book> findByIsbn(String isbn) {
    return booksRef.get().get(isbn);
  }

  @Override
  public boolean existsByIsbn(String isbn) {
    return booksRef.get().containsKey(isbn);
  }

  @Override
  public Book save(Book book) {
    booksRef.updateAndGet(books -> books.put(book.isbn(), book));
    return book;
  }

  @Override
  public void deleteByIsbn(String isbn) {
    booksRef.updateAndGet(books -> books.remove(isbn));
  }
}
