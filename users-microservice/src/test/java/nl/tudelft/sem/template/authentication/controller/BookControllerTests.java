package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.BookController;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.handlers.CommandChain;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class BookControllerTests {
    private transient BookService bookService;
    private transient CommandChain commandChain;
    private transient BookController bookController;
    private transient Book book;
    private transient UUID bookId;
    private transient CreateBookRequestModel createBookRequest;
    private transient Book updatedBook;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.bookService = mock(BookService.class);
        this.commandChain = mock(CommandChain.class);
        this.bookController = new BookController(bookService, commandChain);
        this.book = new Book("title", List.of("author"), List.of(Genre.CRIME), "description", 155);
        this.bookId = UUID.randomUUID();
        createBookRequest = new CreateBookRequestModel();
        createBookRequest.setTitle("title");
        createBookRequest.setAuthors(List.of("Author"));
        createBookRequest.setGenres(List.of(Genre.ROMANCE));
        createBookRequest.setDescription("des");
        createBookRequest.setNumPages(255);
        updatedBook = new Book();

        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);
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
        assertThat(bookController.addBook(createBookRequest))
                .isEqualTo(ResponseEntity.ok().build());
        verify(commandChain, times(1)).setAddBookStrategy();

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(commandChain)
                .handle(any(), any(), any());

        assertThat(bookController.addBook(createBookRequest).getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testUpdate() {
        assertThat(bookController.updateBook(updatedBook))
                .isEqualTo(ResponseEntity.ok().build());
        verify(commandChain, times(1)).setEditBookStrategy();

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(commandChain)
                .handle(any(), any(), any());
        assertThat(bookController.updateBook(updatedBook).getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testDelete() {
        ArgumentCaptor<Book> argumentCaptor = ArgumentCaptor.forClass(Book.class);

        assertThat(bookController.deleteBook(bookId.toString()))
                .isEqualTo(ResponseEntity.ok().build());

        verify(commandChain, times(1)).setDeleteBookStrategy();

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!"))
                .when(commandChain)
                .handle(any(), any(), argumentCaptor.capture());
        assertThat(bookController.deleteBook(bookId.toString()).getStatusCodeValue()).isEqualTo(404);
        assertThat(argumentCaptor.getValue().getId()).isEqualTo(bookId);
    }
}
