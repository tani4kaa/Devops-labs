package edu.mamontova.lab7.repository;

import edu.mamontova.lab7.model.Book;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/*
  @author tanus
  @project lab7
  @class BookRepositoryTest
  @version 1.0.0
  @since 02.05.2025 - 13.50
*/
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
class BookRepositoryTest {

    @Autowired
    BookRepository underTest;

    @BeforeEach
    void setUp() {
        Book book1 = new Book("Book 1", "0011", "ISBN 1");
        Book book2 = new Book("Book 2", "0012", "ISBN 2");
        Book book3 = new Book("Book 3", "0013", "ISBN 3");

    }

    @AfterEach
    void tearDown() {
        List<Book> booksToDelete = underTest.findAll().stream()
                .filter(book -> book.getDescription().contains("###test"))
                .toList();
        underTest.deleteAll(booksToDelete);
    }

    @Test
    void shouldAssignIdWhenSavingNewBook() {
        Book book = new Book("Test Book", "CODE123", "###test");
        Book saved = underTest.save(book);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertFalse(saved.getId().isEmpty());
    }

    @Test
    void shouldDeleteBookById() {
        Book book = underTest.save(new Book("ToDelete", "DEL123", "###test"));
        underTest.deleteById(book.getId());

        assertFalse(underTest.findById(book.getId()).isPresent());
    }

    @Test
    void shouldFindBookById() {
        Book book = underTest.save(new Book("Findable", "FND001", "###test"));
        Book fromDb = underTest.findById(book.getId()).orElse(null);

        assertNotNull(fromDb);
        assertEquals(book.getId(), fromDb.getId());
    }

    @Test
    void shouldFindBooksByCodePrefix() {
        underTest.save(new Book("PrefixMatch1", "PX001", "###test"));
        underTest.save(new Book("PrefixMatch2", "PX002", "###test"));
        underTest.save(new Book("OtherBook", "ZZ999", "###test"));

        List<Book> matchingBooks = underTest.findAll().stream()
                .filter(book -> book.getCode().startsWith("PX"))
                .collect(Collectors.toList());

        assertEquals(2, matchingBooks.size());
        assertTrue(matchingBooks.stream().allMatch(b -> b.getCode().startsWith("PX")));
    }


    @Test
    void shouldUpdateBook() {
        Book book = underTest.save(new Book("Before Edit", "EDIT1", "###test"));
        book.setName("After Edit");
        Book updated = underTest.save(book);

        assertEquals("After Edit", updated.getName());
    }

    @Test
    void shouldFindAllBooksWithDescriptionContainingTest() {
        underTest.save(new Book("1", "C1", "###test"));
        underTest.save(new Book("2", "C2", "###test"));

        List<Book> result = underTest.findAll().stream()
                .filter(b -> b.getDescription().contains("###test"))
                .toList();

        assertTrue(result.size() >= 2);
    }

    @Test
    void shouldReturnNullIfBookNotFound() {
        Book result = underTest.findById("non_existing_id").orElse(null);
        assertNull(result);
    }

    @Test
    void shouldCreateMultipleBooks() {
        List<Book> books = List.of(
                new Book("Bulk1", "B1", "###test"),
                new Book("Bulk2", "B2", "###test")
        );

        List<Book> saved = underTest.saveAll(books);
        assertEquals(2, saved.size());
    }

    @Test
    void shouldNotAssignSameIdToDifferentBooks() {
        Book b1 = underTest.save(new Book("Unique1", "U1", "###test"));
        Book b2 = underTest.save(new Book("Unique2", "U2", "###test"));

        assertNotEquals(b1.getId(), b2.getId());
    }

    @Test
    void shouldReturnSameBookAfterSave() {
        Book book = new Book("RoundTrip", "RT1", "###test");
        Book saved = underTest.save(book);

        assertEquals(book.getName(), saved.getName());
        assertEquals(book.getCode(), saved.getCode());
        assertEquals(book.getDescription(), saved.getDescription());
    }


}