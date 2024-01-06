package nl.tudelft.sem.template.authentication.controllers;

import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.handlers.CommandChain;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/books")
public class BookController {
    private final transient BookService bookService;
    private final transient CommandChain commandChain;

    @Autowired
    public BookController(BookService bookService, CommandChain commandChain) {
        this.bookService = bookService;
        this.commandChain = commandChain;
    }

    /**
     * Gets a book from the overall collection.
     *
     * @param bookId the id of the book to get
     * @return the status of the operation
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBook(@PathVariable String bookId) {
        try {
            return ResponseEntity.ok(bookService.getBook(bookId));
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Adds a book to the overall collection.
     *
     * @param createBookRequestModel the request information about the book
     * @return the status of the operation
     */
    @PostMapping("")
    public ResponseEntity<?> addBook(@RequestBody CreateBookRequestModel createBookRequestModel) {
        try {
            commandChain.setAddBookStrategy();
            Book newBook = new Book(createBookRequestModel);
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            Authority authority = (Authority) SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().iterator().next();
            commandChain.handle(username, authority, newBook);

        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Updates a book in the system.
     *
     * @param updatedBook contains the new information for the book
     * @return the status of the operation
     */
    @PutMapping("")
    public ResponseEntity<?> updateBook(@RequestBody Book updatedBook) {
        try {
            commandChain.setEditBookStrategy();
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            Authority authority = (Authority) SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().iterator().next();
            commandChain.handle(username, authority, updatedBook);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a book from the overall collection.
     *
     * @param bookId      the id of the book to be deleted
     * @return the status of the operation
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable String bookId) {
        try {
            commandChain.setDeleteBookStrategy();
            Book deletedBook = new Book();
            deletedBook.setId(UUID.fromString(bookId));
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            Authority authority = (Authority) SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().iterator().next();
            commandChain.handle(username, authority, deletedBook);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

}
