package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.domain.HasEvents;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;

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
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Getter
    @Column(name = "net_id", nullable = false, unique = true)
    @Convert(converter = NetIdAttributeConverter.class)
    private NetId netId;

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
    @OneToOne
    @JoinColumn(name = "favourite_book")
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

    /**
     * Create new application user.
     *
     * @param netId The NetId for the new user
     * @param password The password for the new user
     */
    public AppUser(NetId netId, String email, HashedPassword password) {
        this.netId = netId;
        this.email = email;
        this.password = password;
        this.favouriteGenres = new ArrayList<>();
        this.follows = new ArrayList<>();
        this.followedBy = new ArrayList<>();
        this.recordThat(new UserWasCreatedEvent(netId));
    }

    public void changePassword(HashedPassword password) {
        this.password = password;
        this.recordThat(new PasswordWasChangedEvent(this));
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
        return id == (appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId);
    }
}
