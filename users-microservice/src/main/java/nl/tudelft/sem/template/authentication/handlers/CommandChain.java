package nl.tudelft.sem.template.authentication.handlers;

import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.AddBookStrategy;
import nl.tudelft.sem.template.authentication.strategies.DeleteBookStrategy;
import nl.tudelft.sem.template.authentication.strategies.EditBookStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandChain {
    @Getter
    private final transient CheckUserExistenceHandler checkUserExistenceHandler;
    @Getter
    private final transient CheckAuthorityHandler checkAuthorityHandler;
    @Getter
    private final transient CheckAuthorHandler checkAuthorHandler;

    private final transient AddBookStrategy addBookStrategy;
    private final transient EditBookStrategy editBookStrategy;
    private final transient DeleteBookStrategy deleteBookStrategy;

    /**
     * Creates a new CommandChain object.
     *
     * @param userRepository is the UserRepository.
     * @param bookService is the BookService.
     */
    @Autowired
    public CommandChain(UserRepository userRepository, BookService bookService) {
        this.checkUserExistenceHandler = new CheckUserExistenceHandler(userRepository);
        this.checkAuthorityHandler = new CheckAuthorityHandler(userRepository);
        this.checkAuthorHandler = new CheckAuthorHandler(userRepository);
        this.checkUserExistenceHandler.setNext(checkAuthorityHandler);
        this.checkAuthorityHandler.setNext(checkAuthorHandler);

        this.addBookStrategy = new AddBookStrategy(bookService);
        this.editBookStrategy = new EditBookStrategy(bookService);
        this.deleteBookStrategy = new DeleteBookStrategy(bookService);
    }

    public void handle(Username username, Authority authority, Book book) {
        FilterBookRequestModel filterBookRequestModel = new FilterBookRequestModel(username, authority, book);
        checkUserExistenceHandler.filter(filterBookRequestModel);
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

    /**
     * Changes the strategy to editBookStrategy for all filters.
     */
    public void setDeleteBookStrategy() {
        this.checkUserExistenceHandler.setStrategy(deleteBookStrategy);
        this.checkAuthorityHandler.setStrategy(deleteBookStrategy);
        this.checkAuthorHandler.setStrategy(deleteBookStrategy);
    }
}
