package com.polarbookshop.catalogservice.persistence;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookRepository;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookRepository implements BookRepository {
  private static final Map<String, Book> books = new ConcurrentHashMap<>();

  @Override
  public List<Book> findAll() {
    return List.ofAll(books.values());
  }

  @Override
  public Option<Book> findByIsbn(String isbn) {
    return existsByIsbn(isbn) ? Option.of(books.get(isbn)) : Option.none();
  }

  @Override
  public boolean existsByIsbn(String isbn) {
    return books.get(isbn) != null;
  }

  @Override
  public Book save(Book book) {
    books.put(book.isbn(), book);
    return book;
  }

  @Override
  public void deleteByIsbn(String isbn) {
    books.remove(isbn);
  }
}
