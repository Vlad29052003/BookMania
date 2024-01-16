package nl.tudelft.sem.template.authentication.domain.stats;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final transient UserRepository userRepository;

    private final transient StatsRepository statsRepository;

    private final transient BookRepository bookRepository;


    /**
     * Creates a StatsService service.
     *
     * @param userRepository  the user repository
     * @param statsRepository the stats repository
     */
    @Autowired
    public StatsService(UserRepository userRepository,
                        StatsRepository statsRepository,
                        BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
        this.bookRepository = bookRepository;
    }


    /**
     * Get login statistics for a user (number of times they accessed /authenticate).
     *
     * @param userId the id of the user
     * @return the number of times the user logged in
     */
    public int getLoginStats(String userId) {
        Optional<Stats> stats = statsRepository.findById(UUID.fromString(userId));
        if (stats.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        return stats.get().getNumberOfLogins();
    }

    /**
     * Get the 3 most popular books by user favourites.
     *
     * @return the list of books
     */
    public List<Book> mostPopularBooks() {
        return userRepository.getMostPopularBooks()
                .stream()
                .map(bookRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    /**
     * Get the 3 most popular genres by user favourites.
     *
     * @return the list of genres
     */
    public List<Genre> mostPopularGenres() {
        return userRepository.getMostPopularGenres();
    }
}
