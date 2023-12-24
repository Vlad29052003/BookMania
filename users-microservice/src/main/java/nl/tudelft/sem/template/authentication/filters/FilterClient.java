package nl.tudelft.sem.template.authentication.filters;

import lombok.Getter;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.strategy.AddBookStrategy;
import nl.tudelft.sem.template.authentication.strategy.EditBookStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilterClient {
    private final transient JwtService jwtService;
    private final transient UserRepository userRepository;
    private final transient BookService bookService;
    @Getter
    private final transient CheckUserExistenceHandler checkUserExistenceHandler;
    @Getter
    private final transient CheckAuthorityHandler checkAuthorityHandler;
    @Getter
    private final transient CheckAuthorHandler checkAuthorHandler;

    private final transient AddBookStrategy addBookStrategy;
    private final transient EditBookStrategy editBookStrategy;

    /**
     * Creates a new FilterClient object.
     *
     * @param jwtService is the JwtService.
     * @param userRepository is the UserRepository.
     * @param bookService is the BookService.
     */
    @Autowired
    public FilterClient(JwtService jwtService, UserRepository userRepository, BookService bookService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.bookService = bookService;

        this.checkUserExistenceHandler = new CheckUserExistenceHandler(jwtService, userRepository);
        this.checkAuthorityHandler = new CheckAuthorityHandler(jwtService);
        this.checkAuthorHandler = new CheckAuthorHandler(jwtService, userRepository);
        this.checkUserExistenceHandler.setNext(checkAuthorityHandler);
        this.checkAuthorityHandler.setNext(checkAuthorHandler);

        this.addBookStrategy = new AddBookStrategy(bookService);
        this.editBookStrategy = new EditBookStrategy(bookService);
    }

    public void handle(Book book, String bearerToken) {
        checkUserExistenceHandler.filter(book, bearerToken);
    }

    /**
     * Changes the strategy to addBookStrategy for all filters.
     */
    public void setAddBookStrategy() {
        this.checkUserExistenceHandler.setStrategy(addBookStrategy);
        this.checkAuthorityHandler.setStrategy(addBookStrategy);
        this.checkAuthorHandler.setStrategy(addBookStrategy);
    }

    /**
     * Changes the strategy to editBookStrategy for all filters.
     */
    public void setEditBookStrategy() {
        this.checkUserExistenceHandler.setStrategy(editBookStrategy);
        this.checkAuthorityHandler.setStrategy(editBookStrategy);
        this.checkAuthorHandler.setStrategy(editBookStrategy);
    }
}
