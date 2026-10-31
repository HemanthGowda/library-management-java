package com.library.management.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBookRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,

        @NotBlank(message = "Author name is required")
        @Size(min = 1, max = 160, message = "Author name must be between 1 and 160 characters")
        String authorName,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(97[89])?\\d{9}[\\dXx]$", message = "ISBN must be a valid ISBN-10 or ISBN-13 without hyphens")
        String isbn,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be 1450 or later")
        @Max(value = 2100, message = "Publication year must be 2100 or earlier")
        Integer publicationYear,

        @NotNull(message = "Total copies is required")
        @Min(value = 1, message = "Total copies must be at least 1")
        @Max(value = 10000, message = "Total copies must be at most 10000")
        Integer totalCopies,

        @NotNull(message = "Available copies is required")
        @Min(value = 0, message = "Available copies cannot be negative")
        @Max(value = 10000, message = "Available copies must be at most 10000")
        Integer availableCopies
) {
}
