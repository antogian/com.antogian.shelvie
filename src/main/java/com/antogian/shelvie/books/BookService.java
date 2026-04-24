package com.antogian.shelvie.books;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks(BookStatus status) {
        List<Book> books = (status != null)
                ? bookRepository.findByStatus(status)
                : bookRepository.findAll();

        return books.stream().map(BookResponse::from).toList();
    }

    public Optional<BookResponse> getBook(UUID id) {
        return bookRepository.findById(id).map(BookResponse::from);
    }

    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setStatus(request.status());
        book.setGenres(request.genres());

        return BookResponse.from(bookRepository.save(book));
    }

    public Optional<BookResponse> updateBook(UUID id, BookUpdateRequest request) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(request.title());
                    book.setAuthor(request.author());
                    if (request.isbn() != null) book.setIsbn(request.isbn());
                    if (request.status() != null) book.setStatus(request.status());
                    if (request.genres() != null) book.setGenres(request.genres());
                    return BookResponse.from(bookRepository.save(book));
                });
    }

    public boolean deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) return false;
        bookRepository.deleteById(id);
        return true;
    }
}