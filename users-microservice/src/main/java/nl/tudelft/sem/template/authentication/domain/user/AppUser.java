package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.domain.HasEvents;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
public class AppUser extends HasEvents {
    /**
     * Identifier for the application user.
     */
    @Getter
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;
    @Getter
    @Column(name = "username", nullable = false, unique = true)
    @Convert(converter = UsernameAttributeConverter.class)
    private Username username;

    @Getter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Getter
    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Getter
    @Setter
    @Column(name = "name")
    private String name;

    @Getter
    @Setter
    @Column(name = "bio")
    private String bio;

    @Getter
    @Setter
    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Getter
    @Setter
    @Column(name = "location")
    private String location;

    @Getter
    @Setter
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "user_favourite_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private List<Genre> favouriteGenres;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book favouriteBook;

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(
            name = "user_connections",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id"))
    private List<AppUser> follows;

    @Getter
    @Setter
    @ManyToMany(mappedBy = "follows")
    private List<AppUser> followedBy;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    private Authority authority;

    @Getter
    @Setter
    @Column(name = "deactivated")
    private boolean isDeactivated;

    /**
     * Create new application user.
     *
     * @param username    The Username for the new user
     * @param password The password for the new user
     */
    public AppUser(Username username, String email, HashedPassword password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.favouriteGenres = new ArrayList<>();
        this.follows = new ArrayList<>();
        this.followedBy = new ArrayList<>();
        this.authority = Authority.REGULAR_USER;
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
        return Objects.hash(username);
    }

    public void recordUserWasCreated() {
        this.recordThat(new UserWasCreatedEvent(this));
    }

    public void recordUserWasDeleted() {
        this.recordThat(new UserWasDeletedEvent(this));
    }
}
