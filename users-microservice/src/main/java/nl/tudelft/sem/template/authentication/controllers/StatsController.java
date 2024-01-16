package nl.tudelft.sem.template.authentication.controllers;


import nl.tudelft.sem.template.authentication.domain.stats.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<?> getLoginStats(@PathVariable String userId) throws ResponseStatusException {

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
    @GetMapping("/popularBooks")
    public ResponseEntity<?> getMostPopularBooks() throws ResponseStatusException {
        try {
            return ResponseEntity.ok(statsService.mostPopularBooks());
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Gets most popular 5 genres by number of users that have that genre as their favourite.
     *
     * @return the list of genres
     */
    @GetMapping("/popularGenres")
    public ResponseEntity<?> getMostPopularGenres() throws ResponseStatusException {
        try {
            return ResponseEntity.ok(statsService.mostPopularGenres());
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }


}



