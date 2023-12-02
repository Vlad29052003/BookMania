package nl.tudelft.sem.template.authentication.domain.book;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {
    private final transient BookRepository bookRepository;
    private final transient UserRepository userRepository;
    private final transient JwtService jwtService;

    @Autowired
    public BookService(BookRepository bookRepository, UserRepository userRepository, JwtService jwtService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public void deleteBook(String bookId, String bearerToken) {
        Authority authority = jwtService.extractAuthorization(bearerToken);
        if (!authority.equals(Authority.ADMIN)) {
            throw new IllegalCallerException("Only admins may delete books from the system!");
        }
        var optBook = bookRepository.findById(UUID.fromString(bookId));
        if (optBook.isEmpty()) {
            throw new IllegalArgumentException("The book does not exist!");
        }
        for (AppUser user : optBook.get().getUsersWithBookAsFavorite()) {
            user.setFavouriteBook(null);
            userRepository.save(user);
        }

        bookRepository.deleteById(UUID.fromString(bookId));
    }

    public void addBook(CreateBookRequestModel createBookRequestModel, String bearerToken) {
        Authority authority = jwtService.extractAuthorization(bearerToken);

        if (!authority.equals(Authority.ADMIN)) {
            throw new IllegalCallerException("Only admins may delete books from the system!");
        }

        List<Book> books = bookRepository.findByTitle(createBookRequestModel.getTitle());
        boolean invalid = books.stream().anyMatch(x -> Set.of(createBookRequestModel.getAuthors()).equals(Set.of(x.getAuthors())));

        if (invalid) {
            throw new IllegalArgumentException("The book is already in the system!");
        }

        Book newBook = new Book(createBookRequestModel.getTitle(),
                createBookRequestModel.getAuthors(),
                createBookRequestModel.getGenre(),
                createBookRequestModel.getDescription(),
                createBookRequestModel.getNumPages());
        bookRepository.saveAndFlush(newBook);
    }
}
