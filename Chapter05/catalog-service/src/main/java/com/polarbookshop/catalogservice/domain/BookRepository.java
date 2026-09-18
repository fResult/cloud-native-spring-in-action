package com.polarbookshop.catalogservice.domain;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface BookRepository extends CrudRepository<Book, Long> {
  List<Book> findAll();

  Option<Book> findByIsbn(String isbn);

  boolean existsByIsbn(String isbn);

  @Modifying
  @Query("DELETE FROM books WHERE isbn = :isbn")
  void deleteByIsbn(String isbn);
}
