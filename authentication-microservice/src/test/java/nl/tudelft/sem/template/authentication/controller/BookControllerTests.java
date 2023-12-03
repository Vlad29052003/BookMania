package nl.tudelft.sem.template.authentication.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import nl.tudelft.sem.template.authentication.models.UpdateBookRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class BookControllerTests {
    private transient BookService bookService;
    private transient BookController bookController;
    private transient Book book;
    private transient UUID bookId;
    private transient CreateBookRequestModel createBookRequest;
    private transient UpdateBookRequestModel updateBookRequestModel;
    private transient String token;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.bookService = mock(BookService.class);
        this.bookController = new BookController(bookService);
        this.book = new Book("title", List.of("author"), List.of(Genre.CRIME), "description", 155);
        this.bookId = UUID.randomUUID();
        createBookRequest = new CreateBookRequestModel();
        updateBookRequestModel = new UpdateBookRequestModel();
        this.token = "token";
    }

    @Test
    public void testGet() {
        when(bookService.getBook(bookId.toString())).thenReturn(book);
        assertEquals(bookController.getBook(bookId.toString(), token), ResponseEntity.ok(book));

        when(bookService.getBook(bookId.toString())).thenThrow(new IllegalArgumentException());
        assertEquals(bookController.getBook(bookId.toString(), token).getStatusCodeValue(), 404);
    }

    @Test
    public void testGetBadRequest() {
        when(bookService.getBook(bookId.toString())).thenThrow(new RuntimeException());
        assertEquals(bookController.getBook(bookId.toString(), token).getStatusCodeValue(), 400);
    }

    @Test
    public void testCreate() {
        assertEquals(bookController.addBook(createBookRequest, token).getStatusCodeValue(), 200);

        doThrow(new IllegalArgumentException())
                .when(bookService)
                .addBook(any(), any());
        assertEquals(bookController.addBook(createBookRequest, token).getStatusCodeValue(), 409);

        doThrow(new IllegalCallerException())
                .when(bookService)
                .addBook(any(), any());
        assertEquals(bookController.addBook(createBookRequest, token).getStatusCodeValue(), 401);

        doThrow(new RuntimeException())
                .when(bookService)
                .addBook(any(), any());
        assertEquals(bookController.addBook(createBookRequest, token).getStatusCodeValue(), 400);
    }

    @Test
    public void testUpdate() {
        assertEquals(bookController.updateBook(updateBookRequestModel, token).getStatusCodeValue(), 200);

        doThrow(new IllegalArgumentException())
                .when(bookService)
                .updateBook(any(), any());
        assertEquals(bookController.updateBook(updateBookRequestModel, token).getStatusCodeValue(), 404);

        doThrow(new IllegalCallerException())
                .when(bookService)
                .updateBook(any(), any());
        assertEquals(bookController.updateBook(updateBookRequestModel, token).getStatusCodeValue(), 401);

        doThrow(new RuntimeException())
                .when(bookService)
                .updateBook(any(), any());
        assertEquals(bookController.updateBook(updateBookRequestModel, token).getStatusCodeValue(), 400);
    }

    @Test
    public void testDelete() {
        assertEquals(bookController.deleteBook(bookId.toString(), token).getStatusCodeValue(), 200);

        doThrow(new IllegalArgumentException())
                .when(bookService)
                .deleteBook(any(), any());
        assertEquals(bookController.deleteBook(bookId.toString(), token).getStatusCodeValue(), 404);

        doThrow(new IllegalCallerException())
                .when(bookService)
                .deleteBook(any(), any());
        assertEquals(bookController.deleteBook(bookId.toString(), token).getStatusCodeValue(), 401);

        doThrow(new RuntimeException())
                .when(bookService)
                .deleteBook(any(), any());
        assertEquals(bookController.deleteBook(bookId.toString(), token).getStatusCodeValue(), 400);
    }
}
