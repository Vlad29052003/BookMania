package nl.tudelft.sem.template.authentication.controllers;

import java.util.Arrays;
import nl.tudelft.sem.template.authentication.domain.stats.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/stats")
public class StatsController {

    private final transient StatsService statsService;

    /**
     * Instantiates a new UsersController.
     *
     * @param statsService the stats service.
     */
    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }


    /**
     * Get login statistics for a user (number of times they accessed /authenticate).
     *
     * @param userId the id of the user
     * @return the number of times the user logged in
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getLoginStats(@RequestBody String userId) throws ResponseStatusException {
        try {
            return ResponseEntity.ok(statsService.getLoginStats(userId));
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }


    /**
     * Gets most popular 5 books by number of users that have that book as their favourite.
     *
     * @return the list of books
     */
    @GetMapping("/mostPopularBooks")
    public ResponseEntity<?> getMostPopularBooks() throws ResponseStatusException {
        try {
            return ResponseEntity.ok(statsService.mostPopularBooks());
        } catch (ResponseStatusException e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Gets most popular 5 genres by number of users that have that genre as their favourite.
     *
     * @return the list of genres
     */
    @GetMapping("/mostPopularGenres")
    public ResponseEntity<?> getMostPopularGenres() throws ResponseStatusException {
        try {
            return ResponseEntity.ok(statsService.mostPopularGenres());
        } catch (ResponseStatusException e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }


}



