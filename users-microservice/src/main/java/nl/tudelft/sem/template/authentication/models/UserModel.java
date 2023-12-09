package nl.tudelft.sem.template.authentication.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;

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
    private boolean isPrivate;

    public UserModel(AppUser user) {
        this.netId = user.getUsername().toString();
        this.email = user.getEmail();
        this.name = user.getName();
        this.bio = user.getBio();
        this.location = user.getLocation();
        this.favouriteGenres = user.getFavouriteGenres();
        this.favouriteBook = user.getFavouriteBook();
        this.isPrivate = user.isPrivate();
    }
}
