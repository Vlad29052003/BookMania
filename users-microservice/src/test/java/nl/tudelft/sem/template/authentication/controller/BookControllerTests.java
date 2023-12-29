package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.BookController;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.filters.FilterClient;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class BookControllerTests {
    private transient BookService bookService;
    private transient FilterClient filterClient;
    private transient BookController bookController;
    private transient Book book;
    private transient UUID bookId;
    private transient CreateBookRequestModel createBookRequest;
    private transient Book updatedBook;
    private transient String token;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.bookService = mock(BookService.class);
        this.filterClient = mock(FilterClient.class);
        this.bookController = new BookController(bookService, filterClient);
        this.book = new Book("title", List.of("author"), List.of(Genre.CRIME), "description", 155);
        this.bookId = UUID.randomUUID();
        createBookRequest = new CreateBookRequestModel();
        createBookRequest.setTitle("title");
        createBookRequest.setAuthors(List.of("Author"));
        createBookRequest.setGenres(List.of(Genre.ROMANCE));
        createBookRequest.setDescription("des");
        createBookRequest.setNumPages(255);
        updatedBook = new Book();
        this.token = "Bearer token";
    }

    @Test
    public void testGet() {
        when(bookService.getBook(bookId.toString())).thenReturn(book);
        assertThat(bookController.getBook(bookId.toString()))
                .isEqualTo(ResponseEntity.ok(book));

        when(bookService.getBook(bookId.toString()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"));
        assertThat(bookController.getBook(bookId.toString()).getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testCreate() {
        assertThat(bookController.addBook(createBookRequest, token))
                .isEqualTo(ResponseEntity.ok().build());

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(filterClient)
                .handle(any(), any());

        assertThat(bookController.addBook(createBookRequest, token).getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testUpdate() {
        assertThat(bookController.updateBook(updatedBook, token))
                .isEqualTo(ResponseEntity.ok().build());

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(filterClient)
                .handle(any(), any());
        assertThat(bookController.updateBook(updatedBook, token).getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testDelete() {
        assertThat(bookController.deleteBook(bookId.toString(), token))
                .isEqualTo(ResponseEntity.ok().build());

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(filterClient)
                .handle(any(), any());
        assertThat(bookController.deleteBook(bookId.toString(), token).getStatusCodeValue()).isEqualTo(404);
    }
}
