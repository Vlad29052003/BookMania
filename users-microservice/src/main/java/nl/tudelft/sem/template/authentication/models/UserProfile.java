package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends UserModel {
    @Getter
    @Setter
    private Authority authority;
    @Setter
    private boolean isDeactivated;
    @Setter
    private boolean isPrivate;
    @Setter
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

    @SuppressWarnings("")
    public boolean getIsDeactivated() {
        return isDeactivated;
    }

    @SuppressWarnings("")
    public boolean getIsPrivate() {
        return isPrivate;
    }

    @SuppressWarnings("")
    public boolean getIs2faEnabled() {
        return is2faEnabled;
    }
}
