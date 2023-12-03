package nl.tudelft.sem.template.authentication.domain.book;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import nl.tudelft.sem.template.authentication.models.UpdateBookRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param bookId      the id of the book to get
     * @return the book, if found
     */
    public Book getBook(String bookId) {
        var optBook = bookRepository.findById(UUID.fromString(bookId));
        if (optBook.isEmpty()) {
            throw new IllegalArgumentException("The book does not exist!");
        }
        return optBook.get();
    }

    /**
     * Adds a book to the database.
     *
     * @param createBookRequestModel contains the book information
     * @param bearerToken            is the jwt token of the user who made the request
     */
    public void addBook(CreateBookRequestModel createBookRequestModel, String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN)) {
            throw new IllegalCallerException("Only admins may add books to the system!");
        }
        List<Book> books = bookRepository.findByTitle(createBookRequestModel.getTitle());
        boolean invalid = books.stream().anyMatch(x -> new HashSet<>(x.getAuthors())
                .containsAll(createBookRequestModel.getAuthors()));
        if (invalid) {
            throw new IllegalArgumentException("The book is already in the system!");
        }

        Book newBook = new Book(createBookRequestModel.getTitle(),
                createBookRequestModel.getAuthors(),
                createBookRequestModel.getGenres(),
                createBookRequestModel.getDescription(),
                createBookRequestModel.getNumPages());
        bookRepository.saveAndFlush(newBook);
    }

    /**
     * Updates a book in the system.
     *
     * @param updateBookRequestModel contains the new information for the book
     * @param bearerToken            is the jwt token of the user that made the request
     */
    @Transactional
    public void updateBook(UpdateBookRequestModel updateBookRequestModel, String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN)) {
            throw new IllegalCallerException("Only admins may edit the books from the system!");
        }
        var optBook = bookRepository.findById(UUID.fromString(updateBookRequestModel.getId()));
        if (optBook.isEmpty()) {
            throw new IllegalArgumentException("The book does not exist!");
        }

        Book currentBook = optBook.get();
        UUID curId = currentBook.getId();
        bookRepository.deleteById(currentBook.getId());

        Book newBook = new Book(updateBookRequestModel.getTitle(),
                updateBookRequestModel.getAuthors(),
                updateBookRequestModel.getGenres(),
                updateBookRequestModel.getDescription(),
                updateBookRequestModel.getNumPages());
        newBook.setId(curId);
        bookRepository.saveAndFlush(newBook);
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
            throw new IllegalCallerException("Only admins may delete books from the system!");
        }
        var optBook = bookRepository.findById(UUID.fromString(bookId));
        if (optBook.isEmpty()) {
            throw new IllegalArgumentException("The book does not exist!");
        }

        userRepository.removeBookFromUsersFavorites(UUID.fromString(bookId));

        bookRepository.deleteById(UUID.fromString(bookId));
    }

    private Authority getAuthority(String bearerToken) {
        return jwtService.extractAuthorization(bearerToken.substring(7));
    }
}
