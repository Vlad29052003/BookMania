package nl.tudelft.sem.template.authentication.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private String netId;
    private String email;
    private String name;
    private String bio;
    private String location;
    private List<Genre> favouriteGenres;
    private Book favouriteBook;
}
