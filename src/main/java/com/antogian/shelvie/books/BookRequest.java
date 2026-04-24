package com.antogian.shelvie.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        @NotNull BookStatus status,
        @NotEmpty Set<String> genres
) { }