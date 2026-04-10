package com.antogian.shelvie.books;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void savesAndRetrievesBook() {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setRead(false);

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(bookRepository.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(b -> {
                    assertThat(b.getTitle()).isEqualTo("Clean Code");
                    assertThat(b.getAuthor()).isEqualTo("Robert C. Martin");
                    assertThat(b.getIsbn()).isEqualTo("9780132350884");
                    assertThat(b.isRead()).isFalse();
                });
    }

    @Test
    void defaultReadValueIsFalse() {
        Book book = new Book();
        book.setTitle("The Pragmatic Programmer");
        book.setAuthor("David Thomas");

        Book saved = bookRepository.save(book);

        assertThat(saved.isRead()).isFalse();
    }
}