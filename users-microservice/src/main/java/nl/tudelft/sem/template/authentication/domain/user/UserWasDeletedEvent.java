package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

@Getter
public class UserWasDeletedEvent {
    private final transient AppUser user;

    public UserWasDeletedEvent(AppUser user) {
        this.user = user;
    }
}
