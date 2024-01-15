package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.template.authentication.controllers.StatsController;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.stats.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;




public class StatsControllerTests {

    private transient StatsController statsController;
    private transient StatsService statsService;

    @BeforeEach
    public void setUp() {
        statsService = mock(StatsService.class);
        this.statsController = new StatsController(statsService);
    }

    @Test
    public void testGetLoginStats() {
        when(statsService.getLoginStats("1"))
                .thenReturn(1);
        assertThat(statsController.getLoginStats("1"))
                .isEqualTo(ResponseEntity.ok(1));
    }

    @Test
    public void testGetLoginStatsUnauthorized() {
        when(statsService.getLoginStats("1"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized!"));
        assertThat(statsController.getLoginStats("1"))
                .isEqualTo(new ResponseEntity<>("401 UNAUTHORIZED \"Unauthorized!\"", HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetMostPopularBooks() {

        Book book1 = new Book("title",
                List.of("author"), List.of(Genre.FANTASY),
                "description", 1);
        Book book2 = new Book("title2",
                List.of("author2"), List.of(Genre.FANTASY),
                "description2", 100);

        List<Book> mostPopularBooks = List.of(book1, book2);
        when(statsService.mostPopularBooks())
                .thenReturn(mostPopularBooks);
        assertThat(statsController.getMostPopularBooks())
                .isEqualTo(ResponseEntity.ok(mostPopularBooks));
    }

    @Test
    public void testGetMostPopularBooksUnauthorized() {
        when(statsService.mostPopularBooks())
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized!"));
        assertThat(statsController.getMostPopularBooks())
                .isEqualTo(new ResponseEntity<>("401 UNAUTHORIZED \"Unauthorized!\"", HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetMostPopularBooksNotFound() {
        when(statsService.mostPopularBooks())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        assertThat(statsController.getMostPopularBooks())
                .isEqualTo(new ResponseEntity<>("404 NOT_FOUND \"Not found!\"", HttpStatus.NOT_FOUND));
    }

    @Test
    public void testGetMostPopularGenres() {

        Genre genre1 = Genre.FANTASY;
        Genre genre2 = Genre.HORROR;

        List<Genre> mostPopularGenres = List.of(genre1, genre2);
        when(statsService.mostPopularGenres())
                .thenReturn(mostPopularGenres);
        assertThat(statsController.getMostPopularGenres())
                .isEqualTo(ResponseEntity.ok(mostPopularGenres));
    }

    @Test
    public void testGetMostPopularGenresUnauthorized() {
        when(statsService.mostPopularGenres())
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized!"));
        assertThat(statsController.getMostPopularGenres())
                .isEqualTo(new ResponseEntity<>("401 UNAUTHORIZED \"Unauthorized!\"", HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetMostPopularGenresNotFound() {
        when(statsService.mostPopularGenres())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        assertThat(statsController.getMostPopularGenres())
                .isEqualTo(new ResponseEntity<>("404 NOT_FOUND \"Not found!\"", HttpStatus.NOT_FOUND));
    }
}
