package nl.tudelft.sem.template.authentication.domain.stats;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test"})
public class StatsServiceTests {

    private transient StatsService statsService;

    private transient StatsRepository statsRepository;

    private transient UserRepository userRepository;

    private transient BookRepository bookRepository;

    /**
     * Set up the testing environment:
     * mock the repositories and create a new StatsService.
     */
    @BeforeEach
    public void setUp() {
        statsRepository = mock(StatsRepository.class);
        userRepository = mock(UserRepository.class);
        bookRepository = mock(BookRepository.class);
        statsService = new StatsService(userRepository, statsRepository, bookRepository);
    }

    @Test
    public void testGetLoginStatsTest() {
        UUID id = UUID.randomUUID();
        when(statsRepository.findById(id)).thenReturn(Optional.of(new Stats(id, 1)));

        assertThat(statsService.getLoginStats(id.toString())).isEqualTo(1);
    }

    @Test
    public void testMostPopularBooksOneEmpty() {
        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Book book1 = new Book("title",
                List.of("author"), List.of(Genre.FANTASY),
                "description", 1);
        Book book2 = new Book("title2",
                List.of("author2"), List.of(Genre.FANTASY),
                "description2", 100);


        when(userRepository.getMostPopularBooks())
                .thenReturn(List.of(id, id2));
        when(bookRepository.findById(id))
                .thenReturn(Optional.of(book1));

        when(bookRepository.findById(id2))
                .thenReturn(Optional.of(book2));

        assertThat(statsService.mostPopularBooks().size()).isEqualTo(2);
        assertThat(statsService.mostPopularBooks()).containsExactlyInAnyOrder(book1, book2);
    }


    @Test
    public void testMostPopularBooks() {
        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Book book1 = new Book("title",
                List.of("author"), List.of(Genre.FANTASY),
                "description", 1);
        Book book2 = new Book("title2",
                List.of("author2"), List.of(Genre.FANTASY),
                "description2", 100);


        when(userRepository.getMostPopularBooks())
                .thenReturn(List.of(id, id2));
        when(bookRepository.findById(id))
                .thenReturn(Optional.of(book1));

        when(bookRepository.findById(id2))
                .thenReturn(Optional.empty());

        assertThat(statsService.mostPopularBooks().size()).isEqualTo(1);
        assertThat(statsService.mostPopularBooks()).containsExactlyInAnyOrder(book1);
    }


    @Test
    public void testMostPopularGenres() {

        when(userRepository.getMostPopularGenres())
                .thenReturn(List.of(Genre.FANTASY, Genre.DRAMA));

        assertThat(statsService.mostPopularGenres().size()).isEqualTo(2);
        assertThat(statsService.mostPopularGenres()).containsExactlyInAnyOrder(Genre.FANTASY, Genre.DRAMA);
    }


}
