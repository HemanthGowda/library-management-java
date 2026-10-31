package com.library.management.book.controller;

import com.library.management.book.dto.BookResponse;
import com.library.management.book.dto.UpdateBookRequest;
import com.library.management.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book catalog APIs. Only update is implemented as a reference slice.")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Update a book",
            description = "Fully replaces mutable book fields. Requires ADMIN or LIBRARIAN role."
    )
    @ApiResponse(responseCode = "200", description = "Book updated",
            content = @Content(schema = @Schema(implementation = BookResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content)
    @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "ISBN conflict", content = @Content)
    public BookResponse update(@PathVariable("id") String id, @Valid @RequestBody UpdateBookRequest request) {
        return bookService.update(id, request);
    }
}
