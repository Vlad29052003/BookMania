package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.authentication.domain.HasEvents;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * A DDD entity representing an application user in our domain.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
@ToString
public class AppUser extends HasEvents {
    /**
     * Identifier for the application user.
     */
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;

    @Setter
    @Column(name = "username", nullable = false, unique = true)
    @Convert(converter = UsernameAttributeConverter.class)
    private Username username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Setter
    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "bio")
    private String bio;

    @Setter
    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Setter
    @Column(name = "location")
    private String location;

    @Setter
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "user_favourite_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private List<Genre> favouriteGenres;

    @Setter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book favouriteBook;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_connections",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id"))
    private List<AppUser> follows;

    @Setter
    @ManyToMany(mappedBy = "follows", fetch = FetchType.EAGER)
    private List<AppUser> followedBy;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    private Authority authority;

    @Setter
    @Column(name = "deactivated")
    private boolean isDeactivated;

    @Setter
    @Column(name = "private")
    private boolean isPrivate;

    @Setter
    @Column(name = "two_factor_auth")
    private boolean is2faEnabled;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Create new application user.
     *
     * @param username The Username for the new user
     * @param password The password for the new user
     */
    public AppUser(Username username, String email, HashedPassword password) {
        this.username = username;
        setEmailAddress(email);
        this.password = password;
        this.favouriteGenres = new ArrayList<>();
        this.follows = new ArrayList<>();
        this.followedBy = new ArrayList<>();
        this.authority = Authority.REGULAR_USER;
    }

    /**
     * Email setter.
     */
    public void setEmail(String newEmail) {
        setEmailAddress(newEmail);
    }

    /**
     * Email setter helper.
     */
    private void setEmailAddress(String newEmail) {
        if (pattern.matcher(newEmail).matches()) {
            this.email = newEmail;
        } else {
            throw new IllegalArgumentException("Illegal email address!");
        }
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return id.equals(appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Follows a user.
     *
     * @param user - The user to follow.
     */
    public void follow(AppUser user) {
        if (!follows.contains(user)) {
            follows.add(user);
            user.followedBy.add(this);
        }
    }

    /**
     * Unfollows a user.
     *
     * @param user - The user to unfollow.
     */

    public void unfollow(AppUser user) {
        if (follows.contains(user)) {
            follows.remove(user);
            user.followedBy.remove(this);
        }
    }

    public void recordUserWasCreated() {
        this.recordThat(new UserWasCreatedEvent(this));
    }
}
