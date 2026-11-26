package kz.iitu.springlab.catalog;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        List<Book> all = repository.findAll();
        if (author == null || author.isBlank()) {
            return all;
        }
        String needle = author.toLowerCase();
        return all.stream()
                .filter(b -> b.author().toLowerCase().contains(needle))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Book create(Book book) {
        // id от клиента игнорируем, его выдаёт репозиторий
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Optional<Book> update(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(repository.save(new Book(id, book.title(), book.author(), book.year())));
    }
    @PreAuthorize("hasRole('ADMIN')")

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
