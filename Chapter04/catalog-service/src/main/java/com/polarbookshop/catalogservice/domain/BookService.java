package com.polarbookshop.catalogservice.domain;

import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

  public Book addBookToCatalog(Book book) {
    if (bookRepository.existsByIsbn(book.isbn())) {
      throw new BookAlreadyExistsException(book.isbn());
    }

    return bookRepository.save(book);
  }

  public void removeBookFromCatalog(String isbn) {
    bookRepository.deleteByIsbn(isbn);
  }

  public Book editBookDetails(String isbn, Book book) {
    return bookRepository
        .findByIsbn(isbn)
        .map(existingBook -> {
          val bookToUpdate = existingBook
              .withTitle(book.title())
              .withAuthor(book.author())
              .withPrice(book.price());

          return bookRepository.save(bookToUpdate);
        })
        .getOrElse(() -> addBookToCatalog(book));
  }
}
