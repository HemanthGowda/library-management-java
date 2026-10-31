package com.library.management.book.service;

import com.library.management.book.dto.BookResponse;
import com.library.management.book.dto.UpdateBookRequest;
import com.library.management.book.model.BookDocument;
import com.library.management.book.repository.BookRepository;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ConflictException;
import com.library.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, new BookMapper());
    }

    @Test
    void updateReplacesMutableFields() {
        BookDocument existing = new BookDocument();
        existing.setId("book-1");
        existing.setTitle("Old Title");
        existing.setAuthorName("Old Author");
        existing.setIsbn("9780134685991");
        existing.setTotalCopies(2);
        existing.setAvailableCopies(1);

        UpdateBookRequest request = new UpdateBookRequest(
                "New Title",
                "New Author",
                "9780596009205",
                "Updated description",
                2020,
                5,
                4
        );

        when(bookRepository.findById("book-1")).thenReturn(Optional.of(existing));
        when(bookRepository.existsByIsbnAndIdNot("9780596009205", "book-1")).thenReturn(false);
        when(bookRepository.save(any(BookDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.update("book-1", request);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.authorName()).isEqualTo("New Author");
        assertThat(response.isbn()).isEqualTo("9780596009205");
        assertThat(response.totalCopies()).isEqualTo(5);
        assertThat(response.availableCopies()).isEqualTo(4);
    }

    @Test
    void updateFailsWhenBookMissing() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update("missing", sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateFailsWhenIsbnConflicts() {
        BookDocument existing = new BookDocument();
        existing.setId("book-1");
        existing.setIsbn("9780134685991");

        when(bookRepository.findById("book-1")).thenReturn(Optional.of(existing));
        when(bookRepository.existsByIsbnAndIdNot("9780596009205", "book-1")).thenReturn(true);

        assertThatThrownBy(() -> bookService.update("book-1", sampleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    void updateFailsWhenAvailableExceedsTotal() {
        BookDocument existing = new BookDocument();
        existing.setId("book-1");
        existing.setIsbn("9780134685991");

        UpdateBookRequest request = new UpdateBookRequest(
                "Title",
                "Author",
                "9780596009205",
                "Desc",
                2020,
                2,
                3
        );

        when(bookRepository.findById("book-1")).thenReturn(Optional.of(existing));
        when(bookRepository.existsByIsbnAndIdNot("9780596009205", "book-1")).thenReturn(false);

        assertThatThrownBy(() -> bookService.update("book-1", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Available copies");
    }

    private UpdateBookRequest sampleRequest() {
        return new UpdateBookRequest(
                "Title",
                "Author",
                "9780596009205",
                "Desc",
                2020,
                5,
                4
        );
    }
}
