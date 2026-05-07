package com.antogian.shelvie.books;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookRecord> getAllBooks(BookStatus status) {
        List<Book> books = (status != null)
                ? bookRepository.findByStatus(status)
                : bookRepository.findAll();

        return books.stream().map(BookRecord::from).toList();
    }

    public Optional<BookRecord> getBook(UUID id) {
        return bookRepository.findById(id).map(BookRecord::from);
    }

    @Transactional
    public BookRecord createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setStatus(request.status());
        book.setGenres(request.genres());

        return BookRecord.from(bookRepository.save(book));
    }

    @Transactional
    public Optional<BookRecord> updateBook(UUID id, BookUpdateRequest request) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(request.title());
                    book.setAuthor(request.author());
                    if (request.isbn() != null) book.setIsbn(request.isbn());
                    if (request.status() != null) book.setStatus(request.status());
                    if (request.genres() != null) book.setGenres(request.genres());
                    return BookRecord.from(bookRepository.save(book));
                });
    }

    @Transactional
    public boolean deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) return false;
        bookRepository.deleteById(id);
        return true;
    }
}