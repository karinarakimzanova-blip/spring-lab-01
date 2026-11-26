package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import kz.iitu.springlab.aspect.CallCounterAspect;
import kz.iitu.springlab.service.CatalogService;
import org.springframework.aop.support.AopUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {

    private final CatalogService catalogService;
    private final CallCounterAspect callCounterAspect;

    public CatalogController(CatalogService catalogService, CallCounterAspect callCounterAspect) {
        this.catalogService = catalogService;
        this.callCounterAspect = callCounterAspect;
    }

    public record ItemRequest(@NotBlank String title) { }

    public record ItemResponse(long id, String title) { }

    @GetMapping("/item/{id}")
    public ItemResponse getItem(@PathVariable long id) {
        String title = catalogService.findById(id);
        if (title == null) {
            throw new NoSuchElementException("Item not found: " + id);
        }
        return new ItemResponse(id, title);
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ItemResponse(1L, request.title()));
    }

    @GetMapping("/items")
    public List<String> getItems(@RequestParam(defaultValue = "5") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String deleteItem(@PathVariable long id) {
        return catalogService.remove(id);
    }

    @GetMapping("/proxy")
    public Map<String, String> proxyInfo() {
        return Map.of(
                "className", catalogService.getClass().getName(),
                "superClass", catalogService.getClass().getSuperclass().getSimpleName(),
                "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalogService)),
                "isCglib", String.valueOf(AopUtils.isCglibProxy(catalogService))
        );
    }

    @GetMapping("/remove-twice/{id}")
    public String removeTwice(@PathVariable long id) {
        return catalogService.removeTwice(id);
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return callCounterAspect.getCallCounts();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }
}