package com.library.management.book.dto;

import java.time.Instant;

public record BookResponse(
        String id,
        String title,
        String authorName,
        String isbn,
        String description,
        Integer publicationYear,
        Integer totalCopies,
        Integer availableCopies,
        Instant createdAt,
        Instant updatedAt
) {
}
