package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserWasDeletedEvent {
    private final transient UUID id;

    public UserWasDeletedEvent(UUID id) {
        this.id = id;
    }
}
