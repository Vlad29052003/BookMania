package nl.tudelft.sem.template.authentication.domain.user;

import java.util.UUID;
import lombok.Getter;

@Getter
public class UserWasDeletedEvent {

    private final transient AppUser user;

    private final transient UUID adminId;

    public UserWasDeletedEvent(AppUser user, UUID adminId) {
        this.user = user;
        this.adminId = adminId;
    }
}
