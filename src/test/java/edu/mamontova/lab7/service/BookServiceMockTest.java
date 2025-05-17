package edu.mamontova.lab7.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import edu.mamontova.lab7.model.Book;
import edu.mamontova.lab7.repository.BookRepository;
import edu.mamontova.lab7.request.BookCreateRequest;
import edu.mamontova.lab7.request.BookUpdateRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;
/*
  @author tanus
  @project lab9
  @class BookServiceMockTest
  @version 1.0.0
  @since 17.05.2025 - 22.16
*/


class BookServiceCustomTest {
    @Mock
    private BookRepository mockRepository;

    private BookService bookService;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    private BookCreateRequest createReq;
    private BookUpdateRequest updateReq;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        bookService = new BookService(mockRepository);
    }

    @Test
    @DisplayName("Add new book if code is unique")
    void shouldSaveBookWhenCodeIsNew() {
        createReq = new BookCreateRequest("Moby Dick", "MD123", "A sea story");
        given(mockRepository.existsByCode("MD123")).willReturn(false);

        bookService.create(createReq);

        then(mockRepository).should().save(bookCaptor.capture());
        Book saved = bookCaptor.getValue();

        assertEquals("Moby Dick", saved.getName());
        assertEquals("MD123", saved.getCode());
        assertNotNull(saved.getCreateDate());
        assertTrue(saved.getUpdateDate().isEmpty());
    }

    @Test
    @DisplayName("Do not save book with duplicate code")
    void shouldReturnNullWhenCodeExists() {
        createReq = new BookCreateRequest("Moby Dick", "MD123", "A sea story");
        given(mockRepository.existsByCode("MD123")).willReturn(true);

        Book result = bookService.create(createReq);

        assertNull(result);
        verify(mockRepository, never()).save(any());
    }

    @Test
    void codeCheckReturnsFalse() {
        given(mockRepository.existsByCode("XYZ")).willReturn(false);
        assertFalse(mockRepository.existsByCode("XYZ"));
    }

    @Test
    void nullCreateRequestThrowsException() {
        assertThrows(NullPointerException.class, () -> bookService.create((BookCreateRequest) null));
    }

    @Test
    void findByIdReturnsBook() {
        Book stored = new Book("10", "Island", "ISL01", "Tropical story");
        given(mockRepository.findById("10")).willReturn(Optional.of(stored));

        Book result = bookService.getById("10");

        assertNotNull(result);
        assertEquals("Island", result.getName());
    }

    @Test
    void findByIdReturnsNull() {
        given(mockRepository.findById("404")).willReturn(Optional.empty());

        Book result = bookService.getById("404");

        assertNull(result);
    }

    @Test
    void deleteBookById() {
        bookService.delById("DEL1");
        verify(mockRepository).deleteById("DEL1");
    }

    @Test
    void updateDirectly() {
        Book toUpdate = new Book("9", "Draft", "DRF9", "Initial");
        bookService.update(toUpdate);
        verify(mockRepository).save(toUpdate);
    }

    @Test
    void updateFromRequestSuccessfully() {
        Book existing = new Book("2", "Start", "S002", "Initial");
        existing.setCreateDate(LocalDateTime.now().minusDays(1));
        existing.setUpdateDate(new ArrayList<>());

        updateReq = new BookUpdateRequest("2", "Final", "S002", "Modified");

        given(mockRepository.findById("2")).willReturn(Optional.of(existing));
        given(mockRepository.save(any(Book.class))).willAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.update(updateReq);

        assertNotNull(result);
        assertEquals("Final", result.getName());
        assertEquals("Modified", result.getDescription());
        assertEquals(existing.getCreateDate(), result.getCreateDate());
        assertEquals(1, result.getUpdateDate().size());
    }

    @Test
    void updateNonExistingBookReturnsNull() {
        updateReq = new BookUpdateRequest("999", "Any", "ANY", "Any");
        given(mockRepository.findById("999")).willReturn(Optional.empty());

        Book result = bookService.update(updateReq);

        assertNull(result);
    }

    @Test
    void createBookWithoutDescription() {
        createReq = new BookCreateRequest("Nameless", "NN100", null);
        given(mockRepository.existsByCode("NN100")).willReturn(false);

        Book saved = new Book("Nameless", "NN100", null);
        saved.setCreateDate(LocalDateTime.now());
        saved.setUpdateDate(new ArrayList<>());
        given(mockRepository.save(any(Book.class))).willReturn(saved);

        Book result = bookService.create(createReq);

        assertEquals("Nameless", result.getName());
        assertNull(result.getDescription());
        verify(mockRepository).save(any());
    }

    @Test
    void createSetsCreationTimestamp() {
        createReq = new BookCreateRequest("TimeBook", "TB01", "desc");
        given(mockRepository.existsByCode("TB01")).willReturn(false);

        bookService.create(createReq);

        verify(mockRepository).save(bookCaptor.capture());
        Book saved = bookCaptor.getValue();

        assertNotNull(saved.getCreateDate());
        assertTrue(saved.getCreateDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void noExceptionOnValidCreate() {
        createReq = new BookCreateRequest("OK", "OK001", "ok");
        given(mockRepository.existsByCode("OK001")).willReturn(false);

        assertDoesNotThrow(() -> bookService.create(createReq));
    }

    @Test
    void noExceptionOnUpdateNotFound() {
        updateReq = new BookUpdateRequest("999", "N", "C", "D");
        given(mockRepository.findById("999")).willReturn(Optional.empty());

        assertDoesNotThrow(() -> bookService.update(updateReq));
    }

    @Test
    void saveCalledOnceOnCreate() {
        createReq = new BookCreateRequest("Once", "ONCE1", "desc");
        given(mockRepository.existsByCode("ONCE1")).willReturn(false);

        bookService.create(createReq);

        verify(mockRepository, times(1)).save(any());
    }

    @Test
    void saveNeverCalledIfCodeExists() {
        createReq = new BookCreateRequest("Dup", "DUP1", "desc");
        given(mockRepository.existsByCode("DUP1")).willReturn(true);

        bookService.create(createReq);

        verify(mockRepository, never()).save(any());
    }

    @Test
    void nullCreateThrows() {
        assertThrows(NullPointerException.class, () -> bookService.create((BookCreateRequest) null));
    }

    @Test
    void nullUpdateThrows() {
        assertThrows(NullPointerException.class, () -> bookService.update((BookUpdateRequest) null));
    }

    @Test
    void deleteByValidIdNoException() {
        assertDoesNotThrow(() -> bookService.delById("DEL123"));
        verify(mockRepository).deleteById("DEL123");
    }

    @Test
    @DisplayName("Empty book list case")
    void emptyListReturnedWhenNoBooks() {
        given(mockRepository.findAll()).willReturn(Collections.emptyList());

        List<Book> result = bookService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("New book has empty update history")
    void createBookHasEmptyUpdateList() {
        createReq = new BookCreateRequest("Fresh", "F001", "text");
        given(mockRepository.existsByCode("F001")).willReturn(false);

        bookService.create(createReq);

        then(mockRepository).should().save(bookCaptor.capture());
        assertThat(bookCaptor.getValue().getUpdateDate()).isEmpty();
    }

    @Test
    @DisplayName("Update adds date to update list")
    void updateAddsNewTimestamp() {
        Book original = new Book("3", "Old", "C3", "D3");
        original.setCreateDate(LocalDateTime.now());
        original.setUpdateDate(new ArrayList<>());

        updateReq = new BookUpdateRequest("3", "New", "C3", "D4");

        given(mockRepository.findById("3")).willReturn(Optional.of(original));

        bookService.update(updateReq);

        then(mockRepository).should().save(bookCaptor.capture());
        assertEquals(1, bookCaptor.getValue().getUpdateDate().size());
    }

    @AfterEach
    void cleanup() {
    }
}