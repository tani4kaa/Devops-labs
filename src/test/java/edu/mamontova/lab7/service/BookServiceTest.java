package edu.mamontova.lab7.service;

import edu.mamontova.lab7.model.Book;
import edu.mamontova.lab7.repository.BookRepository;
import edu.mamontova.lab7.request.BookCreateRequest;
import edu.mamontova.lab7.request.BookUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {


    @Autowired
    private BookRepository repository;

    @Autowired
    private BookService underTest;


    @BeforeEach
    void setUp() {
        repository.deleteAll();
        underTest.create(new BookCreateRequest("name1", "00001", "desc1"));
        underTest.create(new BookCreateRequest("name2", "00002", "desc2"));
        underTest.create(new BookCreateRequest("name3", "00003", "desc3"));
    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }


    @Test
    void whenCreateBook_thenFieldsAreCorrectAndTimestampsPresent() {
        // given
        BookCreateRequest request = new BookCreateRequest("Book test", "CODE001", "Some description");
        LocalDateTime now = LocalDateTime.now();

        // when
        Book book = underTest.create(request);

        // then
        assertNotNull(book);
        assertNotNull(book.getId());
        assertEquals("Book test", book.getName());
        assertEquals("CODE001", book.getCode());
        assertEquals("Some description", book.getDescription());
        assertNotNull(book.getCreateDate());
        assertFalse(book.getCreateDate().isBefore(now));
        assertNotNull(book.getUpdateDate());
        assertTrue(book.getUpdateDate().isEmpty());
    }

    @Test
    void whenGetAll_thenReturnAllBooks() {
        List<Book> all = underTest.getAll();
        assertEquals(3, all.size());
    }

    @Test
    void whenGetById_thenReturnCorrectBook() {
        Book created = underTest.create(new BookCreateRequest("Test", "ID123", "desc"));
        Book found = underTest.getById(created.getId());
        assertNotNull(found);
        assertEquals("ID123", found.getCode());
    }

    @Test
    void whenGetByIdNotExist_thenReturnNull() {
        Book found = underTest.getById("nonexistent-id");
        assertNull(found);
    }

    @Test
    void whenDeleteById_thenBookIsRemoved() {
        Book created = underTest.create(new BookCreateRequest("Delete", "DEL01", "delete me"));
        underTest.delById(created.getId());
        assertFalse(repository.findById(created.getId()).isPresent());
    }

    @Test
    void whenUpdate_thenNewFieldsAndTimestampAppended() {
        Book created = underTest.create(new BookCreateRequest("Upd", "UPD01", "desc"));
        BookUpdateRequest updateRequest = new BookUpdateRequest(
                created.getId(), "Updated", "UPD02", "updated desc"
        );
        Book updated = underTest.update(updateRequest);

        assertNotNull(updated);
        assertEquals("Updated", updated.getName());
        assertEquals("UPD02", updated.getCode());
        assertEquals("updated desc", updated.getDescription());
        assertEquals(1, updated.getUpdateDate().size());
    }

    @Test
    void whenUpdateNonexistent_thenReturnNull() {
        BookUpdateRequest updateRequest = new BookUpdateRequest("nonexistent", "N", "C", "D");
        Book updated = underTest.update(updateRequest);
        assertNull(updated);
    }

    @Test
    void whenCreateWithSameCodeDifferentNames_thenAllSaved() {
        underTest.create(new BookCreateRequest("First", "DUPLICATE", "desc"));
        underTest.create(new BookCreateRequest("Second", "DUPLICATE", "desc"));
        List<Book> result = repository.findAll().stream()
                .filter(b -> b.getCode().equals("DUPLICATE")).toList();
        assertEquals(2, result.size());
    }

    @Test
    void whenCreateMultipleBooks_thenIdsAreUnique() {
        Book b1 = underTest.create(new BookCreateRequest("A", "C1", "D"));
        Book b2 = underTest.create(new BookCreateRequest("B", "C2", "D"));
        assertNotEquals(b1.getId(), b2.getId());
    }

    @Test
    void updateAppendsToUpdateDateList() {
        Book book = underTest.create(new BookCreateRequest("B", "C", "D"));
        BookUpdateRequest req = new BookUpdateRequest(book.getId(), "B2", "C2", "D2");
        underTest.update(req);
        underTest.update(req);
        Book result = repository.findById(book.getId()).orElseThrow();
        assertEquals(2, result.getUpdateDate().size());
    }

    @Test
    void whenUpdateWithoutChangingFields_thenStillAppendsTimestamp() {
        Book book = underTest.create(new BookCreateRequest("Same", "Same", "Same"));
        BookUpdateRequest req = new BookUpdateRequest(book.getId(), "Same", "Same", "Same");
        Book updated = underTest.update(req);
        assertEquals(1, updated.getUpdateDate().size());
    }

    @Test
    void deleteByIdShouldNotThrowOnNonexistentId() {
        assertDoesNotThrow(() -> underTest.delById("nonexistent"));
    }

    @Test
    void createBookSetsCorrectTypes() {
        Book book = underTest.create(new BookCreateRequest("T", "T", "T"));
        assertSame(LocalDateTime.class, book.getCreateDate().getClass());
        assertSame(ArrayList.class, book.getUpdateDate().getClass());
    }

    @Test
    void whenGetAllAfterCreation_thenIncludesNewBook() {
        int before = underTest.getAll().size();
        underTest.create(new BookCreateRequest("New", "N1", "ND"));
        int after = underTest.getAll().size();
        assertEquals(before + 1, after);
    }

    @Test
    void createShouldSaveToRepository() {
        Book book = underTest.create(new BookCreateRequest("Repo", "R", "RepoDesc"));
        Optional<Book> fromRepo = repository.findById(book.getId());
        assertTrue(fromRepo.isPresent());
    }

    @Test
    void whenCreateWithNullFields_thenSaveFailsOrDefaults() {
        BookCreateRequest request = new BookCreateRequest(null, null, null);
        Book created = underTest.create(request);
        assertNotNull(created);
        assertNull(created.getName());
    }

    @Test
    void createWithEmptyDescriptionShouldSucceed() {
        BookCreateRequest request = new BookCreateRequest("Book", "CODE", "");
        Book book = underTest.create(request);
        assertEquals("", book.getDescription());
    }

    @Test
    void whenCreateThenIdIsGenerated() {
        Book book = underTest.create(new BookCreateRequest("CheckId", "CID", "check"));
        assertNotNull(book.getId());
    }

}