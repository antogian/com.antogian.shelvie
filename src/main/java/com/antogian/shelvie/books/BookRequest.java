package com.antogian.shelvie.books;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        boolean read
) { }