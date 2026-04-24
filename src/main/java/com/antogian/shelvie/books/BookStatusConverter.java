package com.antogian.shelvie.books;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookStatusConverter implements Converter<String, BookStatus> {

    @Override
    public BookStatus convert(String source) {
        return BookStatus.fromString(source);
    }
}