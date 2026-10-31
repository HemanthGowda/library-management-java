package com.library.management.book.service;

import com.library.management.book.dto.BookResponse;
import com.library.management.book.dto.UpdateBookRequest;
import com.library.management.book.model.BookDocument;
import com.library.management.book.repository.BookRepository;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ConflictException;
import com.library.management.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public BookResponse update(String bookId, UpdateBookRequest request) {
        BookDocument book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        String normalizedIsbn = normalizeIsbn(request.isbn());

        if (bookRepository.existsByIsbnAndIdNot(normalizedIsbn, bookId)) {
            throw new ConflictException("ISBN is already assigned to another book");
        }

        if (request.availableCopies() > request.totalCopies()) {
            throw new BadRequestException("Available copies cannot exceed total copies");
        }

        book.setTitle(request.title().trim());
        book.setAuthorName(request.authorName().trim());
        book.setIsbn(normalizedIsbn);
        book.setDescription(request.description() == null ? null : request.description().trim());
        book.setPublicationYear(request.publicationYear());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());

        BookDocument saved = bookRepository.save(book);
        return bookMapper.toResponse(saved);
    }

    private String normalizeIsbn(String isbn) {
        return isbn.replace("-", "").trim().toUpperCase();
    }
}
