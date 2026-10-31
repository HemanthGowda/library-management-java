package com.library.management.book.repository;

import com.library.management.book.model.BookDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BookRepository extends MongoRepository<BookDocument, String> {

    Optional<BookDocument> findByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, String id);
}
