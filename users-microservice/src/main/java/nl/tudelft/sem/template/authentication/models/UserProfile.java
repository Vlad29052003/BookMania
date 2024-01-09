package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends UserModel {
    private Authority authority;
    private boolean isDeactivated;
    private boolean isPrivate;
    private boolean is2faEnabled;

    /**
     * Constructor for the UserProfile Class that takes an AppUser as input.
     *
     * @param user the AppUser for which the profile is made
     */
    public UserProfile(AppUser user) {
        super(user);
        this.authority = user.getAuthority();
        this.isDeactivated = user.isDeactivated();
        this.isPrivate = user.isPrivate();
        this.is2faEnabled = user.is2faEnabled();
    }
}
