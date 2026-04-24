package com.antogian.shelvie.books;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Set;

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
        book.setStatus(BookStatus.TO_READ);
        book.setGenres(Set.of("Programming", "Software Engineering"));

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(bookRepository.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(b -> {
                    assertThat(b.getTitle()).isEqualTo("Clean Code");
                    assertThat(b.getAuthor()).isEqualTo("Robert C. Martin");
                    assertThat(b.getIsbn()).isEqualTo("9780132350884");
                    assertThat(b.getStatus()).isEqualTo(BookStatus.TO_READ);
                    assertThat(b.getGenres()).containsExactlyInAnyOrder("Programming", "Software Engineering");
                });
    }

    @Test
    void defaultStatusIsToRead() {
        Book book = new Book();
        book.setTitle("The Pragmatic Programmer");
        book.setAuthor("David Thomas");
        book.setGenres(Set.of("Programming"));

        Book saved = bookRepository.save(book);

        assertThat(saved.getStatus()).isEqualTo(BookStatus.TO_READ);
    }

    @Test
    void findsByStatus() {
        Book read = new Book();
        read.setTitle("Clean Code");
        read.setAuthor("Robert C. Martin");
        read.setStatus(BookStatus.READ);
        read.setGenres(Set.of("Programming"));

        Book toRead = new Book();
        toRead.setTitle("The Pragmatic Programmer");
        toRead.setAuthor("David Thomas");
        toRead.setStatus(BookStatus.TO_READ);
        toRead.setGenres(Set.of("Programming"));

        bookRepository.save(read);
        bookRepository.save(toRead);

        assertThat(bookRepository.findByStatus(BookStatus.READ)).hasSize(1);
        assertThat(bookRepository.findByStatus(BookStatus.TO_READ)).hasSize(1);
        assertThat(bookRepository.findByStatus(BookStatus.CURRENTLY_READING)).isEmpty();
    }
}