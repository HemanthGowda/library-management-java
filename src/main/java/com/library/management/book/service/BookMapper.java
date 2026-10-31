package com.library.management.book.service;

import com.library.management.book.dto.BookResponse;
import com.library.management.book.model.BookDocument;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponse toResponse(BookDocument document) {
        return new BookResponse(
                document.getId(),
                document.getTitle(),
                document.getAuthorName(),
                document.getIsbn(),
                document.getDescription(),
                document.getPublicationYear(),
                document.getTotalCopies(),
                document.getAvailableCopies(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
