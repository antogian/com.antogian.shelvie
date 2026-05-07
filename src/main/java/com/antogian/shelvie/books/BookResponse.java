package com.antogian.shelvie.books;

import java.util.Set;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String isbn,
        BookStatus status,
        Set<String> genres
) {
    public static BookResponse from(BookRecord book) {
        return new BookResponse(
                book.id(),
                book.title(),
                book.author(),
                book.isbn(),
                book.status(),
                book.genres()
        );
    }
}