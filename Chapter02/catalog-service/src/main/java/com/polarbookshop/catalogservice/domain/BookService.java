package com.polarbookshop.catalogservice.domain;

import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
  private final BookRepository bookRepository;

  public List<Book> viewBookList() {
    return bookRepository.findAll();
  }

  public Book viewBookDetails(String isbn) {
    return bookRepository.findByIsbn(isbn).getOrElseThrow(() -> new BookNotFoundException(isbn));
  }
}
