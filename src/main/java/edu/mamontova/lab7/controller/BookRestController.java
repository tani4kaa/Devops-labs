package edu.mamontova.lab7.controller;/*
  @author tanus
  @project lab7
  @class BookRestController
  @version 1.0.0
  @since 02.05.2025 - 13.44
*/
import edu.mamontova.lab7.model.Book;
import edu.mamontova.lab7.request.BookCreateRequest;
import edu.mamontova.lab7.request.BookUpdateRequest;
import edu.mamontova.lab7.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/books/")
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    @GetMapping
    public List<Book> showAll() {
        return bookService.getAll();
    }

    @GetMapping("{id}")
    public Book showOneById(@PathVariable String id) {
        return bookService.getById(id);
    }

    @PostMapping
    public Book insert(@RequestBody Book book) {
        return bookService.create(book);
    }
    //============== request =====================
    @PostMapping("/dto")
    public Book insert(@RequestBody BookCreateRequest request) {
        return bookService.create(request);
    }
    @PutMapping
    public Book edit(@RequestBody Book book) {
        return bookService.update(book);
    }
    //============== request =====================
    @PutMapping("/dto")
    public Book edit(@RequestBody BookUpdateRequest request) {
        return bookService.update(request);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        bookService.delById(id);
    }
}
//TRY PULL REQUEST
//TRY PULL
//TRY PUL