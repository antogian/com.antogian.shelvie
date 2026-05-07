package com.antogian.shelvie.books;

import java.util.Set;
import java.util.UUID;

public record BookRecord(
        UUID id,
        String title,
        String author,
        String isbn,
        BookStatus status,
        Set<String> genres
) {
    public static BookRecord from(Book book) {
        return new BookRecord(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getStatus(),
                book.getGenres()
        );
    }
}