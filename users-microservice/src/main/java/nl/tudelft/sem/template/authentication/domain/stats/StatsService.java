package nl.tudelft.sem.template.authentication.domain.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
     */
    public int getLoginStats(String userId) {
        return statsRepository.findById(UUID.fromString(userId)).get().getNumberOfLogins();
    }

    /**
     * Get most popular book.
     */
    public List<Book> mostPopularBooks() {
        try {
            return userRepository.getMostPopularBooks()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(bookRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Get most popular genres.
     */
    public List<Genre> mostPopularGenres() {
        try {
            return userRepository.getMostPopularGenres()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
