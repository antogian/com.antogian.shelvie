package com.antogian.shelvie.books;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getBooks(
            @RequestParam(required = false) BookStatus status) {
        return ResponseEntity.ok(
                bookService.getAllBooks(status).stream()
                        .map(BookResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable UUID id) {
        return bookService.getBook(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse created = BookResponse.from(bookService.createBook(request));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable UUID id,
                                                   @Valid @RequestBody BookUpdateRequest request) {
        if (request.genres() != null && request.genres().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return bookService.updateBook(id, request)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        return bookService.deleteBook(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}