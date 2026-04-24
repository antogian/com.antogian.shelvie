package com.antogian.shelvie.books;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record BookUpdateRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,          // null = clear it
        BookStatus status,    // null = keep existing
        Set<String> genres    // null = keep existing, but if provided must not be empty
) { }