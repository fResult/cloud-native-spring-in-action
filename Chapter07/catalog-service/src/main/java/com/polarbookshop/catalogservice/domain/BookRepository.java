package com.polarbookshop.catalogservice.domain;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BookRepository extends CrudRepository<Book, Long> {
  @Override
  List<Book> findAll();

  default Option<Book> findBookById(Long id) {
    return Option.ofOptional(findById(id));
  }

  Option<Book> findByIsbn(String isbn);

  boolean existsByIsbn(String isbn);

  @Modifying
  @Transactional
  @Query("DELETE FROM books WHERE isbn = :isbn")
  void deleteByIsbn(String isbn);
}
