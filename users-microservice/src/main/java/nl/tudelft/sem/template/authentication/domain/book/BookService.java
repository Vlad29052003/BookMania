package nl.tudelft.sem.template.authentication.domain.book;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {
    private final transient BookRepository bookRepository;
    private final transient UserRepository userRepository;
    private final transient JwtService jwtService;

    /**
     * Creates a BookService service.
     *
     * @param bookRepository is the book repository
     * @param userRepository is the user repository
     * @param jwtService     is the jwt service
     */
    @Autowired
    public BookService(BookRepository bookRepository, UserRepository userRepository, JwtService jwtService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Gets a book from the overall collection.
     *
     * @param bookId the id of the book to get
     * @return the book, if found
     */
    public Book getBook(String bookId) {
        var optBook = bookRepository.findById(UUID.fromString(bookId));
        if (optBook.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!");
        }
        return optBook.get();
    }

    /**
     * Adds a book to the database.
     *
     * @param newBook is the book to be added.
     */
    public void addBook(Book newBook) {
        List<Book> books = bookRepository.findByTitle(newBook.getTitle());
        boolean invalid = books.stream().anyMatch(x -> new HashSet<>(x.getAuthors())
                .equals(new HashSet<>(newBook.getAuthors())));
        if (invalid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The book is already in the system!");
        }

        bookRepository.saveAndFlush(newBook);
    }


    /**
     * Updates a book in the system.
     *
     * @param updatedBook contains the new information for the book
     */
    public void updateBook(Book updatedBook) {
        if (updatedBook.getAuthors() == null || updatedBook.getGenres() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The authors and genres cannot be null!");
        }
        Optional<Book> optBook = bookRepository.findById(updatedBook.getId());

        if (optBook.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book does not exist!");
        }

        Book currentBook = optBook.get();

        currentBook.setTitle(updatedBook.getTitle());
        currentBook.setAuthors(new ArrayList<>(updatedBook.getAuthors()));
        currentBook.setGenres(new ArrayList<>(updatedBook.getGenres()));
        currentBook.setDescription(updatedBook.getDescription());
        currentBook.setNumPages(updatedBook.getNumPages());

        bookRepository.saveAndFlush(currentBook);
    }

    /**
     * Deletes a book from the overall collection.
     *
     * @param bookId      is the id of the book to be deleted.
     * @param bearerToken is the jwt token of the user that made the request
     */
    @Transactional
    public void deleteBook(String bookId, String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins may delete books from the system!");
        }
        var optBook = bookRepository.findById(UUID.fromString(bookId));
        if (optBook.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This book does not exist!");
        }

        userRepository.removeBookFromUsersFavorites(UUID.fromString(bookId));

        bookRepository.deleteById(UUID.fromString(bookId));
    }

    private Authority getAuthority(String bearerToken) {
        return jwtService.extractAuthorization(bearerToken.substring(7));
    }
}
