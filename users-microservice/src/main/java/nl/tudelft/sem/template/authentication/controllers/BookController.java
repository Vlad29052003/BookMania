package nl.tudelft.sem.template.authentication.controllers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/books")
public class BookController {
    private final transient BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Gets a book from the overall collection.
     *
     * @param bookId      the id of the book to get
     * @param bearerToken the jwt token
     * @return the status of the operation
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBook(@PathVariable String bookId,
                                     @RequestHeader(name = AUTHORIZATION) String bearerToken) {
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
     * @param bearerToken            the jwt token
     * @return the status of the operation
     */
    @PostMapping("")
    public ResponseEntity<?> addBook(@RequestBody CreateBookRequestModel createBookRequestModel,
                                     @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            bookService.addBook(createBookRequestModel, bearerToken);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Updates a book in the system.
     *
     * @param updatedBook contains the new information for the book
     * @param bearerToken            is the jwt token of the user that made the request
     * @return the status of the operation
     */
    @PutMapping("")
    public ResponseEntity<?> updateBook(@RequestBody Book updatedBook,
                                        @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            bookService.updateBook(updatedBook, bearerToken);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a book from the overall collection.
     *
     * @param bookId      the id of the book to be deleted
     * @param bearerToken the jwt token
     * @return the status of the operation
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable String bookId,
                                        @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            bookService.deleteBook(bookId, bearerToken);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        return ResponseEntity.ok().build();
    }

}
