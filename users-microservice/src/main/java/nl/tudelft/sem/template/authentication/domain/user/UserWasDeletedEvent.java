package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

@Getter
public class UserWasDeletedEvent {
    private final transient Username username;

    public UserWasDeletedEvent(Username username) {
        this.username = username;
    }
}
