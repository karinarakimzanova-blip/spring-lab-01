package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> list(@RequestParam(required = false) String author,
                           @RequestParam(defaultValue = "10") int limit) {
        return service.findAll(author).stream().limit(limit).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> find(@PathVariable long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = service.create(book);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> replace(@PathVariable long id, @RequestBody Book book) {
        return service.update(id, book)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ===== ВАРИАНТ 11 =====
    @GetMapping("/sorted")
    public ResponseEntity<?> sorted(@RequestParam(defaultValue = "title") String sort) {
        Comparator<Book> comparator = switch (sort) {
            case "title"  -> Comparator.comparing(Book::title, String.CASE_INSENSITIVE_ORDER);
            case "author" -> Comparator.comparing(Book::author, String.CASE_INSENSITIVE_ORDER);
            case "year"   -> Comparator.comparingInt(Book::year);
            default       -> null;
        };
        if (comparator == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown sort value: " + sort,
                    "allowed", List.of("title", "author", "year")));
        }
        return ResponseEntity.ok(service.findAll(null).stream().sorted(comparator).toList());
    }
}
