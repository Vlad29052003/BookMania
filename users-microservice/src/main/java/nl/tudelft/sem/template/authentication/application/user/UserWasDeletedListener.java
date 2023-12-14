package nl.tudelft.sem.template.authentication.application.user;

import nl.tudelft.sem.template.authentication.domain.user.UserWasDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when an user entity is deleted.
 * which has stored events of type: UserWasDeleted.
 */
@Component
public class UserWasDeletedListener {
/**
     * The name of the function indicated which event is listened to.
     * The format is on EVENTNAME.
     *
     * @param event The event to react to
     */
     @EventListener
     public void onAccountWasDeleted(UserWasDeletedEvent event) {
          // Handler code here
          // Call endpoint of bookshelves microservice to delete an user
     }
}
