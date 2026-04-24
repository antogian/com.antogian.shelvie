package com.antogian.shelvie.books;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookStatus {

    READ("Read"),
    TO_READ("To-Read"),
    CURRENTLY_READING("Currently-Reading");

    private final String value;

    BookStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BookStatus fromString(String input) {
        if (input == null) throw new IllegalArgumentException("Status must not be null");

        for (BookStatus status : values()) {
            if (status.value.equalsIgnoreCase(input)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: '" + input + "'. Accepted values: Read, To-Read, Currently-Reading");
    }
}