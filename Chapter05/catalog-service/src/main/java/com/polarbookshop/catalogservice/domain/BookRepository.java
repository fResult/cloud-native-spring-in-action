package com.polarbookshop.catalogservice.domain;

import io.vavr.collection.List;
import io.vavr.control.Option;

public interface BookRepository {
  List<Book> findAll();

  Option<Book> findByIsbn(String isbn);

  boolean existsByIsbn(String isbn);

  Book save(Book book);

  void deleteByIsbn(String isbn);
}
